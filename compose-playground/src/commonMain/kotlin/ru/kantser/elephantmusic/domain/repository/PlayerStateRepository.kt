package ru.kantser.elephantmusic.domain.repository

import ru.kantser.elephantmusic.domain.model.PlayerState

interface PlayerStateRepository {
    fun load(): PlayerState?
    fun save(state: PlayerState)
}
