package com.github.vatbub.matchmaking.testutils

import kotlin.random.Random

object TestUtils {
    val defaultConnectionId = getRandomHexString()
    val defaultPassword = getRandomHexString()

    fun getRandomHexString(): String {
        return Random.nextInt().toString(16)
    }

    fun getRandomHexString(vararg disallowedStrings: String?): String {
        var result: String
        do {
            result = getRandomHexString()
        } while (disallowedStrings.asList().contains(result))

        return result
    }
}