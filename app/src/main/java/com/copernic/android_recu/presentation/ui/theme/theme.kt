package com.copernic.android_recu.presentation.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.copernic.android_recu.presentation.navigation.AppScreens

val FootballGreen = Color(0xFF4CAF50)
val FootballGreenDark = Color(0xFF1B5E20)
val FootballWhite = Color(0xFFFFFFFF)
val FootballBlack = Color(0xFF000000)
val FootballGray = Color(0xFFBDBDBD)

val RoundedButtonShape = RoundedCornerShape(50.dp)

@Composable
fun RecuHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FootballGreen)
            .padding(horizontal = 20.dp, vertical = 25.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = title,
            color = FootballBlack,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecuFooterPreLogin(navController: NavController) {

    val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FootballGreen)
            .padding(
                bottom = with(LocalDensity.current) { navigationBarHeight.toDp() } + 8.dp,
                top = 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate(AppScreens.Login.route) }) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Login",
                        tint = FootballBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Login", color = FootballBlack)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate(AppScreens.Registro.route) }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Registro",
                        tint = FootballBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Registrarse", color = FootballBlack)
            }
        }
    }
}

@Composable
fun RecuFooterPostLogin(navController: NavController) {

    val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(FootballGreen)
            .padding(
                bottom = with(LocalDensity.current) { navigationBarHeight.toDp() } + 8.dp,
                top = 10.dp
            ),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            // 🔵 BOTÓN 1 - Búsqueda (icono lupa)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate(AppScreens.Equipo.route) }) {
                    Icon(
                        Icons.Filled.Search,     // 🔍 LUPA
                        contentDescription = "Búsqueda",
                        tint = FootballBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Búsqueda", color = FootballBlack)
            }

            // 🔵 BOTÓN 2 - Home
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate(AppScreens.Home.route) }) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Home",
                        tint = FootballBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Home", color = FootballBlack)
            }

            // 🔵 BOTÓN 3 - Perfil
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { navController.navigate(AppScreens.Miperfil.route) }) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Perfil",
                        tint = FootballBlack,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("Perfil", color = FootballBlack)
            }
        }
    }
}
