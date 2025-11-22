package data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_prefs",
        Context.MODE_PRIVATE
    )

    fun saveUser(email: String, nombre: String, idUsuario: Long) {
        prefs.edit().apply {
            putString("email", email)
            putString("nombre", nombre)
            putLong("idUsuario", idUsuario)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    fun getEmail(): String? = prefs.getString("email", null)

    fun getNombre(): String? = prefs.getString("nombre", null)

    fun getIdUsuario(): Long = prefs.getLong("idUsuario", -1)

    fun isLoggedIn(): Boolean = prefs.getBoolean("isLoggedIn", false)

    fun clearUser() {
        prefs.edit().clear().apply()
    }
}
