package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MiPerfilScreen(navController: NavController, firebase: FirebaseService) {
    MiPerfilBody(navController, firebase)
}

@Composable
fun MiPerfilBody(navController: NavController, firebase: FirebaseService) {

    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    // 🔥 Cargar datos del usuario
    LaunchedEffect(Unit) {
        val uid = firebase.getCurrentUser()?.uid
        if (uid != null) {
            try {
                val doc = firebase.db.collection("users").document(uid).get().await()
                val user = doc.toObject(com.copernic.android_recu.model.User::class.java)
                if (user != null) {
                    username = user.username
                    email = user.email
                }
            } catch (_: Exception) { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        RecuHeader(title = "Mi Perfil")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 🔵 USERNAME
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FootballGreen,
                    cursorColor = FootballGreen
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔵 EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FootballGreen,
                    cursorColor = FootballGreen
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🔵 BOTÓN GUARDAR
            Button(
                onClick = {
                    scope.launch {
                        val result = firebase.actualizarPerfil(username, email)
                        mensaje = result.fold(
                            onSuccess = { "Perfil actualizado correctamente." },
                            onFailure = { it.message ?: "Error desconocido" }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text("Guardar cambios", color = FootballWhite)
            }

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = mensaje)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 🔴 BOTÓN CERRAR SESIÓN
            Button(
                onClick = {
                    firebase.logout()
                    navController.navigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballBlack),
                shape = RoundedButtonShape
            ) {
                Text(text = "Cerrar sesión", color = FootballWhite)
            }
        }

        RecuFooterPostLogin(navController)
    }
}
