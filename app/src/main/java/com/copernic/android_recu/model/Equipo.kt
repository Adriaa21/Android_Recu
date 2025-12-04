package com.copernic.android_recu.model

data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val imagenUrl: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val autorId: String = "",

    val latitud: Double = 0.0,
    val longitud: Double = 0.0,

    val ligaId: String = ""
)
