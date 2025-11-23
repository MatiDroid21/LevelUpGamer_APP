package ui.viewmodel

import data.model.UsuarioDTO

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val usuario: UsuarioDTO) : AuthState()
    data class Error(val message: String) : AuthState()
}
