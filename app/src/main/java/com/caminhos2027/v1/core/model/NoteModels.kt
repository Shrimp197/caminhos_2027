package com.caminhos2027.v1.core.model

import java.time.Instant

/** A user-created note. The app never changes manually supplied titles. */
data class Note(
    val id: String,
    val type: NoteType,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val originalDateTime: Instant? = null,
    val walkId: String? = null,
    val routePosition: RoutePosition? = null
)

enum class NoteType {
    GENERAL,
    WALKING
}
