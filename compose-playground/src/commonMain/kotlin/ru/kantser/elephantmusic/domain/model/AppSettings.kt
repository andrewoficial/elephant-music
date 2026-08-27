package ru.kantser.elephantmusic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val language: String = "RU",
    val lastFmName: String = "Anonim",
    val lastFmToken: String = "NULL",
    val activeScrobbling: Boolean = true,
    val volume: Double = 0.8,
)
