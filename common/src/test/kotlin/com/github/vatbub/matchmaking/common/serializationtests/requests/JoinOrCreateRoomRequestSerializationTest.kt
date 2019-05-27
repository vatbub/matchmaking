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
package com.github.vatbub.matchmaking.common.serializationtests.requests

import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest
import com.github.vatbub.matchmaking.common.requests.Operation
import com.github.vatbub.matchmaking.common.requests.Operation.*
import com.github.vatbub.matchmaking.testutils.TestUtils.getRandomHexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JoinOrCreateRoomRequestSerializationTest :
        RequestSerializationTestSuperclass<JoinOrCreateRoomRequest>(JoinOrCreateRoomRequest::class.java) {
    override fun newObjectUnderTest(connectionId: String, password: String, requestId: String?) =
            JoinOrCreateRoomRequest(
                    connectionId,
                    password,
                    JoinOrCreateRoom,
                    getRandomHexString(),
                    requestId = requestId
            )

    @Test
    override fun notEqualsTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, otherOperation(request1.operation), request1.userName, request1.whitelist, request1.blacklist, request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun userNameNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, getRandomHexString(request1.userName), request1.whitelist, request1.blacklist, request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun whitelistNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, listOf("khgvcjhg"), request1.blacklist, request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun blacklistNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, request1.blacklist, listOf("khgvcjhg"), request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun whitelistHashCodeNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, listOf("khgvcjhg"), request1.blacklist, request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun blacklistHashCodeNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, request1.blacklist, listOf("khgvcjhg"), request1.minRoomSize, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun minRoomSizeNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, request1.whitelist, request1.blacklist, request1.minRoomSize + 1, request1.maxRoomSize, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun maxRoomSizeNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = JoinOrCreateRoomRequest(request1.connectionId!!, request1.password!!, request1.operation, request1.userName, request1.whitelist, request1.blacklist, request1.minRoomSize, request1.maxRoomSize + 1, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    private fun otherOperation(operation: Operation) = when (operation) {
        JoinRoom -> CreateRoom
        CreateRoom -> JoinOrCreateRoom
        JoinOrCreateRoom -> JoinRoom
    }
}
