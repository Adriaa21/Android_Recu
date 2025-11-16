package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.presentation.navigation.AppScreens
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(navController: NavController, firebaseService: FirebaseService) {
    RegistroBody(navController, firebaseService)
}

@Composable
fun RegistroBody(navController: NavController, firebaseService: FirebaseService) {

    // Campos del formulario
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    // Variable para mostrar errores debajo del botón
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Variable para mostrar el indicador de carga
    var isLoading by remember { mutableStateOf(false) }

    // Alcance para corrutinas
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Encabezado personalizado de tu aplicación
        RecuHeader("Registrarse")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Campo nombre de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo confirmar contraseña
            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de registro
            Button(
                onClick = {

                    // Se resetea el mensaje de error antes de validar
                    errorMessage = null

                    // Validación de campos
                    if (username.isBlank()) {
                        errorMessage = "El nombre de usuario no puede estar vacío"
                        return@Button
                    }
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "Formato de correo no válido"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "La contraseña debe tener mínimo 6 caracteres"
                        return@Button
                    }
                    if (password != confirmPass) {
                        errorMessage = "Las contraseñas no coinciden"
                        return@Button
                    }

                    // Indicador de carga mientras se realiza el registro
                    isLoading = true

                    // Se lanza el registro con Firebase
                    scope.launch {
                        val result = firebaseService.registerUser(username, email, password)
                        isLoading = false

                        if (result.isSuccess) {

                            // Si el registro es correcto, se navega al login
                            navController.navigate(AppScreens.Login.route) {
                                popUpTo(AppScreens.Registro.route) { inclusive = true }
                            }

                        } else {

                            // Se obtiene el mensaje de error desde Firebase
                            val msg = result.exceptionOrNull()?.message ?: "Error"

                            // Se asigna un mensaje legible según el tipo
                            errorMessage = when {
                                msg.contains("email address is already in use", true) ->
                                    "El correo ya está registrado"
                                msg.contains("username", true) ->
                                    "El nombre de usuario ya existe"
                                else -> msg
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(170.dp)
                    .height(42.dp),
                shape = RoundedButtonShape,
                colors = ButtonDefaults.buttonColors(FootballGreen)
            ) {

                // Indicador de carga mientras se realiza el registro
                if (isLoading) {
                    CircularProgressIndicator(
                        color = FootballWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse", color = FootballWhite)
                }
            }

            // Mensaje de error debajo del botón
            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        // Pie con opción de ir al login
        RecuFooterPreLogin(navController)
    }
}
