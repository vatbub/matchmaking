package com.github.vatbub.matchmaking.common

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JavaVersionTest {
    @Test
    fun assertJavaVersion() {
        val javaVersion = System.getProperty("java.version")
        println("Detected java version: $javaVersion")
        assertTrue(javaVersion.startsWith("14"))
    }
}