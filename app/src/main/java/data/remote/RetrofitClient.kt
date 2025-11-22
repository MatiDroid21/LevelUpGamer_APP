package cl.duoc.levelupgamer.data.remote

import data.remote.ProductosApiService
import data.remote.UsuariosApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ========== CONFIGURACIÓN ==========
    private const val USUARIOS_BASE_URL = "http://192.168.0.101:8080/"
    private const val PRODUCTOS_BASE_URL = "http://192.168.0.101:8081/"
    private const val API_KEY = "lvlupgamer1306"

    // ========== INTERCEPTORS COMUNES ==========
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val apiKeyInterceptor = { chain: okhttp3.Interceptor.Chain ->
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .addHeader("x-api-key", API_KEY)
            .build()
        chain.proceed(requestWithApiKey)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(apiKeyInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // ========== RETROFIT PARA USUARIOS (8080) ==========
    private val retrofitUsuarios by lazy {
        Retrofit.Builder()
            .baseUrl(USUARIOS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ========== RETROFIT PARA PRODUCTOS (8081) ==========
    private val retrofitProductos by lazy {
        Retrofit.Builder()
            .baseUrl(PRODUCTOS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ========== API SERVICES ==========
    val usuariosService: UsuariosApiService by lazy {
        retrofitUsuarios.create(UsuariosApiService::class.java)
    }

    val productosService: ProductosApiService by lazy {
        retrofitProductos.create(ProductosApiService::class.java)
    }
}
