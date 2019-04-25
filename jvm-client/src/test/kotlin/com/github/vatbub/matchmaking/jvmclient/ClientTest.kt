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

import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ClientTest : KotlinTestSuperclass() {
    @Test
    @Disabled
    fun dummyTest() {
        val client1 = Client(listOf(EndpointConfiguration.KryoEndpointConfiguration("localhost")),
                { _, _ -> println("[Client 1] Connected users changed") },
                { _, newValue -> println("[Client 1] New game state:\n$newValue") },
                { println("[Client 1] Game started") },
                { _, _ -> println("[Client 1] New data to be sent to host.") })
        val client2 = Client(listOf(EndpointConfiguration.KryoEndpointConfiguration("localhost")),
                { _, _ -> println("[Client 2] Connected users changed") },
                { _, newValue -> println("[Client 2] New game state:\n$newValue") },
                { println("[Client 2] Game started") },
                { _, _ -> println("[Client 2] New data to be sent to host.") })

        client1.requestConnectionId()
        client2.requestConnectionId()

        Thread.sleep(1000)

        client1.joinOrCreateRoom("vatbub")
        client2.joinOrCreateRoom("heykey")

        Thread.sleep(1000)

        println("Is client 1 host?: ${client1.currentRoom?.amITheHost}")
        println("Is client 2 host?: ${client1.currentRoom?.amITheHost}")

        if (client1.currentRoom?.amITheHost == true)
            client1.startGame()
        else
            client2.startGame()
    }
}
