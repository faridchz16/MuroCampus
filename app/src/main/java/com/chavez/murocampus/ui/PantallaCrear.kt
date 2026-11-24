package com.chavez.murocampus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chavez.murocampus.viewmodel.PostsViewModel
import com.chavez.murocampus.viewmodel.UiState

private val VerdeFondo = Color(0xFFE8F5E9)
private val VerdeTexto = Color(0xFF1B5E20)

@Composable
fun PantallaCrear(
    vm: PostsViewModel,
    modifier: Modifier = Modifier
) {
    val estado by vm.creacion.collectAsStateWithLifecycle()
    var titulo by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(estado) {
        if (estado is UiState.Exito) {
            titulo = ""
            contenido = ""
        }
    }

    val enviando = estado is UiState.Cargando
    val formularioValido = titulo.isNotBlank() && contenido.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nueva publicación",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            singleLine = true,
            enabled = !enviando,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = contenido,
            onValueChange = { contenido = it },
            label = { Text("Contenido") },
            minLines = 4,
            enabled = !enviando,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { vm.publicar(titulo, contenido) },
            enabled = formularioValido && !enviando,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Publicar")
        }

        when (val e = estado) {
            null -> Unit
            is UiState.Cargando -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Publicando…",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            is UiState.Exito -> MensajeResultado(
                texto = "Publicado con id ${e.datos.id}",
                fondo = VerdeFondo,
                colorTexto = VerdeTexto
            )
            is UiState.Error -> MensajeResultado(
                texto = e.mensaje,
                fondo = MaterialTheme.colorScheme.errorContainer,
                colorTexto = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun MensajeResultado(
    texto: String,
    fondo: Color,
    colorTexto: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = fondo,
        contentColor = colorTexto,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colorTexto)
    ) {
        Text(
            text = texto,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
    }
}