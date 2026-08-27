package ru.kantser.elephantmusic.data.repository

import ru.kantser.elephantmusic.domain.model.PlayerState
import ru.kantser.elephantmusic.domain.repository.PlayerStateRepository

class PlayerStateRepositoryImpl(private val store: JsonFileStore) : PlayerStateRepository {
    override fun load(): PlayerState? = store.read("player_state.json")

    override fun save(state: PlayerState) = store.write("player_state.json", state)
}
