package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.User
import com.copernic.android_recu.presentation.navigation.AppScreens
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController, firebaseService: FirebaseService) {
    HomeBody(navController, firebaseService)
}

@Composable
fun HomeBody(navController: NavController, firebaseService: FirebaseService) {

    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        val uid = firebaseService.getCurrentUser()?.uid
        if (uid != null) {
            try {
                val doc = firebaseService.db.collection("users").document(uid).get().await()
                currentUser = doc.toObject(User::class.java)
            } catch (_: Exception) { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        RecuHeader(title = "Menú Principal")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ⬅️ BOTÓN EQUIPO (SINGULAR)
            Button(
                onClick = {
                    navController.navigate(AppScreens.Equipo.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballGreen),
                shape = RoundedButtonShape
            ) {
                Text(text = "Equipo", color = FootballWhite)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate(AppScreens.Liga.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(FootballBlack),
                shape = RoundedButtonShape
            ) {
                Text(text = "Ligas", color = FootballWhite)
            }

            if (currentUser?.rol == "ADMIN") {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { navController.navigate(AppScreens.Admin.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(FootballGray),
                    shape = RoundedButtonShape
                ) {
                    Text(text = "Admin", color = FootballWhite)
                }
            }
        }

        RecuFooterPostLogin(navController)
    }
}
