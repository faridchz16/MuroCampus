package com.chavez.murocampus.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY id LIMIT :cantidad OFFSET :inicio")
    suspend fun pagina(inicio: Int, cantidad: Int): List<PostEntity>

    @Query("SELECT * FROM posts ORDER BY id")
    suspend fun todos(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY id")
    suspend fun porUsuario(userId: Int): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun porId(id: Int): PostEntity?

    @Upsert
    suspend fun guardar(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun borrar(id: Int)

    @Query("UPDATE posts SET title = :titulo WHERE id = :id")
    suspend fun actualizarTitulo(id: Int, titulo: String)
}