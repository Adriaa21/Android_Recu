package com.copernic.android_recu.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.presentation.ui.screens.*


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavigation(startDestination: String = AppScreens.Login.route) {
    val navController = rememberNavController()
    val firebaseService = FirebaseService()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppScreens.Login.route) { LoginScreen(navController, firebaseService) }
        composable(AppScreens.Registro.route) { RegistroScreen(navController, firebaseService) }
        composable(AppScreens.Home.route) { HomeScreen(navController, firebaseService) }
        composable(AppScreens.RContraseña.route) { RecuperarContrasenaScreen(navController, firebaseService) }
    }

}
