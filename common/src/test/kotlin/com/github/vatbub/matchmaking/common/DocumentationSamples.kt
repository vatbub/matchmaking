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
package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest
import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest
import com.github.vatbub.matchmaking.common.requests.Operation
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result
import org.junit.jupiter.api.Test
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

class DocumentationSamples {
    companion object {
        const val connectionId = "79f96ee2"
        const val password = "3450e711"
        val room = Room("73065963", connectionId, listOf("heykey", "mo-mar"), listOf("leoll"))
    }

    private fun <T : Any> serializeAndPrint(objectToPrint: T) {
        println(toJson(objectToPrint, prettify = true))
    }

    @Test
    fun getConnectionIdRequest() {
        serializeAndPrint(GetConnectionIdRequest())
    }

    @Test
    fun getConnectionIdResponse() {
        serializeAndPrint(GetConnectionIdResponse(connectionId, password))
    }

    @Test
    fun joinOrCreateRoomRequest() {
        serializeAndPrint(
                JoinOrCreateRoomRequest(
                        connectionId,
                        password,
                        Operation.JoinOrCreateRoom,
                        "vatbub",
                        listOf("heykey", "mo-mar"),
                        listOf("leoll")
                )
        )
    }

    @Test
    fun joinOrCreateRoomResponse() {
        serializeAndPrint(JoinOrCreateRoomResponse(connectionId, Result.RoomCreated, room.id))
    }

    @Test
    fun getRoomDataRequest() {
        serializeAndPrint(GetRoomDataRequest(connectionId, password, room.id))
    }

    @Test
    fun getRoomDataResponse() {
        val roomCopy = room.copy()
        roomCopy.connectedUsers.add(
                User(
                        "13527189",
                        "vatbub",
                        InetAddress.getByName("192.168.5.0") as Inet4Address,
                        InetAddress.getByName("684D:1111:222:3333:4444:5555:6:77") as Inet6Address
                )
        )
        serializeAndPrint(GetRoomDataResponse(connectionId, roomCopy))
    }
}
