/*-
 * #%L
 * matchmaking.jvm-client
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
