package com.chavez.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chavez.murocampus.data.PostRepository
import com.chavez.murocampus.model.Comentario
import com.chavez.murocampus.model.NuevoPost
import com.chavez.murocampus.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUi(
    val post: Post,
    val comentarios: List<Comentario>
)

class PostsViewModel(private val repo: PostRepository) : ViewModel() {

    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    val postsVisibles: StateFlow<UiState<List<Post>>> = combine(_lista, _textoBusqueda) { estado, query ->
        when (estado) {
            is UiState.Exito -> {
                val q = query.trim()
                if (q.isEmpty()) {
                    estado
                } else {
                    val filtrados = estado.datos.filter {
                        it.title.contains(q, ignoreCase = true)
                    }
                    UiState.Exito(filtrados)
                }
            }
            else -> estado
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Cargando
    )

    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro: StateFlow<Int?> = _usuarioFiltro.asStateFlow()

    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    private val _creacion = MutableStateFlow<UiState<Post>?>(null)
    val creacion: StateFlow<UiState<Post>?> = _creacion.asStateFlow()

    private val _borrado = MutableStateFlow<UiState<Int>?>(null)
    val borrado: StateFlow<UiState<Int>?> = _borrado.asStateFlow()

    private var trabajoLista: Job? = null
    private var trabajoDetalle: Job? = null

    init {
        cargar()
    }

    fun buscar(texto: String) {
        _textoBusqueda.value = texto
    }

    fun cargar() {
        trabajoLista?.cancel()
        _lista.value = UiState.Cargando
        val userId = _usuarioFiltro.value
        trabajoLista = viewModelScope.launch {
            val resultado = if (userId == null) {
                repo.listar()
            } else {
                repo.porUsuario(userId)
            }
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        _borrado.value = null
        trabajoDetalle?.cancel()
        _detalle.value = UiState.Cargando
        trabajoDetalle = viewModelScope.launch {
            val (resultadoPost, resultadoComentarios) = coroutineScope {
                val post = async { repo.detalle(id) }
                val comentarios = async { repo.comentarios(id) }
                post.await() to comentarios.await()
            }

            val post = resultadoPost.getOrElse {
                _detalle.value = UiState.Error(mensajeDe(it))
                return@launch
            }
            val comentarios = resultadoComentarios.getOrElse {
                _detalle.value = UiState.Error(mensajeDe(it))
                return@launch
            }

            _detalle.value = UiState.Exito(DetalleUi(post, comentarios))
        }
    }

    fun publicar(titulo: String, texto: String) {
        _creacion.value = UiState.Cargando
        viewModelScope.launch {
            val nuevo = NuevoPost(
                userId = 1,
                title = titulo.trim(),
                body = texto.trim()
            )
            repo.crear(nuevo)
                .onSuccess { _creacion.value = UiState.Exito(it) }
                .onFailure { _creacion.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun reiniciarCreacion() {
        _creacion.value = null
    }

    fun borrar(id: Int) {
        _borrado.value = UiState.Cargando
        viewModelScope.launch {
            repo.borrar(id)
                .onSuccess {
                    val estadoLista = _lista.value
                    if (estadoLista is UiState.Exito) {
                        _lista.value = UiState.Exito(estadoLista.datos.filter { post -> post.id != id })
                    }
                    _borrado.value = UiState.Exito(id)
                }
                .onFailure {
                    _borrado.value = UiState.Error(mensajeDe(it))
                }
        }
    }

    fun reiniciarBorrado() {
        _borrado.value = null
    }
}