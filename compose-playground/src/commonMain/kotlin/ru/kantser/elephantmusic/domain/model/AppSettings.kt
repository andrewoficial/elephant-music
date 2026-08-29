package ru.kantser.elephantmusic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val language: String = "RU",
    val lastFmName: String = "Anonim",
    val lastFmToken: String = "NULL",
    val activeScrobbling: Boolean = true,
    val volume: Double = 0.8,
    val updateSourceUrl: String? = null,
    val updateToken: String? = null,
    val auddToken: String? = null,
    val acrAccessKey: String? = null,
    val acrAccessSecret: String? = null,
    val acrHost: String? = null,
)
