package com.copernic.android_recu.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.copernic.android_recu.data.firebase.FirebaseService
import com.copernic.android_recu.model.Equipo
import com.copernic.android_recu.model.Liga
import com.copernic.android_recu.presentation.ui.theme.*
import kotlinx.coroutines.tasks.await

@Composable
fun EquipoScreen(navController: NavController, firebaseService: FirebaseService) {
    EquipoBody(navController, firebaseService)
}

@Composable
fun EquipoBody(navController: NavController, firebaseService: FirebaseService) {

    var equipos by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var ligas by remember { mutableStateOf<Map<String, Liga>>(emptyMap()) }

    // 🔍 Campo de búsqueda
    var filtro by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val snapEquipos = firebaseService.db.collection("equipos").get().await()
        equipos = snapEquipos.toObjects(Equipo::class.java)

        val snapLigas = firebaseService.db.collection("ligas").get().await()
        val listaLigas = snapLigas.toObjects(Liga::class.java)
        ligas = listaLigas.associateBy { it.id }
    }

    // 🔍 Filtrado
    val equiposFiltrados = equipos.filter {
        it.nombre.contains(filtro, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FootballWhite)
    ) {
        RecuHeader(title = "Equipos")


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {

            // 🔍 BARRA DE BÚSQUEDA
            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                label = { Text("Buscar equipo por nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (equipos.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay equipos añadidos.", color = FootballBlack)
                }

            } else if (equiposFiltrados.isEmpty()) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron equipos.")
                }

            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(equiposFiltrados) { equipo ->
                        val ligaNombre = ligas[equipo.ligaId]?.nombre ?: "Liga desconocida"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            colors = CardDefaults.cardColors(FootballGray)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(equipo.imagenUrl),
                                    contentDescription = equipo.nombre,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .padding(end = 12.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column {
                                    Text(
                                        text = equipo.nombre,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = FootballWhite
                                        )
                                    )
                                    Text(
                                        text = ligaNombre,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = FootballWhite
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        RecuFooterPostLogin(navController)
    }
}
