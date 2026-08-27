package ru.kantser.elephantmusic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val playlistName: String? = null,
    val trackFilePath: String? = null,
    val positionSeconds: Double = 0.0,
)
