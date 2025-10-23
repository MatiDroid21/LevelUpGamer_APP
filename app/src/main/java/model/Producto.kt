package model
import androidx.annotation.DrawableRes
import cl.duoc.levelupgamer.R

data class Producto(
    val id: Int,
    val titulo: String,
    val precio: String,
    @DrawableRes val imagenRes: Int,
    val categoria: String,
    val descripcion: String
)

val productosDemo = listOf(
    Producto(
        id = 1,
        titulo = "PS5",
        precio = "700.000",
        imagenRes = R.drawable.ps5,
        categoria = "Consolas",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 2,
        titulo = "MSI KATANA 17",
        precio = "900.000",
        imagenRes = R.drawable.katana17,
        categoria = "Laptops",
        descripcion = "Laptop gamer msi de alto rendimiento"
    ),
    Producto(
        id = 3,
        titulo = "Asus TUF 16",
        precio = "500.000",
        imagenRes = R.drawable.tuf,
        categoria = "Laptops",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 4,
        titulo = "Mouse Gamer Logitech",
        precio = "3.500 (1kg)",
        imagenRes = R.drawable.logitech,
        categoria = "Perifericos",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 5,
        titulo = "Ram kingston fury 16gb ddr5",
        precio = "25.000",
        imagenRes = R.drawable.fury_ram,
        categoria = "Componentes",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 6,
        titulo = "HP Victus 16",
        precio = "650.000",
        imagenRes = R.drawable.victus,
        categoria = "Laptops",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 7,
        titulo = "Xbox Series X",
        precio = "700.000",
        imagenRes = R.drawable.xboxseriesx,
        categoria = "Consolas",
        descripcion = "Xbox pa jugar"
    ),
    Producto(
        id = 8,
        titulo = "Acer Predator",
        precio = "1.200.000",
        imagenRes = R.drawable.predator,
        categoria = "Laptops",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 9,
        titulo = "CLUE",
        precio = "10.000",
        imagenRes = R.drawable.clue,
        categoria = "juegos de mesa",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 10,
        titulo = "UNO",
        precio = "5.000",
        imagenRes = R.drawable.uno,
        categoria = "juegos de mesa",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 11,
        titulo = "Silla 1",
        precio = "100.000",
        imagenRes = R.drawable.silla1,
        categoria = "Sillas",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 12,
        titulo = "Silla 2",
        precio = "100.000",
        imagenRes = R.drawable.silla2,
        categoria = "Sillas",
        descripcion = "Aca se describe el producto"
    ),
    Producto(
        id = 13,
        titulo = "Silla 3",
        precio = "100.000",
        imagenRes = R.drawable.silla3,
        categoria = "Sillas",
        descripcion = "Aca se describe el producto"),
    Producto(
        id = 14,
        titulo = "Silla 4",
        precio = "100.000",
        imagenRes = R.drawable.silla4,
        categoria = "Sillas",
        descripcion = "Aca se describe el producto"
    )
)
