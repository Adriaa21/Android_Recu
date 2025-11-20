package com.copernic.android_recu.presentation.ui.screens

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminScreen(navController: NavController, firebaseService: FirebaseService) {
    AdminBody(navController, firebaseService)
}

@Composable
fun AdminBody(navController: NavController, firebaseService: FirebaseService) {

    var showPopupLiga by remember { mutableStateOf(false) }
    var showPopupEquipo by remember { mutableStateOf(false) }

    var showPopupListaLigas by remember { mutableStateOf(false) }
    var showPopupListaEquipos by remember { mutableStateOf(false) }

    var ligaEdit by remember { mutableStateOf<Liga?>(null) }
    var equipoEdit by remember { mutableStateOf<Equipo?>(null) }

    val scope = rememberCoroutineScope()

    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) }
    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }

    var ligaAEliminar by remember { mutableStateOf<Liga?>(null) }
    var equipoAEliminar by remember { mutableStateOf<Equipo?>(null) }


    // Cargar datos
    LaunchedEffect(Unit) {
        ligas = firebaseService.obtenerLigas()
        val snap = firebaseService.db.collection("equipos").get().await()
        equipos = snap.toObjects(Equipo::class.java)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {

        RecuHeader(title = "Panel de Administración")

        // BOTONES CENTRADOS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    showPopupLiga = true
                    ligaEdit = null
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Añadir Liga", color = FootballWhite)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = {
                    showPopupEquipo = true
                    equipoEdit = null
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Añadir Equipo", color = FootballWhite)
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = { showPopupListaLigas = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGray),
                shape = RoundedButtonShape
            ) {
                Text("Ver Ligas Existentes")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showPopupListaEquipos = true },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGray),
                shape = RoundedButtonShape
            ) {
                Text("Ver Equipos Existentes")
            }
        }

        RecuFooterPostLogin(navController)
    }

    // ---------- POPUP LIGAS ----------
    if (showPopupLiga) {
        LigaPopup(
            ligaExistente = ligaEdit,
            onDismiss = { showPopupLiga = false },
            onConfirm = { liga ->
                scope.launch {
                    if (liga.id.isEmpty()) {
                        firebaseService.addLiga(liga)
                    } else {
                        firebaseService.db.collection("ligas").document(liga.id).set(liga).await()
                    }
                    ligas = firebaseService.obtenerLigas()
                    showPopupLiga = false
                }
            }
        )
    }

    // ---------- POPUP EQUIPOS ----------
    if (showPopupEquipo) {
        EquipoPopup(
            firebaseService = firebaseService,
            equipoExistente = equipoEdit,
            onDismiss = { showPopupEquipo = false },
            onConfirm = { equipo ->
                scope.launch {
                    if (equipoEdit == null) {
                        firebaseService.addEquipo(equipo)
                    } else {
                        firebaseService.db.collection("equipos").document(equipo.id).set(equipo).await()
                    }
                    val snapEquipos = firebaseService.db.collection("equipos").get().await()
                    equipos = snapEquipos.toObjects(Equipo::class.java)
                    showPopupEquipo = false
                }
            }
        )
    }

    // LISTA LIGAS
    if (showPopupListaLigas) {
        AlertDialog(
            onDismissRequest = { showPopupListaLigas = false },
            title = { Text("Ligas Existentes") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(ligas) { liga ->

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(liga.nombre, Modifier.weight(1f))

                            // ---- EDITAR ----
                            IconButton(onClick = {
                                ligaEdit = liga
                                showPopupLiga = true
                                showPopupListaLigas = false
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            // ---- NUEVO: ELIMINAR ----
                            IconButton(onClick = {
                                ligaAEliminar = liga
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }

                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPopupListaLigas = false }) {
                    Text("Cerrar")
                }
            }
        )
        // ---------- CONFIRMACIÓN ELIMINAR LIGA ----------
        if (ligaAEliminar != null) {
            AlertDialog(
                onDismissRequest = { ligaAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que deseas eliminar la liga \"${ligaAEliminar!!.nombre}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                firebaseService.db.collection("ligas")
                                    .document(ligaAEliminar!!.id)
                                    .delete().await()

                                ligas = firebaseService.obtenerLigas()

                            } catch (e: Exception) {
                                println("Error al eliminar la liga: ${e.message}")
                            }
                            ligaAEliminar = null
                        }
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ligaAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

    }

    // LISTA EQUIPOS
    if (showPopupListaEquipos) {
        AlertDialog(
            onDismissRequest = { showPopupListaEquipos = false },
            title = { Text("Equipos Existentes") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(equipos) { equipo ->

                        val ligaNombre = ligas.find { it.id == equipo.ligaId }?.nombre
                            ?: "Liga desconocida"

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text("${equipo.nombre} - $ligaNombre", Modifier.weight(1f))

                            // ---- EDITAR ----
                            IconButton(onClick = {
                                equipoEdit = equipo
                                showPopupEquipo = true
                                showPopupListaEquipos = false
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            // ---- NUEVO: ELIMINAR ----
                            IconButton(onClick = {
                                equipoAEliminar = equipo
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPopupListaEquipos = false }) {
                    Text("Cerrar")
                }
            }
        )
        // ---------- CONFIRMACIÓN ELIMINAR EQUIPO ----------
        if (equipoAEliminar != null) {
            AlertDialog(
                onDismissRequest = { equipoAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que deseas eliminar el equipo \"${equipoAEliminar!!.nombre}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                firebaseService.db.collection("equipos")
                                    .document(equipoAEliminar!!.id)
                                    .delete().await()

                                val snap = firebaseService.db.collection("equipos").get().await()
                                equipos = snap.toObjects(Equipo::class.java)

                            } catch (e: Exception) {
                                println("Error al eliminar el equipo: ${e.message}")
                            }
                            equipoAEliminar = null
                        }
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { equipoAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

    }
}

//////////////////////////////////////////////
// ---------- POPUP LIGA ----------
//////////////////////////////////////////////

@Composable
fun LigaPopup(
    ligaExistente: Liga? = null,
    onDismiss: () -> Unit,
    onConfirm: (Liga) -> Unit
) {

    var nombre by rememberSaveable { mutableStateOf(ligaExistente?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(ligaExistente?.descripcion ?: "") }
    var imagenUri by rememberSaveable { mutableStateOf(ligaExistente?.imagen) }

    val context = LocalContext.current

    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imagenUri = it.toString() } }

    fun hasPermission(): Boolean {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        return ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }

    fun openGallery() {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (hasPermission()) selectorGaleria.launch("image/*")
        else permissionRequest.launch(p)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ligaExistente != null) "Editar Liga" else "Nueva Liga") },
        text = {
            Column {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { openGallery() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(FootballGreen),
                    shape = RoundedButtonShape
                ) {
                    Text("Seleccionar imagen", color = FootballWhite)
                }

                if (!imagenUri.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Imagen seleccionada ✔")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri.isNullOrBlank()) return@Button

                    val finalLiga = ligaExistente?.copy(
                        nombre = nombre,
                        descripcion = descripcion,
                        imagen = imagenUri!!
                    ) ?: Liga(
                        id = "",
                        nombre = nombre,
                        descripcion = descripcion,
                        imagen = imagenUri!!
                    )

                    onConfirm(finalLiga)
                },
                colors = ButtonDefaults.buttonColors(FootballGreen)
            ) { Text("Guardar", color = FootballWhite) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

////////////////////////////////////////////////
// ---------- POPUP EQUIPO ----------
////////////////////////////////////////////////

@Composable
fun EquipoPopup(
    firebaseService: FirebaseService,
    equipoExistente: Equipo? = null,
    onDismiss: () -> Unit,
    onConfirm: (Equipo) -> Unit
) {

    var nombre by rememberSaveable { mutableStateOf(equipoExistente?.nombre ?: "") }
    var imagenUri by rememberSaveable { mutableStateOf(equipoExistente?.imagenUrl) }

    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) }
    var ligaSeleccionada by remember { mutableStateOf<Liga?>(null) }

    var expanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val snap = firebaseService.db.collection("ligas").get().await()
        ligas = snap.toObjects(Liga::class.java)

        if (equipoExistente != null) {
            ligaSeleccionada = ligas.find { it.id == equipoExistente.ligaId }
        }
    }

    val permissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val selectorGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imagenUri = it.toString() } }

    fun hasPermission(): Boolean {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        return ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }

    fun openGallery() {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (hasPermission()) selectorGaleria.launch("image/*")
        else permissionRequest.launch(p)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (equipoExistente != null) "Editar Equipo" else "Nuevo Equipo") },
        text = {
            Column {

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del equipo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { openGallery() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(FootballGreen),
                    shape = RoundedButtonShape
                ) {
                    Text("Seleccionar imagen", color = FootballWhite)
                }

                if (!imagenUri.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Imagen seleccionada ✔")
                }

                Spacer(Modifier.height(16.dp))

                Text("Liga del equipo:")

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(ligaSeleccionada?.nombre ?: "Selecciona una liga")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ligas.forEach { liga ->
                        DropdownMenuItem(
                            text = { Text(liga.nombre) },
                            onClick = {
                                ligaSeleccionada = liga
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || imagenUri.isNullOrBlank() || ligaSeleccionada == null)
                        return@Button

                    val finalEquipo =
                        equipoExistente?.copy(
                            nombre = nombre,
                            imagenUrl = imagenUri!!,
                            ligaId = ligaSeleccionada!!.id
                        ) ?: Equipo(
                            id = firebaseService.db.collection("equipos").document().id,
                            nombre = nombre,
                            ligaId = ligaSeleccionada!!.id,
                            imagenUrl = imagenUri!!
                        )

                    onConfirm(finalEquipo)
                },
                colors = ButtonDefaults.buttonColors(FootballGreen)
            ) { Text("Guardar", color = FootballWhite) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
