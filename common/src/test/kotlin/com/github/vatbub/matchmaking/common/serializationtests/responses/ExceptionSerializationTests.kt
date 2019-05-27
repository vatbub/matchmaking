/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.serializationtests.responses

import com.github.vatbub.matchmaking.common.responses.*
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ServerInteractionExceptionSerializationTestSuperclass<T : ServerInteractionException>(clazz: Class<T>) : ResponseImplSerializationTestSuperclass<T>(clazz) {
    abstract fun newObjectUnderTest(message: String? = null, connectionId: String?, responseTo: String? = null): T
    fun newObjectUnderTest(message: String? = null, connectionId: String?, httpStatusCode: Int? = null, responseTo: String? = null): T {
        val result = newObjectUnderTest(message, connectionId, responseTo)
        if (httpStatusCode != null)
            result.httpStatusCode = httpStatusCode
        return result
    }

    override fun newObjectUnderTest(connectionId: String?, responseTo: String?) =
            newObjectUnderTest(null, connectionId, responseTo)

    @Test
    fun messageHashCodeNotEquals() {
        val instance1 = newObjectUnderTest()
        val instance2 = newObjectUnderTest(TestUtils.getRandomHexString(instance1.message), instance1.connectionId, instance1.httpStatusCode, instance1.responseTo)
        Assertions.assertNotEquals(instance1.hashCode(), instance2.hashCode())
    }

    @Test
    override fun notEqualsTest() {
        val instance1 = newObjectUnderTest()
        val instance2 = newObjectUnderTest(TestUtils.getRandomHexString(instance1.message), instance1.connectionId, instance1.httpStatusCode, instance1.responseTo)
        Assertions.assertNotEquals(instance1, instance2)
    }
}

class AuthorizationExceptionSerializationTest :
        ServerInteractionExceptionSerializationTestSuperclass<AuthorizationException>(AuthorizationException::class.java) {
    override fun newObjectUnderTest(message: String?, connectionId: String?, responseTo: String?) =
            AuthorizationException(message, connectionId, responseTo)
}

class BadRequestExceptionSerializationTest :
        ServerInteractionExceptionSerializationTestSuperclass<BadRequestException>(BadRequestException::class.java) {
    override fun newObjectUnderTest(message: String?, connectionId: String?, responseTo: String?) =
            BadRequestException(message, connectionId, responseTo)
}

class InternalServerErrorExceptionSerializationTest :
        ServerInteractionExceptionSerializationTestSuperclass<InternalServerErrorException>(InternalServerErrorException::class.java) {
    override fun newObjectUnderTest(message: String?, connectionId: String?, responseTo: String?) =
            InternalServerErrorException(message, connectionId, responseTo)
}

class NotAllowedExceptionSerializationTest :
        ServerInteractionExceptionSerializationTestSuperclass<NotAllowedException>(NotAllowedException::class.java) {
    override fun newObjectUnderTest(message: String?, connectionId: String?, responseTo: String?) =
            NotAllowedException(message, connectionId, responseTo)
}

class UnknownConnectionIdExceptionSerializationTest :
        ServerInteractionExceptionSerializationTestSuperclass<UnknownConnectionIdException>(UnknownConnectionIdException::class.java) {
    override fun newObjectUnderTest(message: String?, connectionId: String?, responseTo: String?) =
            UnknownConnectionIdException(message, connectionId, responseTo)
}
