package com.example.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.launch

@Composable
fun ScreenInicio() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido a Series App",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    val listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listaSeries.addAll(listado)
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: e.toString()
            Log.e("SERIES-LISTADO", "Error al cargar series", e)
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error al conectar con la API:",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp
            )
            Text(
                text = errorMessage ?: "",
                modifier = Modifier.padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = { reloadKey++ }
            ) {
                Text("Reintentar")
            }
        }
    } else if (listaSeries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay series registradas en la API.",
                fontSize = 18.sp
            )
        }
    } else {
        LazyColumn {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ID",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.2f)
                    )
                    Text(
                        text = "SERIE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        text = "Acción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.3f)
                    )
                }
            }

            items(listaSeries) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.2f)
                    )
                    Text(
                        text = item.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.5f)
                    )
                    IconButton(
                        onClick = {
                            navController.navigate("serieVer/${item.id}")
                            Log.e("SERIE-VER", "ID = ${item.id}")
                        },
                        modifier = Modifier.weight(0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar"
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate("serieDel/${item.id}")
                            Log.e("SERIE-DEL", "ID = ${item.id}")
                        },
                        modifier = Modifier.weight(0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Eliminar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    var id by remember { mutableIntStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var releaseDate by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    if (id != 0) {
        LaunchedEffect(id) {
            try {
                val response = servicio.selectSerie(id.toString())
                if (response.isSuccessful) {
                    response.body()?.let { serie ->
                        name = serie.name
                        releaseDate = serie.release_date
                        rating = serie.rating.toString()
                        category = serie.category
                    }
                }
            } catch (e: Exception) {
                Log.e("SERIE-EDITAR", "Error al obtener serie: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = id.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre: ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = releaseDate,
            onValueChange = { releaseDate = it },
            label = { Text("Fecha de Estreno:") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating:") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría:") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    try {
                        val ratingInt = rating.toIntOrNull() ?: 0
                        val objSerie = SerieModel(id, name, releaseDate, ratingInt, category)
                        if (id == 0) {
                            servicio.insertSerie(objSerie)
                        } else {
                            servicio.updateSerie(id.toString(), objSerie)
                        }
                    } catch (e: Exception) {
                        Log.e("SERIE-GRABAR", "Error al guardar serie: ${e.message}")
                    }
                    navController.navigate("series")
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("series")
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            try {
                                servicio.deleteSerie(id.toString())
                            } catch (e: Exception) {
                                Log.e("SERIE-ELIMINAR", "Error al eliminar serie: ${e.message}")
                            }
                            navController.navigate("series")
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("series")
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
