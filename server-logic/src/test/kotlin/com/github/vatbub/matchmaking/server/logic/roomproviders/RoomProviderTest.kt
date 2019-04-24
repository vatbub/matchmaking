/*-
 * #%L
 * matchmaking.server
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
package com.github.vatbub.matchmaking.server.logic.roomproviders

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class RoomProviderTest : KotlinTestSuperclass() {
    abstract fun newInstance(): RoomProvider

    @Test
    fun negativeContainsTest() {
        Assertions.assertFalse(newInstance().containsRoom("khczufgijkln"))
    }

    @Test
    fun positiveContainsTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom("1d6aa98d")
        Assertions.assertTrue(roomProvider.containsRoom(room.id))
    }

    @Test
    fun createRoomTest() {
        val expectedRooms = listOf(
            Room("", "325f6f32", listOf("vatbub", "mo-mar"), listOf("leoll"), 2, 5),
            Room("", "22321b1b", listOf("heykey", "mylord"), listOf("leoll"), 4, 10),
            Room("", "0208e980", null, null, 3, 4),
            Room("", "29c806f4")
        )

        val roomIds = mutableListOf<String>()
        for (expectedRoom in expectedRooms) {
            val room = newInstance().createNewRoom(
                expectedRoom.hostUserConnectionId,
                expectedRoom.whitelist,
                expectedRoom.blacklist,
                expectedRoom.minRoomSize,
                expectedRoom.maxRoomSize
            )
            Assertions.assertEquals(expectedRoom.hostUserConnectionId, room.hostUserConnectionId)
            Assertions.assertEquals(expectedRoom.whitelist, room.whitelist)
            Assertions.assertEquals(expectedRoom.blacklist, room.blacklist)
            Assertions.assertEquals(expectedRoom.minRoomSize, room.minRoomSize)
            Assertions.assertEquals(expectedRoom.maxRoomSize, room.maxRoomSize)
            Assertions.assertFalse(roomIds.contains(room.id))
            roomIds.add(room.id)
        }
    }

    @Test
    fun getRoomTest() {
        val roomProvider = newInstance()
        val createdRoom1 = roomProvider.createNewRoom("29c806f4")
        val createdRoom2 = roomProvider.createNewRoom("325f6f32")
        val retrievedRoom1 = roomProvider[createdRoom1.id]
        val retrievedRoom2 = roomProvider[createdRoom2.id]
        Assertions.assertEquals(createdRoom1, retrievedRoom1)
        Assertions.assertEquals(createdRoom2, retrievedRoom2)
        Assertions.assertNotEquals(createdRoom1, retrievedRoom2)
        Assertions.assertNotEquals(createdRoom2, retrievedRoom1)
    }

    @Test
    fun createRoomImmutabilityTest() {
        val roomProvider = newInstance()
        val createdRoom = roomProvider.createNewRoom("29c806f4")
        val retrievedRoom1 = roomProvider[createdRoom.id]!!
        retrievedRoom1.gameStarted = true
        retrievedRoom1.dataToBeSentToTheHost.add(GameData(TestUtils.getRandomHexString(createdRoom.hostUserConnectionId)))
        retrievedRoom1.gameState["key"] = "value"
        retrievedRoom1.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))

        Assertions.assertNotEquals(retrievedRoom1.gameStarted, createdRoom.gameStarted)
        Assertions.assertNotEquals(retrievedRoom1.dataToBeSentToTheHost, createdRoom.dataToBeSentToTheHost)
        Assertions.assertNotEquals(retrievedRoom1.gameState, createdRoom.gameState)
        Assertions.assertNotEquals(retrievedRoom1.connectedUsers, createdRoom.connectedUsers)
    }

    @Test
    fun getRoomImmutabilityTest() {
        val roomProvider = newInstance()
        val createdRoom = roomProvider.createNewRoom("29c806f4")
        val retrievedRoom1 = roomProvider[createdRoom.id]!!
        retrievedRoom1.gameStarted = true
        retrievedRoom1.dataToBeSentToTheHost.add(GameData(TestUtils.getRandomHexString(createdRoom.hostUserConnectionId)))
        retrievedRoom1.gameState["key"] = "value"
        retrievedRoom1.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))

        val retrievedRoom2 = roomProvider[createdRoom.id]!!

        Assertions.assertNotEquals(retrievedRoom1.gameStarted, retrievedRoom2.gameStarted)
        Assertions.assertNotEquals(retrievedRoom1.dataToBeSentToTheHost, retrievedRoom2.dataToBeSentToTheHost)
        Assertions.assertNotEquals(retrievedRoom1.gameState, retrievedRoom2.gameState)
        Assertions.assertNotEquals(retrievedRoom1.connectedUsers, retrievedRoom2.connectedUsers)
    }

    @Test
    fun getRoomsByIdTest() {
        val roomProvider = newInstance()
        val roomsToGet = mutableListOf("21e8b855", "36f1d82b")
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        for (hostConnectionId in hostConnectionIds) {
            var createdRoom: Room? = null
            do {
                if (createdRoom != null)
                    roomProvider.deleteRoom(createdRoom.id)
                createdRoom = roomProvider.createNewRoom(hostConnectionId)
            } while (createdRoom == null || roomsToGet.contains(createdRoom.id))

            roomsToGet.add(createdRoom.id)
        }

        val retrievedRooms = roomProvider.getRoomsById(roomsToGet)
        Assertions.assertEquals(hostConnectionIds.size, retrievedRooms.size)

        for (room in retrievedRooms) {
            Assertions.assertTrue(hostConnectionIds.contains(room.hostUserConnectionId))
        }
    }

    @Test
    fun getRoomsByIdImmutabilityTest() {
        val roomProvider = newInstance()
        val roomsToGet = mutableListOf("21e8b855", "36f1d82b")
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        for (hostConnectionId in hostConnectionIds) {
            var createdRoom: Room? = null
            do {
                if (createdRoom != null)
                    roomProvider.deleteRoom(createdRoom.id)
                createdRoom = roomProvider.createNewRoom(hostConnectionId)
            } while (createdRoom == null || roomsToGet.contains(createdRoom.id))

            roomsToGet.add(createdRoom.id)
        }

        val retrievedRooms = roomProvider.getRoomsById(roomsToGet)
        Assertions.assertEquals(hostConnectionIds.size, retrievedRooms.size)

        for (room in retrievedRooms) {
            room.gameStarted = true
            room.dataToBeSentToTheHost.add(GameData(TestUtils.getRandomHexString(room.hostUserConnectionId)))
            room.gameState["key"] = "value"
            room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))

            val retrievedRoom2 = roomProvider[room.id]!!
            Assertions.assertNotEquals(room.gameStarted, retrievedRoom2.gameStarted)
            Assertions.assertNotEquals(room.dataToBeSentToTheHost, retrievedRoom2.dataToBeSentToTheHost)
            Assertions.assertNotEquals(room.gameState, retrievedRoom2.gameState)
            Assertions.assertNotEquals(room.connectedUsers, retrievedRoom2.connectedUsers)
        }
    }

    @Test
    fun deleteRoomTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom("1ffbec47")
        Assertions.assertTrue(roomProvider.containsRoom(room.id))
        Assertions.assertNotNull(roomProvider[room.id])

        Assertions.assertEquals(room, roomProvider.deleteRoom(room.id))

        Assertions.assertFalse(roomProvider.containsRoom(room.id))
        Assertions.assertNull(roomProvider[room.id])
    }

    @Test
    fun deleteRoomsTest() {
        val roomProvider = newInstance()
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val rooms = mutableListOf<Room>()
        val roomIds = mutableListOf<String>()

        for (hostConnectionId in hostConnectionIds) {
            val room = roomProvider.createNewRoom(hostConnectionId)
            rooms.add(room)
            roomIds.add(room.id)
        }

        for (room in rooms) {
            Assertions.assertTrue(roomProvider.containsRoom(room.id))
            Assertions.assertNotNull(roomProvider[room.id])
        }

        val deletedRooms = roomProvider.deleteRooms(*roomIds.toTypedArray())

        Assertions.assertEquals(rooms, deletedRooms)

        for (room in rooms) {
            Assertions.assertFalse(roomProvider.containsRoom(room.id))
            Assertions.assertNull(roomProvider[room.id])
        }
    }

    @Test
    fun deleteNonExistingRoomTest() {
        val roomProvider = newInstance()
        Assertions.assertNull(roomProvider.deleteRoom(TestUtils.getRandomHexString()))
    }

    @Test
    fun deleteNonExistingRoomsTest() {
        val roomProvider = newInstance()

        val roomIds = mutableListOf<String>()

        for (i in 0 until 5) {
            roomIds.add(TestUtils.getRandomHexString(*roomIds.toTypedArray()))
        }

        val deletedRooms = roomProvider.deleteRooms(*roomIds.toTypedArray())

        Assertions.assertEquals(0, deletedRooms.size)
    }

    @Test
    fun clearRoomsTest() {
        val roomProvider = newInstance()
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )

        for (hostConnectionId in hostConnectionIds) {
            roomProvider.createNewRoom(hostConnectionId)
        }

        Assertions.assertEquals(hostConnectionIds.size, roomProvider.getAllRooms().size)

        roomProvider.clearRooms()

        Assertions.assertEquals(0, roomProvider.getAllRooms().size)
    }

    @Test
    fun getAllRoomsTest() {
        val roomProvider = newInstance()
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val createdRooms = List(hostConnectionIds.size) { roomProvider.createNewRoom(hostConnectionIds[it]) }

        val allRooms = roomProvider.getAllRooms()

        Assertions.assertEquals(createdRooms.size, allRooms.size)

        for (room in allRooms) {
            Assertions.assertTrue(createdRooms.contains(room))
        }
    }

    @Test
    fun atomicityOfTransactionsAbortTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.connectedUsers.add(User(TestUtils.getRandomHexString(), "vatbub"))
        transaction.room.gameStarted = true
        transaction.room.dataToBeSentToTheHost.add(GameData(TestUtils.getRandomHexString(TestUtils.defaultConnectionId)))

        val newGameState = GameData(transaction.room.hostUserConnectionId)
        newGameState["should_not_appear_key"] = "should_not_appear_value"
        transaction.room.gameState.replaceContents(newGameState)

        transaction.abort()

        val roomAfterTransaction = roomProvider[room.id]

        Assertions.assertEquals(room, roomAfterTransaction)
    }

    @Test
    fun atomicityOfTransactionsCommitTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.connectedUsers.add(User(TestUtils.getRandomHexString(), "vatbub"))
        transaction.room.gameStarted = true
        transaction.room.dataToBeSentToTheHost.add(GameData(TestUtils.getRandomHexString(TestUtils.defaultConnectionId)))
        val newGameState = GameData(transaction.room.hostUserConnectionId)
        newGameState["some_key"] = "hello"
        transaction.room.gameState.replaceContents(newGameState)

        transaction.commit()

        val roomAfterTransaction = roomProvider[room.id]!!

        Assertions.assertEquals(1, roomAfterTransaction.connectedUsers.size)
        Assertions.assertTrue(roomAfterTransaction.gameStarted)
        Assertions.assertEquals(1, roomAfterTransaction.dataToBeSentToTheHost.size)
        Assertions.assertEquals(newGameState, roomAfterTransaction.gameState)
    }

    @Test
    fun isolationOfParallelTransactionsTest() {
        val roomProvider = newInstance()
        if (!roomProvider.supportsConcurrentTransactionsOnSameRoom)
            return

        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction1 = roomProvider.beginTransactionWithRoom(room.id)!!
        val transaction2 = roomProvider.beginTransactionWithRoom(room.id)!!

        val userConnectionId = TestUtils.getRandomHexString()
        transaction1.room.connectedUsers.add(User(userConnectionId, "vatbub"))
        transaction2.room.dataToBeSentToTheHost.add(GameData(userConnectionId))

        Assertions.assertEquals(0, transaction1.room.dataToBeSentToTheHost.size)
        Assertions.assertEquals(0, transaction2.room.connectedUsers.size)

        transaction1.commit()
        transaction2.commit()

        val roomAfterTransactions = roomProvider[room.id]!!

        Assertions.assertEquals(1, roomAfterTransactions.connectedUsers.size)
        Assertions.assertEquals(1, roomAfterTransactions.dataToBeSentToTheHost.size)
    }

    @Test
    fun schedulingOfParallelTransactionsTest() {
        val roomProvider = newInstance()
        if (roomProvider.supportsConcurrentTransactionsOnSameRoom)
            return

        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction1 = roomProvider.beginTransactionWithRoom(room.id)!!
        var result = false

        val transaction2Thread = Thread {
            println("Thread 2 started")
            roomProvider.beginTransactionWithRoom(room.id)
            println("transaction 2 acquired")
            result = transaction1.finalized
            println("transaction 1 finalized: $result")
        }
        transaction2Thread.start()

        Thread.sleep(100)
        transaction1.abort()
        transaction2Thread.join()
        Assertions.assertTrue(result)
    }

    @Test
    fun beginTransactionTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val roomTransaction = roomProvider.beginTransactionWithRoom(room.id)
        Assert.assertNotNull(roomTransaction)
        roomTransaction!!
        Assertions.assertEquals(room.id, roomTransaction.room.id)
        roomTransaction.abort()
    }

    @Test
    fun beginTransactionForAllRoomsTest() {
        val roomProvider = newInstance()
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val createdRooms = List(hostConnectionIds.size) { roomProvider.createNewRoom(hostConnectionIds[it]) }

        var callCount = 0
        roomProvider.beginTransactionForAllRooms { roomTransaction ->
            callCount++
            Assertions.assertTrue(createdRooms.contains(roomTransaction.room.toRoom()))
            roomTransaction.abort()
        }

        Assertions.assertEquals(createdRooms.size, callCount)
    }

    @Test
    fun beginTransactionForRoomsWithFilterTest() {
        val roomProvider = newInstance()
        val targetConnectionId = "250b7528"
        val hostConnectionIds = listOf(
            targetConnectionId,
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )

        for (id in hostConnectionIds)
            roomProvider.createNewRoom(id)

        var callCount = 0
        roomProvider.beginTransactionsForRoomsWithFilter(
            { room -> room.hostUserConnectionId == targetConnectionId },
            { roomTransaction ->
                callCount++
                Assertions.assertEquals(targetConnectionId, roomTransaction.room.hostUserConnectionId)
                roomTransaction.abort()
            })

        Assertions.assertEquals(1, callCount)
    }

    @Test
    fun beginTransactionRoomNotFoundTest() {
        val roomProvider = newInstance()
        val roomTransaction = roomProvider.beginTransactionWithRoom(TestUtils.getRandomHexString())
        Assert.assertNull(roomTransaction)
    }

    @Test
    fun beginTransactionsWithRoomsVarargTest() {
        val roomProvider = newInstance()
        val createdRoomIds = Array(5) { roomProvider.createNewRoom(TestUtils.defaultConnectionId).id }

        var callCount = 0
        roomProvider.beginTransactionsWithRooms(ids = *createdRoomIds, onTransactionAvailable = { transaction ->
            callCount++
            Assertions.assertTrue(createdRoomIds.contains(transaction.room.id))
            transaction.abort()
        })

        Assertions.assertEquals(createdRoomIds.size, callCount)
    }

    @Test
    fun beginTransactionsWithRoomsListTest() {
        val roomProvider = newInstance()
        val createdRoomIds = List(5) { roomProvider.createNewRoom(TestUtils.defaultConnectionId).id }

        var callCount = 0
        roomProvider.beginTransactionsWithRooms(ids = createdRoomIds, onTransactionAvailable = { transaction ->
            callCount++
            Assertions.assertTrue(createdRoomIds.contains(transaction.room.id))
            transaction.abort()
        })

        Assertions.assertEquals(createdRoomIds.size, callCount)
    }

    @Test
    fun beginTransactionsWithRoomsVarargUnknownIdTest() {
        val roomProvider = newInstance()
        val createdRoomIds = MutableList(5) { roomProvider.createNewRoom(TestUtils.defaultConnectionId).id }
        createdRoomIds.add(TestUtils.getRandomHexString(*createdRoomIds.toTypedArray()))

        var callCount = 0
        roomProvider.beginTransactionsWithRooms(
            ids = *createdRoomIds.toTypedArray(),
            onTransactionAvailable = { transaction ->
                callCount++
                Assertions.assertTrue(createdRoomIds.contains(transaction.room.id))
                transaction.abort()
            })

        Assertions.assertEquals(createdRoomIds.size - 1, callCount)
    }

    @Test
    fun beginTransactionsWithRoomsListUnknownIdTest() {
        val roomProvider = newInstance()
        val createdRoomIds = MutableList(5) { roomProvider.createNewRoom(TestUtils.defaultConnectionId).id }
        createdRoomIds.add(TestUtils.getRandomHexString(*createdRoomIds.toTypedArray()))

        var callCount = 0
        roomProvider.beginTransactionsWithRooms(ids = createdRoomIds, onTransactionAvailable = { transaction ->
            callCount++
            Assertions.assertTrue(createdRoomIds.contains(transaction.room.id))
            transaction.abort()
        })

        Assertions.assertEquals(createdRoomIds.size - 1, callCount)
    }

    @Test
    fun hasApplicableRoomEmptyProviderTest() {
        Assertions.assertNull(newInstance().hasApplicableRoom("vatbub"))
    }

    @Test
    fun hasApplicableRoomLobbyFullTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
        transaction.room.connectedUsers.add(User(TestUtils.getRandomHexString(TestUtils.defaultConnectionId), "heykey"))
        transaction.commit()

        Assertions.assertNull(roomProvider.hasApplicableRoom("mo-mar"))
    }

    @Test
    fun hasApplicableRoomNonMatchingMinRoomSizeTest() {
        val roomProvider = newInstance()
        roomProvider.createNewRoom(TestUtils.defaultConnectionId, minRoomSize = 0)
        Assertions.assertNull(roomProvider.hasApplicableRoom("mo-mar"))
    }

    @Test
    fun hasApplicableRoomNonMatchingMaxRoomSizeTest() {
        val roomProvider = newInstance()
        roomProvider.createNewRoom(TestUtils.defaultConnectionId, maxRoomSize = 5)
        Assertions.assertNull(roomProvider.hasApplicableRoom("mo-mar"))
    }

    @Test
    fun hasApplicableRoomNonMatchingRoomBlacklistTest() {
        val roomProvider = newInstance()
        val userName = "mo-mar"
        roomProvider.createNewRoom(TestUtils.defaultConnectionId, blacklist = listOf(userName))
        Assertions.assertNull(roomProvider.hasApplicableRoom(userName))
    }

    @Test
    fun hasApplicableRoomNonMatchingRoomWhitelistTest() {
        val roomProvider = newInstance()
        roomProvider.createNewRoom(TestUtils.defaultConnectionId, whitelist = listOf("mo-mar"))
        Assertions.assertNull(roomProvider.hasApplicableRoom("vatbub"))
    }

    @Test
    fun hasApplicableRoomNonMatchingRequestBlacklistTest() {
        val roomProvider = newInstance()
        val userName = "mo-mar"
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.connectedUsers.add(User(TestUtils.defaultConnectionId, userName))
        transaction.commit()
        Assertions.assertNull(roomProvider.hasApplicableRoom("vatbub", blacklist = listOf(userName)))
    }

    @Test
    fun hasApplicableRoomNonMatchingRequestWhitelistTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.connectedUsers.add(User(TestUtils.defaultConnectionId, "mo-mar"))
        transaction.commit()
        Assertions.assertNull(roomProvider.hasApplicableRoom("vatbub", whitelist = listOf("heykey")))
    }

    @Test
    fun positiveHasApplicableRoomTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val applicableRoomTransaction = roomProvider.hasApplicableRoom("mo-mar")
        Assertions.assertNotNull(applicableRoomTransaction)
        applicableRoomTransaction!!
        Assertions.assertEquals(room.id, applicableRoomTransaction.room.id)
        applicableRoomTransaction.abort()
    }

    @Test
    fun positiveHasApplicableRoomWithMultipleApplicableRoomsTest() {
        val roomProvider = newInstance()
        val room1 = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))
        val applicableRoomTransaction = roomProvider.hasApplicableRoom("mo-mar")
        Assertions.assertNotNull(applicableRoomTransaction)
        applicableRoomTransaction!!
        Assertions.assertEquals(room1.id, applicableRoomTransaction.room.id)
        applicableRoomTransaction.abort()
    }

    @Test
    fun filterTest() {
        val roomProvider = newInstance()
        val targetConnectionId = "250b7528"
        val hostConnectionIds = listOf(
            targetConnectionId,
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )

        for (id in hostConnectionIds)
            roomProvider.createNewRoom(id)

        val filteredRooms = roomProvider.filter { room -> room.hostUserConnectionId == targetConnectionId }

        Assertions.assertEquals(1, filteredRooms.size)
        Assertions.assertEquals(targetConnectionId, filteredRooms.toList()[0].hostUserConnectionId)
    }

    @Test
    fun forEachTest() {
        val roomProvider = newInstance()
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val createdRooms = List(hostConnectionIds.size) { roomProvider.createNewRoom(hostConnectionIds[it]) }

        var callCount = 0
        roomProvider.forEach {
            Assertions.assertEquals(createdRooms[callCount], it)
            callCount++
        }

        Assertions.assertEquals(createdRooms.size, callCount)
    }

    @Test
    fun dataToBeSentToHostTest() {
        val roomProvider = newInstance()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        val data = MutableList(2) { GameData(TestUtils.defaultConnectionId) }
        data.forEachIndexed { index, gameData ->
            gameData["key$index"] = "value$index"
        }

        for (gameData in data)
            transaction.room.dataToBeSentToTheHost.add(gameData)
        transaction.commit()

        val retrievedRoom = roomProvider[room.id]!!
        Assertions.assertEquals(data.size, retrievedRoom.dataToBeSentToTheHost.size)

        room.dataToBeSentToTheHost.forEachIndexed { index, gameData ->
            Assertions.assertEquals(data[index], gameData)
        }
    }
}
