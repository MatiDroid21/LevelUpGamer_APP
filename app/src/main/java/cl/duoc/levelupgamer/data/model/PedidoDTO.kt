package data.model

data class PedidoDTO(
    val idPedido: Long? = null,
    val idUsuario: Long,
    val total: Double,
    val direccion: String,
    val estado: String = "PENDIENTE",
    val fechaPedido: String? = null,
    val fechaEstimada: String? = null,
    val detalles: List<PedidoDetalleDTO> = emptyList()
)
