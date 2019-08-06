package com.github.vatbub.matchmaking.jvmclient

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RequestIdGeneratorTest {
    @Test
    fun noDuplicateTest() {
        val usedIds = mutableListOf<String>()
        repeat(10000) {
            val newId = RequestIdGenerator.getNewId()
            Assertions.assertFalse(usedIds.contains(newId))
            usedIds.add(newId)
        }
    }
}