package com.chavez.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chavez.murocampus.model.Comentario
import com.chavez.murocampus.viewmodel.DetalleUi
import com.chavez.murocampus.viewmodel.PostsViewModel
import com.chavez.murocampus.viewmodel.UiState

@Composable
fun PantallaDetalle(
    id: Int,
    vm: PostsViewModel,
    onBorrado: () -> Unit,
    onEditar: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vm.detalle.collectAsStateWithLifecycle()
    val borrado by vm.borrado.collectAsStateWithLifecycle()
    var mostrarDialogo by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(borrado) {
        if (borrado is UiState.Exito) {
            onBorrado()
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que deseas eliminar esta publicación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogo = false
                        vm.borrar(id)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    when (val e = estado) {
        is UiState.Cargando -> IndicadorCarga(
            modifier = modifier,
            texto = "Cargando publicación…"
        )
        is UiState.Error -> RecuadroError(
            mensaje = e.mensaje,
            onReintentar = { vm.abrir(id) },
            modifier = modifier
        )
        is UiState.Exito -> ContenidoDetalle(
            detalle = e.datos,
            borrado = borrado,
            onBorrar = { mostrarDialogo = true },
            onEditar = onEditar,
            modifier = modifier
        )
    }
}

@Composable
private fun ContenidoDetalle(
    detalle: DetalleUi,
    borrado: UiState<Int>?,
    onBorrar: () -> Unit,
    onEditar: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EtiquetaUsuario(detalle.post.userId)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detalle.post.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = detalle.post.body,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEditar(detalle.post.id, detalle.post.title) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar título")
                }
                Button(
                    onClick = onBorrar,
                    enabled = borrado !is UiState.Cargando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    if (borrado is UiState.Cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Eliminar")
                    }
                }
            }
            if (borrado is UiState.Error) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = borrado.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Text(
                text = "Comentarios (${detalle.comentarios.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        items(detalle.comentarios, key = { it.id }) { comentario ->
            TarjetaComentario(comentario)
        }
    }
}

@Composable
private fun TarjetaComentario(comentario: Comentario) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = comentario.email,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comentario.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}