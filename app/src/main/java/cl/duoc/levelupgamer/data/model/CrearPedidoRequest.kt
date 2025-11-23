package data.model

data class CrearPedidoRequest(
    val idUsuario: Long,
    val total: Double,
    val direccion: String,
    val detalles: List<PedidoDetalleDTO>
)

data class PedidoDetalleDTO(
    val idProducto: Long,
    val cantidad: Int,
    val precioUnitario: Double
)