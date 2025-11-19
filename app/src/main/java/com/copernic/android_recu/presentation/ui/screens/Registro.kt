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

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        RecuHeader("Registrarse")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPass,
                onValueChange = { confirmPass = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    errorMessage = null

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

                    isLoading = true

                    scope.launch {
                        // 🔥 CORREGIDO — ahora usa registrarUsuario()
                        val result = firebaseService.registrarUsuario(username, email, password)
                        isLoading = false

                        if (result.isSuccess) {

                            navController.navigate(AppScreens.Login.route) {
                                popUpTo(AppScreens.Registro.route) { inclusive = true }
                            }

                        } else {

                            val msg = result.exceptionOrNull()?.message ?: "Error"

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

                if (isLoading) {
                    CircularProgressIndicator(
                        color = FootballWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Registrarse", color = FootballWhite)
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        RecuFooterPreLogin(navController)
    }
}
