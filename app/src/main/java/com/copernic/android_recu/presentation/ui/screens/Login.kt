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
import com.copernic.android_recu.presentation.ui.theme.*
import com.copernic.android_recu.presentation.navigation.AppScreens
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, firebaseService: FirebaseService) {
    LoginBody(navController, firebaseService)
}

@Composable
fun LoginBody(navController: NavController, firebaseService: FirebaseService) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        RecuHeader(title = "Iniciar Sesión")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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

            // 🔥 AÑADIMOS EL BOTÓN DE RECUPERAR CONTRASEÑA AQUÍ
            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = {
                    navController.navigate(AppScreens.RContraseña.route)
                }
            ) {
                Text(
                    text = "¿Has olvidado tu contraseña?",
                    color = FootballBlack
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    errorMessage = null

                    // Validación del correo
                    if (email.isBlank()) {
                        errorMessage = "El correo no puede estar vacío"
                        return@Button
                    }
                    if (!email.contains("@") || !email.contains(".")) {
                        errorMessage = "El formato del correo no es válido"
                        return@Button
                    }

                    // Validación de la contraseña
                    if (password.isBlank()) {
                        errorMessage = "La contraseña no puede estar vacía"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                        return@Button
                    }

                    isLoading = true

                    scope.launch {
                        val result = firebaseService.loginUser(email, password)
                        isLoading = false

                        if (result.isSuccess) {
                            navController.navigate(AppScreens.Home.route) {
                                popUpTo(AppScreens.Login.route) { inclusive = true }
                            }
                        } else {
                            val msg = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"

                            errorMessage = when {
                                msg.contains("password", true) ->
                                    "La contraseña es incorrecta"
                                msg.contains("no user", true) ||
                                        msg.contains("user record", true) ->
                                    "No existe una cuenta con este correo"
                                msg.contains("badly formatted", true) ->
                                    "El correo no tiene un formato válido"
                                else ->
                                    "Error al iniciar sesión. Comprueba tus datos."
                            }
                        }
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(40.dp),
                shape = RoundedButtonShape,
                colors = ButtonDefaults.buttonColors(FootballGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = FootballWhite,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Login", color = FootballWhite)
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
