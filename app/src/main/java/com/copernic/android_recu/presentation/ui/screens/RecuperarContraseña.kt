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

@Composable
fun RecuperarContrasenaScreen(navController: NavController, firebaseService: FirebaseService) {
    RecuperarContrasenaBody(navController, firebaseService)
}

@Composable
fun RecuperarContrasenaBody(navController: NavController, firebaseService: FirebaseService) {

    var correo by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        RecuHeader(title = "Recuperar contraseña")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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

            Button(
                onClick = {
                    mensajeError = null
                    mensajeExito = null

                    // Validaciones
                    if (correo.isBlank()) {
                        mensajeError = "El correo no puede estar vacío"
                        return@Button
                    }
                    if (!correo.contains("@") || !correo.contains(".")) {
                        mensajeError = "El formato del correo no es válido"
                        return@Button
                    }

                    cargando = true

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
                if (cargando) {
                    CircularProgressIndicator(
                        color = FootballWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Enviar correo", color = FootballWhite)
                }
            }

            mensajeError?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = Color.Red)
            }

            mensajeExito?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = it, color = Color(0xFF2EA043)) // Verde éxito
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = { navController.navigate(AppScreens.Login.route) },
            ) {
                Text("Volver al inicio de sesión", color = FootballBlack)
            }
        }

        RecuFooterPreLogin(navController)
    }
}
