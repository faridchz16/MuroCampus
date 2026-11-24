package com.chavez.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chavez.murocampus.model.Post
import com.chavez.murocampus.viewmodel.PostsViewModel
import com.chavez.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onAbrir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vm.lista.collectAsStateWithLifecycle()
    val filtro by vm.usuarioFiltro.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        FiltroUsuarios(
            seleccionado = filtro,
            onSeleccionar = { vm.filtrar(it) }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val e = estado) {
                is UiState.Cargando -> IndicadorCarga()
                is UiState.Error -> RecuadroError(
                    mensaje = e.mensaje,
                    onReintentar = { vm.cargar() }
                )
                is UiState.Exito -> {
                    if (e.datos.isEmpty()) {
                        SinPublicaciones()
                    } else {
                        ListaPosts(posts = e.datos, onAbrir = onAbrir)
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroUsuarios(
    seleccionado: Int?,
    onSeleccionar: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val opciones: List<Int?> = listOf(null) + (1..10).toList()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { userId ->
            FilterChip(
                selected = seleccionado == userId,
                onClick = { onSeleccionar(userId) },
                label = { Text(if (userId == null) "Todos" else "Usuario $userId") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun ListaPosts(
    posts: List<Post>,
    onAbrir: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(posts, key = { it.id }) { post ->
            TarjetaPost(
                usuarioId = post.userId,
                titulo = post.title,
                cuerpo = post.body,
                onClick = { onAbrir(post.id) }
            )
        }
    }
}

@Composable
private fun SinPublicaciones() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sin publicaciones",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}