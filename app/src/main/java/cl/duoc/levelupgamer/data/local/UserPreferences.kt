package cl.duoc.levelupgamer.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_prefs",
        Context.MODE_PRIVATE
    )

    // ========== GUARDAR USUARIO COMPLETO ==========
    fun saveUser(email: String, nombre: String, idUsuario: Long) {
        prefs.edit().apply {
            putString("email", email)
            putString("nombre", nombre)
            putLong("idUsuario", idUsuario)
            putBoolean("isLoggedIn", true)
            apply()
        }
        Log.d("UserPreferences", " Usuario guardado - Email: $email, ID: $idUsuario")
    }

    // ========== GUARDAR SOLO EMAIL (para Firebase) ==========
    fun saveEmail(email: String) {
        prefs.edit().apply {
            putString("email", email)
            putBoolean("isLoggedIn", true)
            apply()
        }
        Log.d("UserPreferences", " Email guardado: $email")
    }

    // ========== GUARDAR NOMBRE ==========
    fun saveNombre(nombre: String) {
        prefs.edit().apply {
            putString("nombre", nombre)
            apply()
        }
        Log.d("UserPreferences", " Nombre guardado: $nombre")
    }

    // AGREGAR: GUARDAR SOLO ID USUARIO
    fun saveIdUsuario(idUsuario: Long) {
        prefs.edit().apply {
            putLong("idUsuario", idUsuario)
            apply()
        }
        Log.d("UserPreferences", " ID Usuario guardado: $idUsuario")
    }

    // ========== OBTENER DATOS ==========
    fun getEmail(): String? {
        val email = prefs.getString("email", null)
        Log.d("UserPreferences", " Email obtenido: $email")
        return email
    }

    fun getNombre(): String? {
        return prefs.getString("nombre", null)
    }

    fun getIdUsuario(): Long {
        val id = prefs.getLong("idUsuario", -1)
        Log.d("UserPreferences", "ID Usuario obtenido: $id")
        return id
    }

    // ========== VERIFICAR LOGIN ==========
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    // ========== LIMPIAR USUARIO ==========
    fun clearUser() {
        prefs.edit().clear().apply()
        Log.d("UserPreferences", "🗑 Datos de usuario eliminados")
    }


}
