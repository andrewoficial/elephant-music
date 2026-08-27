package ru.kantser.elephantmusic.platform

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
