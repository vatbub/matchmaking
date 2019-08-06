package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.responses.*
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ThrowableWrappersTest {
    private fun assertException(serverInteractionException: ServerInteractionException, exceptionWrapper: RuntimeException) {
        Assertions.assertEquals(serverInteractionException.message, exceptionWrapper.message)
    }

    @Test
    fun authorizationExceptionTest() {
        val authorizationException = AuthorizationException(TestUtils.getRandomHexString())
        val authorizationExceptionWrapper = AuthorizationExceptionWrapper(authorizationException)
        assertException(authorizationException, authorizationExceptionWrapper)
    }

    @Test
    fun badRequestExceptionTest() {
        val badRequestException = BadRequestException(TestUtils.getRandomHexString())
        val badRequestExceptionWrapper = BadRequestExceptionWrapper(badRequestException)
        assertException(badRequestException, badRequestExceptionWrapper)
    }

    @Test
    fun internalServerErrorExceptionTest() {
        val internalServerErrorException = InternalServerErrorException(TestUtils.getRandomHexString())
        val internalServerErrorExceptionWrapper = InternalServerErrorExceptionWrapper(internalServerErrorException)
        assertException(internalServerErrorException, internalServerErrorExceptionWrapper)
    }

    @Test
    fun notAllowedExceptionTest() {
        val notAllowedException = NotAllowedException(TestUtils.getRandomHexString())
        val notAllowedExceptionWrapper = NotAllowedExceptionWrapper(notAllowedException)
        assertException(notAllowedException, notAllowedExceptionWrapper)
    }

    @Test
    fun unknownConnectionIdExceptionTest() {
        val unknownConnectionIdException = UnknownConnectionIdException(TestUtils.getRandomHexString())
        val unknownConnectionIdExceptionWrapper = UnknownConnectionIdExceptionWrapper(unknownConnectionIdException)
        assertException(unknownConnectionIdException, unknownConnectionIdExceptionWrapper)
    }
}