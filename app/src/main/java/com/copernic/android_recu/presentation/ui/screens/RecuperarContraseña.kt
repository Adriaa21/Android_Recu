package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.presentation.navigation.AppScreens
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch

// Pantalla principal de recuperación de contraseña
@Composable
fun RecuperarContrasenaScreen(navController: NavController, firebaseService: FirebaseService) {
    RecuperarContrasenaBody(navController, firebaseService)
}

// Contenido de la pantalla
@Composable
fun RecuperarContrasenaBody(navController: NavController, firebaseService: FirebaseService) {

    var correo by remember { mutableStateOf("") } // Correo introducido por el usuario
    var mensajeError by remember { mutableStateOf<String?>(null) } // Mensaje de error
    var mensajeExito by remember { mutableStateOf<String?>(null) } // Mensaje de éxito
    var cargando by remember { mutableStateOf(false) } // Muestra el loading

    val scope = rememberCoroutineScope() // Scope para corrutinas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Cabecera superior
        RecuHeader(title = "Recuperar contraseña")

        // Zona central del formulario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Campo de texto para el correo
            OutlinedTextField(
                value = correo,
                onValueChange = {
                    correo = it
                    mensajeError = null
                    mensajeExito = null
                },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para enviar el correo de recuperación
            Button(
                onClick = {
                    mensajeError = null
                    mensajeExito = null

                    // Validaciones del correo
                    if (correo.isBlank()) {
                        mensajeError = "El correo no puede estar vacío"
                        return@Button
                    }
                    if (!correo.contains("@") || !correo.contains(".")) {
                        mensajeError = "El formato del correo no es válido"
                        return@Button
                    }

                    cargando = true

                    // Llamada a Firebase para enviar el correo
                    scope.launch {
                        val result = firebaseService.enviarCorreoRecuperacion(correo)
                        cargando = false

                        if (result.isSuccess) {
                            mensajeExito = "Se ha enviado un correo para restablecer la contraseña"
                        } else {
                            val msg = result.exceptionOrNull()?.message ?: ""

                            mensajeError = when {
                                msg.contains("no user", true) ||
                                        msg.contains("record", true) ->
                                    "No existe una cuenta con este correo"
                                msg.contains("badly formatted", true) ->
                                    "El correo no tiene un formato válido"
                                else ->
                                    "No se pudo enviar el correo. Inténtalo más tarde."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                // Spinner de carga o texto normal
                if (cargando) {
                    CircularProgressIndicator(
                        color = FootballWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Enviar correo", color = FootballWhite)
                }
            }

            // Mensaje de error
            mensajeError?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = Color.Red)
            }

            // Mensaje de éxito
            mensajeExito?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = Color(0xFF2EA043)) // Verde éxito
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para volver al login
            TextButton(
                onClick = { navController.navigate(AppScreens.Login.route) },
            ) {
                Text("Volver al inicio de sesión", color = FootballBlack)
            }
        }

        // Footer inferior antes de login
        RecuFooterPreLogin(navController)
    }
}
