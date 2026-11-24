package com.chavez.murocampus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chavez.murocampus.model.Post

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

fun PostEntity.toPost(): Post =
    Post(
        id = id,
        userId = userId,
        title = title,
        body = body
    )

fun Post.toEntity(): PostEntity =
    PostEntity(
        id = id,
        userId = userId,
        title = title,
        body = body
    )