package data.model

data class UsuarioDTO(
    val idUsuario: Long? = null,
    val nombre: String = "",
    val rut: String = "",
    val email: String = "",
    val fechaNacimiento: String? = null,
    val puntos: Int = 0,
    val codigoReferido: String? = null,
    val referidoPor: Long? = null,
    val rolNombre: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val fotoNombre: String? = null,
    val fotoTipo: String? = null,
    val fotoTamano: Long? = null
)