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
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.graphics.Color

// Pantalla principal del perfil
@Composable
fun MiPerfilScreen(navController: NavController, firebase: FirebaseService) {
    MiPerfilBody(navController, firebase)
}

// Contenido de la pantalla de perfil
@Composable
fun MiPerfilBody(navController: NavController, firebase: FirebaseService) {

    var username by remember { mutableStateOf("") } // Nombre de usuario
    var email by remember { mutableStateOf("") } // Email del usuario

    // Cargar datos del usuario desde Firebase
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
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Cabecera de la pantalla
        RecuHeader(title = "Mi Perfil")

        // Zona central con los datos del usuario
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 60.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Texto del nombre de usuario
            Text(
                text = "Nombre de usuario:",
                color = FootballBlack
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = username,
                color = FootballGreen,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(25.dp))

            // Texto del correo electrónico
            Text(
                text = "Correo electrónico:",
                color = FootballBlack
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = email,
                color = FootballGreen,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Botón para cerrar sesión
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(
                onClick = {
                    firebase.logout() // Cierra sesión en Firebase
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(Color.Red),
                shape = RoundedButtonShape
            ) {
                Text(text = "Cerrar sesión", color = FootballWhite)
            }
        }

        // Footer inferior después de login
        RecuFooterPostLogin(navController)
    }
}
