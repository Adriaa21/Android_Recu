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

// Pantalla de ligas
@Composable
fun LigaScreen(navController: NavController, firebaseService: FirebaseService) {
    LigaBody(navController, firebaseService)
}

// Cuerpo principal de la pantalla de ligas
@Composable
fun LigaBody(navController: NavController, firebaseService: FirebaseService) {

    val scope = rememberCoroutineScope()
    var ligas by remember { mutableStateOf<List<Liga>>(emptyList()) } // Lista de ligas
    var cargando by remember { mutableStateOf(true) } // Estado de carga

    var filtro by remember { mutableStateOf("") } // Texto de búsqueda

    // Cargar ligas desde Firebase al iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            ligas = firebaseService.obtenerLigas()
            cargando = false
        }
    }

    // Filtrar ligas según el texto de búsqueda
    val ligasFiltradas = ligas.filter {
        it.nombre.contains(filtro, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {
        // Header de la pantalla
        RecuHeader(title = "Ligas")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {

            // Campo de búsqueda
            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar liga por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar indicador de carga
            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            // Mostrar mensaje si no hay ligas
            else if (ligasFiltradas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron ligas.")
                }
            }
            // Mostrar lista de ligas
            else {
                ligasFiltradas.forEach { liga ->
                    LigaCard(liga) // Tarjeta individual de liga
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Footer con navegación post-login
        RecuFooterPostLogin(navController)
    }
}

// Tarjeta que muestra la información de una liga
@Composable
fun LigaCard(liga: Liga) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Imagen de la liga
            Image(
                painter = rememberAsyncImagePainter(liga.imagen),
                contentDescription = liga.nombre,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y descripción de la liga
            Column {
                Text(text = liga.nombre, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = liga.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }
        }
    }
}
