package com.chavez.murocampus.data

import com.chavez.murocampus.data.local.PostDao
import com.chavez.murocampus.data.local.toEntity
import com.chavez.murocampus.data.local.toPost
import com.chavez.murocampus.model.Comentario
import com.chavez.murocampus.model.NuevoPost
import com.chavez.murocampus.model.Post
import kotlinx.coroutines.CancellationException
import java.io.IOException

class PostRepositoryConCache(
    private val remoto: PostRepository,
    private val dao: PostDao
) : PostRepository {

    override suspend fun listar(): Result<List<Post>> {
        val resultadoRemoto = remoto.listar()
        return resultadoRemoto.fold(
            onSuccess = { posts ->
                val resultadoGuardar = ejecutarLocal { dao.guardar(posts.map { it.toEntity() }) }
                if (resultadoGuardar.isFailure) {
                    Result.failure(resultadoGuardar.exceptionOrNull()!!)
                } else {
                    resultadoRemoto
                }
            },
            onFailure = { error ->
                if (error is IOException) {
                    val resultadoLocal = ejecutarLocal { dao.todos() }
                    resultadoLocal.fold(
                        onSuccess = { entities ->
                            if (entities.isEmpty()) {
                                resultadoRemoto
                            } else {
                                Result.success(entities.map { it.toPost() })
                            }
                        },
                        onFailure = { Result.failure(it) }
                    )
                } else {
                    resultadoRemoto
                }
            }
        )
    }

    override suspend fun listarPagina(inicio: Int, cantidad: Int): Result<List<Post>> {
        val resultadoRemoto = remoto.listarPagina(inicio, cantidad)
        return resultadoRemoto.fold(
            onSuccess = { posts ->
                val resultadoGuardar = ejecutarLocal { dao.guardar(posts.map { it.toEntity() }) }
                if (resultadoGuardar.isFailure) {
                    Result.failure(resultadoGuardar.exceptionOrNull()!!)
                } else {
                    resultadoRemoto
                }
            },
            onFailure = { error ->
                if (error is IOException) {
                    val resultadoLocal = ejecutarLocal { dao.pagina(inicio, cantidad) }
                    resultadoLocal.fold(
                        onSuccess = { entities ->
                            if (entities.isEmpty()) {
                                resultadoRemoto
                            } else {
                                Result.success(entities.map { it.toPost() })
                            }
                        },
                        onFailure = { Result.failure(it) }
                    )
                } else {
                    resultadoRemoto
                }
            }
        )
    }

    override suspend fun porUsuario(userId: Int): Result<List<Post>> {
        val resultadoRemoto = remoto.porUsuario(userId)
        return resultadoRemoto.fold(
            onSuccess = { posts ->
                val resultadoGuardar = ejecutarLocal { dao.guardar(posts.map { it.toEntity() }) }
                if (resultadoGuardar.isFailure) {
                    Result.failure(resultadoGuardar.exceptionOrNull()!!)
                } else {
                    resultadoRemoto
                }
            },
            onFailure = { error ->
                if (error is IOException) {
                    val resultadoLocal = ejecutarLocal { dao.porUsuario(userId) }
                    resultadoLocal.fold(
                        onSuccess = { entities ->
                            if (entities.isEmpty()) {
                                resultadoRemoto
                            } else {
                                Result.success(entities.map { it.toPost() })
                            }
                        },
                        onFailure = { Result.failure(it) }
                    )
                } else {
                    resultadoRemoto
                }
            }
        )
    }

    override suspend fun detalle(id: Int): Result<Post> {
        val resultadoRemoto = remoto.detalle(id)
        return resultadoRemoto.fold(
            onSuccess = { post ->
                val resultadoGuardar = ejecutarLocal { dao.guardar(listOf(post.toEntity())) }
                if (resultadoGuardar.isFailure) {
                    Result.failure(resultadoGuardar.exceptionOrNull()!!)
                } else {
                    resultadoRemoto
                }
            },
            onFailure = { error ->
                if (error is IOException) {
                    val resultadoLocal = ejecutarLocal { dao.porId(id) }
                    resultadoLocal.fold(
                        onSuccess = { entity ->
                            if (entity == null) {
                                resultadoRemoto
                            } else {
                                Result.success(entity.toPost())
                            }
                        },
                        onFailure = { Result.failure(it) }
                    )
                } else {
                    resultadoRemoto
                }
            }
        )
    }

    override suspend fun comentarios(id: Int): Result<List<Comentario>> =
        remoto.comentarios(id)

    override suspend fun crear(nuevo: NuevoPost): Result<Post> =
        remoto.crear(nuevo)

    override suspend fun borrar(id: Int): Result<Unit> {
        val resultadoRemoto = remoto.borrar(id)
        if (resultadoRemoto.isSuccess) {
            val resultadoBorrar = ejecutarLocal { dao.borrar(id) }
            if (resultadoBorrar.isFailure) {
                return Result.failure(resultadoBorrar.exceptionOrNull()!!)
            }
        }
        return resultadoRemoto
    }

    override suspend fun editar(id: Int, titulo: String): Result<Post> {
        val resultadoRemoto = remoto.editar(id, titulo)
        resultadoRemoto.onSuccess { post ->
            val resultadoActualizar = ejecutarLocal { dao.actualizarTitulo(id, post.title) }
            if (resultadoActualizar.isFailure) {
                return Result.failure(resultadoActualizar.exceptionOrNull()!!)
            }
        }
        return resultadoRemoto
    }

    private inline fun <T> ejecutarLocal(bloque: () -> T): Result<T> {
        return try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}