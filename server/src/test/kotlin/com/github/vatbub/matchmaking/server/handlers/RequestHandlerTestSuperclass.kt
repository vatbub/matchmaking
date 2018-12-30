package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.server.KotlinTestSuperclass
import org.junit.jupiter.api.Test

abstract class RequestHandlerTestSuperclass: KotlinTestSuperclass() {
    @Test
    abstract fun handleTest()

    @Test
    abstract fun positiveCanHandleTest()

    @Test
    abstract fun negativeCanHandleTest()
}