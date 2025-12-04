package com.copernic.android_recu.presentation.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import kotlinx.coroutines.launch

class AdminViewModel(private val firebaseService: FirebaseService) : ViewModel() {

    // 🔵 CONTROL DE VISIBILIDAD DE POPUPS
    var showPopupLiga by mutableStateOf(false)
    var showPopupEquipo by mutableStateOf(false)
    var showPopupListaLigas by mutableStateOf(false)
    var showPopupListaEquipos by mutableStateOf(false)

    // 📦 LISTAS DE DATOS
    var ligas by mutableStateOf<List<Liga>>(emptyList())
    var equipos by mutableStateOf<List<Equipo>>(emptyList())

    // ✏️ OBJETOS EN EDICIÓN
    var ligaEdit: Liga? by mutableStateOf(null)
    var equipoEdit: Equipo? by mutableStateOf(null)

    // 🗑️ OBJETOS A ELIMINAR (CONFIRMACIÓN)
    var ligaAEliminar: Liga? by mutableStateOf(null)
    var equipoAEliminar: Equipo? by mutableStateOf(null)

    // 🔄 CARGA INICIAL DE DATOS AL CREAR EL VIEWMODEL
    init {
        cargarDatos()
    }

    // 🔽 CARGA LIGAS Y EQUIPOS DESDE FIREBASE
    fun cargarDatos() {
        viewModelScope.launch {
            ligas = firebaseService.obtenerLigas()
            equipos = firebaseService.obtenerEquipos()
        }
    }

    // ➕ ABRIR POPUP PARA AÑADIR LIGA
    fun openAddLiga() {
        ligaEdit = null
        showPopupLiga = true
    }

    // ➕ ABRIR POPUP PARA AÑADIR EQUIPO
    fun openAddEquipo() {
        equipoEdit = null
        showPopupEquipo = true
    }

    // ❌ CERRAR POPUPS
    fun closeLigaPopup() { showPopupLiga = false }
    fun closeEquipoPopup() { showPopupEquipo = false }

    // 💾 GUARDAR LIGA (NUEVA O EDITADA)
    fun guardarLiga(liga: Liga) {
        viewModelScope.launch {
            if (liga.id.isBlank()) firebaseService.addLiga(liga)     // Crear
            else firebaseService.updateLiga(liga)                   // Editar

            cargarDatos()
            showPopupLiga = false
        }
    }

    // 💾 GUARDAR EQUIPO (NUEVO O EDITADO)
    fun guardarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            if (equipo.id.isBlank()) firebaseService.addEquipo(equipo)   // Crear
            else firebaseService.updateEquipo(equipo)                   // Editar

            cargarDatos()
            showPopupEquipo = false
        }
    }

    // ✏️ EDITAR LIGA
    fun editLiga(liga: Liga) {
        ligaEdit = liga
        showPopupLiga = true
    }

    // ✏️ EDITAR EQUIPO
    fun editEquipo(equipo: Equipo) {
        equipoEdit = equipo
        showPopupEquipo = true
    }

    // ⚠️ CONFIRMAR ELIMINACIÓN DE LIGA
    fun confirmarEliminarLiga(liga: Liga) {
        ligaAEliminar = liga
    }

    // ⚠️ CONFIRMAR ELIMINACIÓN DE EQUIPO
    fun confirmarEliminarEquipo(equipo: Equipo) {
        equipoAEliminar = equipo
    }

    // 🗑️ ELIMINAR LIGA DEFINITIVAMENTE
    fun eliminarLiga() {
        viewModelScope.launch {
            ligaAEliminar?.let { firebaseService.deleteLiga(it.id) }
            ligaAEliminar = null
            cargarDatos()
        }
    }

    // 🗑️ ELIMINAR EQUIPO DEFINITIVAMENTE
    fun eliminarEquipo() {
        viewModelScope.launch {
            equipoAEliminar?.let { firebaseService.deleteEquipo(it.id) }
            equipoAEliminar = null
            cargarDatos()
        }
    }
}
