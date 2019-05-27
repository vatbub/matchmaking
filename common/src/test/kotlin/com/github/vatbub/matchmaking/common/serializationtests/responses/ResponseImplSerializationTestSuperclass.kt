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

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ResponseImplSerializationTestSuperclass<T : ResponseImpl>(clazz: Class<T>) : ServerInteractionSerializationTestSuperclass<T>(clazz) {
    override fun newObjectUnderTest() = newObjectUnderTest(defaultConnectionId)
    abstract fun newObjectUnderTest(connectionId: String?, responseTo: String? = null): T
    fun newObjectUnderTest(connectionId: String?, httpStatusCode: Int? = null, responseTo: String? = null): T {
        val result = newObjectUnderTest(connectionId, responseTo)
        if (httpStatusCode != null)
            result.httpStatusCode = httpStatusCode
        return result
    }

    @Test
    fun connectionIdNotEqualTest() {
        if (skipConnectionIdAndPasswordEqualityTests) return
        val response1 = newObjectUnderTest()
        val response2 = newObjectUnderTest(TestUtils.getRandomHexString(response1.connectionId), null, response1.responseTo)
        Assertions.assertNotEquals(response1, response2)
    }

    @Test
    fun httpStatusCodeNotEqualTest() {
        if (skipConnectionIdAndPasswordEqualityTests) return
        val response1 = newObjectUnderTest()
        val response2 = newObjectUnderTest(response1.connectionId, response1.httpStatusCode + 1)
        Assertions.assertNotEquals(response1, response2)
    }
}

class ResponseImplSerializationTest : ResponseImplSerializationTestSuperclass<ResponseImpl>(ResponseImpl::class.java) {
    override fun newObjectUnderTest(connectionId: String?, responseTo: String?) = ResponseImpl(connectionId, Request::class.qualifiedName!!, responseTo)

    override fun newObjectUnderTest() = newObjectUnderTest(defaultConnectionId, null, TestUtils.getRandomHexString())

    @Test
    override fun notEqualsTest() {
        val response1 = newObjectUnderTest()
        val response2 = ResponseImpl(response1.connectionId, TestUtils.getRandomHexString(response1.className), response1.responseTo)
        Assertions.assertNotEquals(response1, response2)
    }
}
