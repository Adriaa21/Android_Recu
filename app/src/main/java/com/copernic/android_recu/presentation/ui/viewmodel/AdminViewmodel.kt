package com.copernic.android_recu.presentation.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import kotlinx.coroutines.launch

class AdminViewModel(private val firebaseService: FirebaseService) : ViewModel() {

    var showPopupLiga by mutableStateOf(false)
    var showPopupEquipo by mutableStateOf(false)
    var showPopupListaLigas by mutableStateOf(false)
    var showPopupListaEquipos by mutableStateOf(false)

    var ligas by mutableStateOf<List<Liga>>(emptyList())
    var equipos by mutableStateOf<List<Equipo>>(emptyList())

    var ligaEdit: Liga? by mutableStateOf(null)
    var equipoEdit: Equipo? by mutableStateOf(null)

    var ligaAEliminar: Liga? by mutableStateOf(null)
    var equipoAEliminar: Equipo? by mutableStateOf(null)

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            ligas = firebaseService.obtenerLigas()
            equipos = firebaseService.obtenerEquipos()
        }
    }

    fun openAddLiga() {
        ligaEdit = null
        showPopupLiga = true
    }

    fun openAddEquipo() {
        equipoEdit = null
        showPopupEquipo = true
    }

    fun closeLigaPopup() { showPopupLiga = false }
    fun closeEquipoPopup() { showPopupEquipo = false }

    fun guardarLiga(liga: Liga) {
        viewModelScope.launch {
            if (liga.id.isBlank()) firebaseService.addLiga(liga)
            else firebaseService.updateLiga(liga)
            cargarDatos()
            showPopupLiga = false
        }
    }

    fun guardarEquipo(equipo: Equipo) {
        viewModelScope.launch {
            if (equipo.id.isBlank()) firebaseService.addEquipo(equipo)
            else firebaseService.updateEquipo(equipo)
            cargarDatos()
            showPopupEquipo = false
        }
    }

    fun editLiga(liga: Liga) {
        ligaEdit = liga
        showPopupLiga = true
    }

    fun editEquipo(equipo: Equipo) {
        equipoEdit = equipo
        showPopupEquipo = true
    }

    fun confirmarEliminarLiga(liga: Liga) {
        ligaAEliminar = liga
    }

    fun confirmarEliminarEquipo(equipo: Equipo) {
        equipoAEliminar = equipo
    }

    fun eliminarLiga() {
        viewModelScope.launch {
            ligaAEliminar?.let { firebaseService.deleteLiga(it.id) }
            ligaAEliminar = null
            cargarDatos()
        }
    }

    fun eliminarEquipo() {
        viewModelScope.launch {
            equipoAEliminar?.let { firebaseService.deleteEquipo(it.id) }
            equipoAEliminar = null
            cargarDatos()
        }
    }
}
