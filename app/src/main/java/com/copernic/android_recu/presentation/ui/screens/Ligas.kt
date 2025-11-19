package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.presentation.ui.theme.FootballWhite
import com.copernic.android_recu.presentation.ui.theme.RecuFooterPostLogin
import com.copernic.android_recu.presentation.ui.theme.RecuHeader
import kotlinx.coroutines.launch

@Composable
fun LigaScreen(navController: NavController, firebaseService: FirebaseService) {
    LigaBody(navController, firebaseService)
}

@Composable
fun LigaBody(navController: NavController, firebaseService: FirebaseService) {

    val scope = rememberCoroutineScope()
    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            ligas = firebaseService.obtenerLigas()
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {
        RecuHeader(title = "Ligas")

        Column(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(16.dp)
        ) {
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (ligas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay ligas añadidas.")
                }
            } else {
                ligas.forEach { liga ->
                    LigaCard(liga)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        RecuFooterPostLogin(navController)
    }
}

@Composable
fun LigaCard(liga: Liga) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(liga.imagen),
                contentDescription = liga.nombre,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(text = liga.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = liga.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
        }
    }
}
