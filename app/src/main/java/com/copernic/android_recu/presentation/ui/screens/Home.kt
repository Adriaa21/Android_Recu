package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.presentation.ui.theme.*

@Composable
fun HomeScreen(navController: NavController, firebaseService: FirebaseService) {
    HomeBody(navController)
}

@Composable
fun HomeBody(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Primer botón
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(FootballGreen),
            shape = RoundedButtonShape
        ) {
            Text(text = "Opción 1", color = FootballWhite)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Segundo botón
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(FootballBlack),
            shape = RoundedButtonShape
        ) {
            Text(text = "Opción 2", color = FootballWhite)
        }
    }
}
