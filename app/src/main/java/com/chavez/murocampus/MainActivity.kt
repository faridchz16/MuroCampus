package com.chavez.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chavez.murocampus.data.PostRepositoryConCache
import com.chavez.murocampus.data.PostRepositoryRemote
import com.chavez.murocampus.data.local.MuroDatabase
import com.chavez.murocampus.data.remote.RetrofitClient
import com.chavez.murocampus.ui.PantallaCrear
import com.chavez.murocampus.ui.PantallaDetalle
import com.chavez.murocampus.ui.PantallaEditar
import com.chavez.murocampus.ui.PantallaLista
import com.chavez.murocampus.ui.theme.MuroCampusTheme
import com.chavez.murocampus.viewmodel.PostsViewModel
import com.chavez.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {

    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(
            PostRepositoryConCache(
                remoto = PostRepositoryRemote(RetrofitClient.api),
                dao = MuroDatabase.obtener(applicationContext).postDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MuroCampusTheme {
                MuroCampusApp(vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuroCampusApp(vm: PostsViewModel) {
    var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
    var mostrandoCrear by rememberSaveable { mutableStateOf(false) }
    var editandoId by rememberSaveable { mutableStateOf<Int?>(null) }
    var editandoTitulo by rememberSaveable { mutableStateOf<String?>(null) }

    val enLista = idSeleccionado == null && !mostrandoCrear && editandoId == null

    BackHandler(enabled = !enLista) {
        if (editandoId != null) {
            editandoId = null
            editandoTitulo = null
        } else {
            idSeleccionado = null
            mostrandoCrear = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("MuroCampus") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (enLista) {
                FloatingActionButton(
                    onClick = {
                        vm.reiniciarCreacion()
                        mostrandoCrear = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        val id = idSeleccionado
        val eId = editandoId
        val eTitulo = editandoTitulo

        when {
            mostrandoCrear -> PantallaCrear(
                vm = vm,
                modifier = Modifier.padding(innerPadding)
            )
            eId != null && eTitulo != null -> PantallaEditar(
                id = eId,
                tituloActual = eTitulo,
                vm = vm,
                modifier = Modifier.padding(innerPadding)
            )
            id != null -> PantallaDetalle(
                id = id,
                vm = vm,
                onBorrado = {
                    idSeleccionado = null
                    vm.reiniciarBorrado()
                },
                onEditar = { editId, editTitulo ->
                    vm.reiniciarEdicion()
                    editandoId = editId
                    editandoTitulo = editTitulo
                },
                modifier = Modifier.padding(innerPadding)
            )
            else -> PantallaLista(
                vm = vm,
                onAbrir = { nuevoId ->
                    idSeleccionado = nuevoId
                    vm.abrir(nuevoId)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}