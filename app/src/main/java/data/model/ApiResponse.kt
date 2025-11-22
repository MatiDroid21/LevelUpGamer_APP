package data.model

data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null,
    val code: Int = 0
)