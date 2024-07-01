package com.example.minecraftserverstatus.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.text.Html
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minecraftserverstatus.R
import com.example.minecraftserverstatus.model.Server

class ServerAdapter(private var servers: List<Server>, private val viewModel: ServerViewModel) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    class ServerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val serverIcon: ImageView = view.findViewById(R.id.server_icon_imageview)
        val ipTextView: TextView = view.findViewById(R.id.ip_textview)
        val addressTextView: TextView = view.findViewById(R.id.address_textview)
        val versionTextView: TextView = view.findViewById(R.id.version_textview)
        val playersTextView: TextView = view.findViewById(R.id.players_textview)
        val motdTextView: TextView = view.findViewById(R.id.motd_textview)
        val favoriteButton: ImageView = view.findViewById(R.id.favoriteButton)
    }

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]

        holder.ipTextView.text = server.hostname ?: "N/A"
        holder.addressTextView.text = server.ip ?: "N/A"
        holder.versionTextView.text = server.version ?: "N/A"
        holder.playersTextView.text = "Online: ${server.players?.online ?: 0}/${server.players?.max ?: 0}"

        // Set server icon
        server.icon?.let { iconBase64 ->
            try {
                val imageBytes = Base64.decode(iconBase64.split(",")[1], Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.serverIcon.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                holder.serverIcon.setImageResource(R.drawable.default_server_icon)
            }
        } ?: run {
            holder.serverIcon.setImageResource(R.drawable.default_server_icon)
        }

        // Set MOTD with HTML if available
        val motdHtml = server.motd?.html?.joinToString("<br>") ?: "N/A"
        holder.motdTextView.text = Html.fromHtml(motdHtml, Html.FROM_HTML_MODE_COMPACT)

        // Set favorite button state
        updateFavoriteButtonState(holder.favoriteButton, server)

        // Handle click events
        holder.itemView.setOnClickListener {
            showServerDetailDialog(server)
        }

        holder.favoriteButton.setOnClickListener {
            toggleFavoriteState(server)
        }

        holder.itemView.setOnLongClickListener {
            showEditDeleteDialog(server)
            true
        }
    }

    override fun getItemCount(): Int = servers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newServers: List<Server>) {
        servers = newServers
        notifyDataSetChanged()
    }

    private fun showEditDeleteDialog(server: Server) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(context)
            .setTitle("Selecciona una opción")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditDialog(server)
                    1 -> viewModel.deleteServer(server.ip ?: "")
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditDialog(server: Server) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Editar IP")
        val input = EditText(context)
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            val newIp = input.text.toString().trim()
            if (newIp.isNotEmpty()) {
                viewModel.editServer(server, newIp)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun showServerDetailDialog(server: Server) {
        val dialog = ServerDetailDialog(context, server)
        dialog.show()
    }

    private fun toggleFavoriteState(server: Server) {
        if (viewModel.isServerFavorite(server.ip ?: "")) {
            viewModel.removeServerFromFavorites(server)
        } else {
            viewModel.addServerToFavorites(server)
        }
    }

    private fun updateFavoriteButtonState(favoriteButton: ImageView, server: Server) {
        if (viewModel.isServerFavorite(server.ip ?: "")) {
            favoriteButton.setImageResource(R.drawable.ic_star_filled)
        } else {
            favoriteButton.setImageResource(R.drawable.ic_star_border)
        }
    }
}
