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
    }

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {

        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {

        val server = servers[position]

        // Mostrar detalles del servidor
        holder.ipTextView.text = server.hostname ?: "N/A"
        holder.addressTextView.text = server.ip ?: "N/A"
        holder.versionTextView.text = server.version ?: "N/A"
        holder.playersTextView.text = "Online: ${server.players?.online ?: 0}/${server.players?.max ?: 0}"

        // Configurar el icono del servidor si está disponible
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

        // Mostrar el motd con HTML si está disponible
        val motdHtml = server.motd?.html?.joinToString("<br>") ?: "N/A"
        holder.motdTextView.text = Html.fromHtml(motdHtml, Html.FROM_HTML_MODE_COMPACT)

        // Manejar clics en los elementos de la lista
        holder.itemView.setOnClickListener {

            // Mostrar el Dialog con los detalles del servidor
            val dialog = ServerDetailDialog(context, server)
            dialog.show()
        }

        // Manejar clics largos para editar o eliminar el servidor
        holder.itemView.setOnLongClickListener {
            showEditDeleteDialog(server)
            true
        }
    }

    override fun getItemCount(): Int = servers.size

    // Función para actualizar los datos del adaptador
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newServers: List<Server>) {
        servers = newServers
        notifyDataSetChanged()
    }

    // Función para mostrar el AlertDialog de editar/eliminar servidor
    private fun showEditDeleteDialog(server: Server) {
        val options = arrayOf("Editar", "Eliminar")
        AlertDialog.Builder(context)
            .setTitle("Selecciona una opción")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> {
                        // Editar servidor
                        showEditDialog(server)
                    }
                    1 -> {
                        // Eliminar servidor
                        viewModel.deleteServer(server.ip ?: "")
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    // Función para mostrar el diálogo de edición de IP
    private fun showEditDialog(server: Server) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Editar IP")

        // Set up the input
        val input = EditText(context)
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            val newIp = input.text.toString().trim()
            if (newIp.isNotEmpty()) {
                // Mostrar GIF de carga
                viewModel.editServer(server, newIp)
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}