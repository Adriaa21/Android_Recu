package com.copernic.android_recu.presentation.ui.screens

import android.Manifest
import android.os.Build
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.pm.PackageManager
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun AdminScreen(navController: NavController, firebaseService: FirebaseService) {
    AdminBody(navController, firebaseService)
}

@Composable
fun AdminBody(navController: NavController, firebaseService: FirebaseService) {
    var showPopupLiga by remember { mutableStateOf(false) }
    var showPopupEquipo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {
        RecuHeader(title = "Panel de Admin")

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { showPopupLiga = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Añadir Liga", color = FootballWhite)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showPopupEquipo = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Añadir Equipo", color = FootballWhite)
            }
        }

        RecuFooterPostLogin(navController)
    }

    if (showPopupLiga) {
        AddLigaPopup(
            onDismiss = { showPopupLiga = false },
            onConfirm = { liga ->
                scope.launch {
                    firebaseService.addLiga(liga)
                    showPopupLiga = false
                }
            }
        )
    }

    if (showPopupEquipo) {
        AddEquipoPopup(
            firebaseService = firebaseService,
            onDismiss = { showPopupEquipo = false },
            onConfirm = { equipo ->
                scope.launch {
                    firebaseService.addEquipo(equipo)
                    showPopupEquipo = false
                }
            }
        )
    }
}

@Composable
fun AddLigaPopup(onDismiss: () -> Unit, onConfirm: (Liga) -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var imagenUri by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permission launcher (single-permission)
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // nothing here: gallery launch handled separately
        }
    }

    val selectorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imagenUri = it.toString() }
    }

    fun hasReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun solicitarPermisoYAbrirGaleria() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (hasReadPermission()) {
            selectorGaleria.launch("image/*")
        } else {
            pedirPermiso.launch(permission)
            // after granted callback, user must tap again (simple flow). Alternatively you can open automatically if granted immediately.
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Liga") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la liga") },
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
                    onClick = { solicitarPermisoYAbrirGaleria() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(FootballGreen),
                    shape = RoundedButtonShape
                ) {
                    Text("Seleccionar imagen", color = FootballWhite)
                }

                Spacer(Modifier.height(8.dp))

                if (!imagenUri.isNullOrBlank()) Text("Imagen seleccionada ✔")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || descripcion.isBlank() || imagenUri.isNullOrBlank()) return@Button
                    onConfirm(Liga(id = "", nombre = nombre, descripcion = descripcion, imagen = imagenUri!!))
                },
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Guardar", color = FootballWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddEquipoPopup(
    firebaseService: FirebaseService,
    onDismiss: () -> Unit,
    onConfirm: (Equipo) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var imagenUri by rememberSaveable { mutableStateOf<String?>(null) }
    var ligaSeleccionada by remember { mutableStateOf<Liga?>(null) }
    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load ligas
    LaunchedEffect(Unit) {
        val snap = firebaseService.db.collection("ligas").get().await()
        ligas = snap.toObjects(Liga::class.java)
    }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // nothing here, user should press selector again
        }
    }

    val selectorGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { imagenUri = it.toString() }
    }

    fun hasReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun solicitarPermisoYAbrirGaleria() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (hasReadPermission()) selectorGaleria.launch("image/*")
        else pedirPermiso.launch(permission)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Equipo") },
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
                    onClick = { solicitarPermisoYAbrirGaleria() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(FootballGreen),
                    shape = RoundedButtonShape
                ) {
                    Text("Seleccionar imagen", color = FootballWhite)
                }

                Spacer(Modifier.height(8.dp))

                if (!imagenUri.isNullOrBlank()) Text("Imagen seleccionada ✔")

                Spacer(Modifier.height(16.dp))

                Text("Liga del equipo:")
                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }

                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(ligaSeleccionada?.nombre ?: "Selecciona una liga")
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ligas.forEach { liga ->
                        DropdownMenuItem(text = { Text(liga.nombre) }, onClick = {
                            ligaSeleccionada = liga
                            expanded = false
                        })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || imagenUri.isNullOrBlank() || ligaSeleccionada == null) return@Button
                    val nuevoEquipo = Equipo(
                        id = firebaseService.db.collection("equipos").document().id,
                        nombre = nombre,
                        ligaId = ligaSeleccionada!!.id,
                        imagenUrl = imagenUri!!
                    )
                    onConfirm(nuevoEquipo)
                },
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Guardar", color = FootballWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
