package com.copernic.android_recu.presentation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val firebase: FirebaseService = FirebaseService()) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user

    private val _mensaje = MutableStateFlow("")
    val mensaje: StateFlow<String> get() = _mensaje

    fun cargarPerfil() {
        viewModelScope.launch {
            _user.value = firebase.obtenerPerfilUsuario()
        }
    }

    fun actualizarPerfil(username: String, email: String) {
        viewModelScope.launch {
            val result = firebase.actualizarPerfil(username, email)
            _mensaje.value = result.fold(
                onSuccess = { "Perfil actualizado correctamente." },
                onFailure = { it.message ?: "Error desconocido" }
            )
        }
    }

    fun logout() {
        firebase.logout()
    }
}
