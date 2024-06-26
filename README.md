Pantalla 1: Splash
Esta es la pantalla de bienvenida de la aplicación, donde se muestra el logotipo o nombre de la aplicación durante el proceso de carga inicial.

Pantalla 2: Iniciar sesión
En esta pantalla, los usuarios pueden iniciar sesión en la aplicación. Se presentan dos campos de entrada para el correo electrónico o nombre de usuario y la contraseña. También se incluye la opción "¿Aún no tienes una cuenta?", que redirige a los usuarios a la pantalla de registro.

Pantalla 3: Registro
Esta pantalla se utiliza para crear una nueva cuenta en la aplicación. Los usuarios deben proporcionar su correo electrónico y elegir una contraseña. También deben confirmar la contraseña para evitar errores.

Pantalla 4: Página Principal
La página principal de la aplicación muestra todos los servidores agregados por el usuario. Cada servidor se presenta con una vista previa de la información relevante, como el estado en línea, el número de jugadores, etc. Los usuarios pueden acceder a la información detallada de cada servidor y tienen la opción de agregar o eliminar servidores de la lista. Además, pueden filtrar los servidores por nombre o fecha de ingreso.

Pantalla 5: Agregar Nuevo Servidor
Esta pantalla permite al usuario agregar un nuevo servidor a la lista. Se solicita al usuario que ingrese un nombre para el servidor (a elección del usuario) y la dirección IP específica del servidor.

Pantalla 6: Información del Servidor
En esta pantalla, los usuarios pueden ver toda la información detallada de un servidor seleccionado. Se muestran los siguientes elementos:
* Información del servidor:
* Dirección IP
* Puerto
* Nombre del servidor
* Estado del servidor (en línea u offline)
* Número de jugadores en línea
* Versión del servidor
* Lista de jugadores en línea (opcional)
* Icono del servidor (opcional)
* Software del servidor (opcional)
* Mapa del servidor (opcional)
* Modo de juego del servidor (opcional)
* ID del servidor (solo para servidores Bedrock, opcional)
* Descripción del servidor (MOTD - mensaje del día, opcional)
* Lista de plugins instalados en el servidor (opcional)
* Lista de mods instalados en el servidor (opcional)
* Información adicional (opcional)
Descripción de los Endpoints del API Rest:

 Endpoint para obtener el estado del servidor:

 Endpoint: https://api.mcsrvstat.us/3/<address>

Descripción: Este endpoint proporciona información detallada sobre el estado de un servidor de Minecraft, incluyendo si está en línea u offline, la dirección IP, el puerto, el nombre del servidor, el número de jugadores en línea, la versión del servidor, entre otros datos.

    Endpoint para obtener el estado simple del servidor:

  Endpoint: https://api.mcsrvstat.us/simple/<address>

 Descripción: Este endpoint proporciona una respuesta simple que indica si el servidor de Minecraft está en línea u offline. Es útil si solo necesitas saber el estado del servidor sin detalles adicionales.



Descripción de los Datos a Almacenar de Forma Remota:


* Datos de usuario:
* Nombre de usuario
* Correo electrónico
* Datos de autenticación (token, sesión, etc.)

* Datos de configuración (servidores agregados)

Wireframe:
https://www.figma.com/board/L4x1rXKcNrDaHuNLEzN6Bw/Wireframe-Proyecto-Desarrollo-de-Aplicaciones-I?node-id=0-1&t=PDNgfgBzvwimTUUh-0
