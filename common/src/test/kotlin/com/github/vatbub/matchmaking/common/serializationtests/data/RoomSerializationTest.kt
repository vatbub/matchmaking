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
package com.github.vatbub.matchmaking.common.serializationtests.data

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import java.net.Inet4Address
import java.net.Inet6Address

class RoomSerializationTest :
    SerializationTestSuperclass<Room>(Room::class.java) {
    override fun newObjectUnderTest(): Room {
        val room = Room(
            TestUtils.getRandomHexString(),
            TestUtils.defaultConnectionId,
            listOf(TestUtils.getRandomHexString(), TestUtils.getRandomHexString()),
            listOf(TestUtils.getRandomHexString(), TestUtils.getRandomHexString()),
            2,
            2
        )
        room.connectedUsers.add(
            User(
                TestUtils.getRandomHexString(),
                "vatbub",
                Inet4Address.getByName("129.187.211.162") as Inet4Address?,
                Inet6Address.getByName("2001:4ca0:2fff:11:0:0:0:25") as Inet6Address?
            )
        )
        room.dataToBeSentToTheHost.add(GameData())
        room.gameStarted = true
        return room
    }

}
