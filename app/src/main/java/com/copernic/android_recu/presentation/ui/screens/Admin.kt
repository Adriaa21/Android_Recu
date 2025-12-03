package com.copernic.android_recu.presentation.ui.screens

import android.Manifest
import android.net.Uri
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
import com.copernic.android_recu.presentation.ui.viewmodel.AdminViewModel
import com.copernic.android_recu.presentation.utils.MapaSelectorPopup
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Pantalla principal de administración
@Composable
fun AdminScreen(navController: NavController, firebaseService: FirebaseService) {
    val vm = remember { AdminViewModel(firebaseService) }
    AdminBody(navController = navController, vm = vm, firebaseService = firebaseService)
}

// Cuerpo principal de la pantalla de administración
@Composable
fun AdminBody(navController: NavController, vm: AdminViewModel, firebaseService: FirebaseService) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {
        // Header de la pantalla
        RecuHeader(title = "Panel de Administración")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para añadir liga
            Button(
                onClick = { vm.openAddLiga() },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) { Text("Añadir Liga", color = FootballWhite) }

            Spacer(modifier = Modifier.height(15.dp))

            // Botón para añadir equipo
            Button(
                onClick = { vm.openAddEquipo() },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) { Text("Añadir Equipo", color = FootballWhite) }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón para mostrar lista de ligas existentes
            Button(
                onClick = { vm.showPopupListaLigas = true },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGray),
                shape = RoundedButtonShape
            ) { Text("Ver Ligas Existentes") }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para mostrar lista de equipos existentes
            Button(
                onClick = { vm.showPopupListaEquipos = true },
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGray),
                shape = RoundedButtonShape
            ) { Text("Ver Equipos Existentes") }
        }

        // Footer con navegación
        RecuFooterPostLogin(navController)
    }

    // POPUP para añadir/editar liga
    if (vm.showPopupLiga) {
        LigaPopup(
            firebaseService = firebaseService,
            ligaExistente = vm.ligaEdit,
            onDismiss = { vm.closeLigaPopup() },
            onConfirm = { liga -> vm.guardarLiga(liga) }
        )
    }

    // POPUP para añadir/editar equipo
    if (vm.showPopupEquipo) {
        EquipoPopup(
            firebaseService = firebaseService,
            equipoExistente = vm.equipoEdit,
            onDismiss = { vm.closeEquipoPopup() },
            onConfirm = { equipo -> vm.guardarEquipo(equipo) }
        )
    }

    // POPUP lista de ligas existentes
    if (vm.showPopupListaLigas) {
        AlertDialog(
            onDismissRequest = { vm.showPopupListaLigas = false },
            title = { Text("Ligas Existentes") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(vm.ligas) { liga ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(liga.nombre, Modifier.weight(1f))
                            IconButton(onClick = {
                                vm.editLiga(liga)
                                vm.showPopupListaLigas = false
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }

                            IconButton(onClick = { vm.confirmarEliminarLiga(liga) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.showPopupListaLigas = false }) { Text("Cerrar") } }
        )

        // Confirmación de eliminación de liga
        if (vm.ligaAEliminar != null) {
            AlertDialog(
                onDismissRequest = { vm.ligaAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que deseas eliminar la liga \"${vm.ligaAEliminar!!.nombre}\"?") },
                confirmButton = { TextButton(onClick = { vm.eliminarLiga() }) { Text("Eliminar") } },
                dismissButton = { TextButton(onClick = { vm.ligaAEliminar = null }) { Text("Cancelar") } }
            )
        }
    }

    // POPUP lista de equipos existentes
    if (vm.showPopupListaEquipos) {
        AlertDialog(
            onDismissRequest = { vm.showPopupListaEquipos = false },
            title = { Text("Equipos Existentes") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(vm.equipos) { equipo ->
                        val ligaNombre = vm.ligas.find { it.id == equipo.ligaId }?.nombre ?: "Liga desconocida"
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${equipo.nombre} - $ligaNombre", Modifier.weight(1f))

                            IconButton(onClick = {
                                vm.editEquipo(equipo)
                                vm.showPopupListaEquipos = false
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }

                            IconButton(onClick = { vm.confirmarEliminarEquipo(equipo) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { vm.showPopupListaEquipos = false }) { Text("Cerrar") } }
        )

        // Confirmación de eliminación de equipo
        if (vm.equipoAEliminar != null) {
            AlertDialog(
                onDismissRequest = { vm.equipoAEliminar = null },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Seguro que deseas eliminar el equipo \"${vm.equipoAEliminar!!.nombre}\"?") },
                confirmButton = { TextButton(onClick = { vm.eliminarEquipo() }) { Text("Eliminar") } },
                dismissButton = { TextButton(onClick = { vm.equipoAEliminar = null }) { Text("Cancelar") } }
            )
        }
    }
}

// POPUP para añadir/editar liga
@Composable
fun LigaPopup(
    firebaseService: FirebaseService,
    ligaExistente: Liga? = null,
    onDismiss: () -> Unit,
    onConfirm: (Liga) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf(ligaExistente?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(ligaExistente?.descripcion ?: "") }
    var imagenUri by rememberSaveable { mutableStateOf(ligaExistente?.imagen) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Launchers para permisos y selección de imagen
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val selectorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { imagenUri = it.toString() } }

    // Verificar permisos para galería
    fun hasPermission(): Boolean {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }
    fun openGallery() {
        if (hasPermission()) selectorGaleria.launch("image/*")
        else permissionRequest.launch(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    // Popup de liga
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ligaExistente != null) "Editar Liga" else "Nueva Liga") },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Button(onClick = { openGallery() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(FootballGreen), shape = RoundedButtonShape) {
                    Text("Seleccionar imagen", color = FootballWhite)
                }
                if (!imagenUri.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text("Imagen seleccionada ✔") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri.isNullOrBlank()) return@Button

                    isLoading = true
                    scope.launch {
                        val imagenUrl = if (imagenUri!!.startsWith("content://") || imagenUri!!.startsWith("file://")) {
                            firebaseService.subirImagenAStorage(Uri.parse(imagenUri), "ligas")
                        } else imagenUri!!

                        val finalLiga = ligaExistente?.copy(nombre = nombre, descripcion = descripcion, imagen = imagenUrl)
                            ?: Liga(id = "", nombre = nombre, descripcion = descripcion, imagen = imagenUrl)

                        onConfirm(finalLiga)
                        isLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(FootballGreen),
                enabled = !isLoading
            ) { Text(if (isLoading) "Subiendo..." else "Guardar", color = FootballWhite) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// POPUP para añadir/editar equipo
@Composable
fun EquipoPopup(
    firebaseService: FirebaseService,
    equipoExistente: Equipo? = null,
    onDismiss: () -> Unit,
    onConfirm: (Equipo) -> Unit
) {
    // Datos del equipo
    var nombre by rememberSaveable { mutableStateOf(equipoExistente?.nombre ?: "") }
    var descripcion by rememberSaveable { mutableStateOf(equipoExistente?.descripcion ?: "") }
    var imagenUri by rememberSaveable { mutableStateOf(equipoExistente?.imagenUrl) }

    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) }
    var ligaSeleccionada by remember { mutableStateOf<Liga?>(null) }

    var expanded by remember { mutableStateOf(false) }

    // Ubicación del equipo
    var latitud by rememberSaveable { mutableStateOf(equipoExistente?.latitud ?: 40.4167) }
    var longitud by rememberSaveable { mutableStateOf(equipoExistente?.longitud ?: -3.70325) }
    var showMap by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar ligas disponibles
    LaunchedEffect(Unit) {
        val snap = firebaseService.db.collection("ligas").get().await()
        ligas = snap.toObjects(Liga::class.java)
        if (equipoExistente != null)
            ligaSeleccionada = ligas.find { it.id == equipoExistente.ligaId }
    }

    // Launchers para permisos y selección de imagen
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val selectorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { imagenUri = it.toString() } }

    fun hasPermission(): Boolean {
        val p = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }

    fun openGallery() {
        if (hasPermission()) selectorGaleria.launch("image/*")
        else permissionRequest.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    // Mapa para seleccionar ubicación
    if (showMap) {
        MapaSelectorPopup(
            latitudInicial = latitud,
            longitudInicial = longitud,
            onDismiss = { showMap = false },
            onSelect = { lat, lon ->
                latitud = lat
                longitud = lon
                showMap = false
            }
        )
    }

    // Popup de equipo
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (equipoExistente != null) "Editar Equipo" else "Nuevo Equipo") },
        text = {
            Column {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del equipo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = { openGallery() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(FootballGreen), shape = RoundedButtonShape) { Text("Seleccionar imagen", color = FootballWhite) }
                if (!imagenUri.isNullOrBlank()) { Spacer(Modifier.height(6.dp)); Text("✔ Imagen seleccionada") }
                Spacer(Modifier.height(12.dp))
                Text("Liga del equipo:")
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(ligaSeleccionada?.nombre ?: "Selecciona una liga") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ligas.forEach { liga -> DropdownMenuItem(text = { Text(liga.nombre) }, onClick = { ligaSeleccionada = liga; expanded = false }) }
                }
                Spacer(Modifier.height(12.dp))
                Text("Ubicación:")
                Text("Lat: $latitud   Lon: $longitud")
                Spacer(Modifier.height(6.dp))
                Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) { Text("Seleccionar ubicación en mapa") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri.isNullOrBlank() || ligaSeleccionada == null) return@Button
                    isLoading = true
                    scope.launch {
                        val imagenUrl = if (imagenUri!!.startsWith("content://") || imagenUri!!.startsWith("file://")) firebaseService.subirImagenAStorage(Uri.parse(imagenUri), "equipos") else imagenUri!!
                        val uid = firebaseService.getCurrentUser()?.uid ?: "anon"

                        val finalEquipo = equipoExistente?.copy(
                            nombre = nombre,
                            descripcion = descripcion,
                            imagenUrl = imagenUrl,
                            ligaId = ligaSeleccionada!!.id,
                            latitud = latitud,
                            longitud = longitud
                        ) ?: Equipo(
                            id = firebaseService.generateEquipoId(),
                            nombre = nombre,
                            descripcion = descripcion,
                            imagenUrl = imagenUrl,
                            ligaId = ligaSeleccionada!!.id,
                            fechaCreacion = System.currentTimeMillis(),
                            autorId = uid,
                            latitud = latitud,
                            longitud = longitud
                        )

                        onConfirm(finalEquipo)
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(FootballGreen)
            ) { Text(if (isLoading) "Subiendo..." else "Guardar", color = FootballWhite) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
