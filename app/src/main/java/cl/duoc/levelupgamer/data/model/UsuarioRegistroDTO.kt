package data.model

data class UsuarioRegistroDTO(
    val nombre: String,
    val rut: String,
    val email: String,
    val contrasena: String,
    val fechaNacimiento: String?,  // Formato ISO: "1990-01-15" o null
    val idRol: Long,
    val codigoReferido: String?,
    val direccion: String,
    val telefono: String
)
