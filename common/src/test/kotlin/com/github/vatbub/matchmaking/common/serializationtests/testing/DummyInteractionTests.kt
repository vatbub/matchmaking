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
package com.github.vatbub.matchmaking.common.serializationtests.testing

import com.github.vatbub.matchmaking.common.serializationtests.requests.RequestSerializationTestSuperclass
import com.github.vatbub.matchmaking.common.serializationtests.responses.ResponseImplSerializationTestSuperclass
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DummyRequestSerializationTest : RequestSerializationTestSuperclass<DummyRequest>(DummyRequest::class.java) {
    override fun newObjectUnderTest(connectionId: String, password: String, requestId: String?) =
            DummyRequest(connectionId, password, requestId)

    @Test
    override fun notEqualsTest() {
        val request1 = newObjectUnderTest()
        val request2 = DummyRequest(TestUtils.getRandomHexString(request1.connectionId), request1.password!!, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }
}

class DummyResponseSerializationTest :
        ResponseImplSerializationTestSuperclass<DummyResponse>(DummyResponse::class.java) {
    override fun newObjectUnderTest(connectionId: String?, responseTo: String?) =
            DummyResponse(connectionId, responseTo)

    @Test
    override fun notEqualsTest() {
        val response1 = newObjectUnderTest()
        val response2 = DummyResponse(TestUtils.getRandomHexString(response1.connectionId), response1.responseTo)
        Assertions.assertNotEquals(response1, response2)
    }
}
