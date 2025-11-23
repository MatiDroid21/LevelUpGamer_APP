package data.model

data class ProductoDTO(
    val idProducto: Long? = null,
    val nombre: String = "",
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val stock: Int = 0,
    val idCategoria: Long? = null,
    val nombreCategoria: String? = null,
    val imagenNombre: String? = null,
    val imagenTipo: String? = null,
    val imagenTamano: Long? = null,
    val imagenBase64: String? = null
)