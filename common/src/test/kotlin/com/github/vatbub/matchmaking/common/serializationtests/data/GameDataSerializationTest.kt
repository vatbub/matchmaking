package com.github.vatbub.matchmaking.common.serializationtests.data

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass

class GameDataSerializationTest : SerializationTestSuperclass<GameData>(GameData::class.java) {
    override fun newObjectUnderTest(): GameData {
        val gameData = GameData()
        for (i in 1..5) {
            gameData["key$i"] = "value$i"
        }
        return gameData
    }
}