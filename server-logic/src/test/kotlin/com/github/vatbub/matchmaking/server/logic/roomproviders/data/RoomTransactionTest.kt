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
package com.github.vatbub.matchmaking.server.logic.roomproviders.data

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RoomTransactionTest : KotlinTestSuperclass<RoomTransaction>() {
    override fun getCloneOf(instance: RoomTransaction) = RoomTransaction(instance.room, instance.roomProvider)
    override val skipEqualsOtherInstanceTests = true

    override fun newObjectUnderTest() = RoomTransaction(ObservableRoom(Room(TestUtils.getRandomHexString(), TestUtils.defaultConnectionId)), MemoryRoomProvider())

    @Test
    fun equalityTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction1 = RoomTransaction(ObservableRoom(room), roomProvider)

        Assertions.assertEquals(transaction1, transaction1)
        Assertions.assertEquals(transaction1.hashCode(), transaction1.hashCode())
    }

    @Test
    fun sameRoomEqualityTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction1 = RoomTransaction(ObservableRoom(room), roomProvider)
        val transaction2 = RoomTransaction(ObservableRoom(room), roomProvider)

        Assertions.assertNotEquals(transaction1, transaction2)
        Assertions.assertNotEquals(transaction1.hashCode(), transaction2.hashCode())
    }

    @Test
    fun inequalityTest() {
        val roomProvider = DummyRoomProvider()
        val room1 = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val room2 = roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))

        val transaction1 = roomProvider.beginTransactionWithRoom(room1.id)
        val transaction2 = roomProvider.beginTransactionWithRoom(room2.id)

        Assertions.assertNotEquals(transaction1, transaction2)
        Assertions.assertNotEquals(transaction1.hashCode(), transaction2.hashCode())
    }

    @Test
    fun commitCallTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        Assertions.assertFalse(transaction.finalized)
        transaction.commit()
        Assertions.assertEquals(1, roomProvider.commitTransactionCallCount)
        Assertions.assertEquals(0, roomProvider.abortTransactionCallCount)
        Assertions.assertTrue(transaction.finalized)
    }

    @Test
    fun abortCallTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        Assertions.assertFalse(transaction.finalized)
        transaction.abort()
        Assertions.assertEquals(0, roomProvider.commitTransactionCallCount)
        Assertions.assertEquals(1, roomProvider.abortTransactionCallCount)
        Assertions.assertTrue(transaction.finalized)
    }

    @Test
    fun multipleCommitsTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        transaction.commit()
        transaction.commit()
        Assertions.assertEquals(1, roomProvider.commitTransactionCallCount)
        Assertions.assertEquals(0, roomProvider.abortTransactionCallCount)
    }

    @Test
    fun multipleAbortsTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        transaction.abort()
        transaction.abort()
        Assertions.assertEquals(0, roomProvider.commitTransactionCallCount)
        Assertions.assertEquals(1, roomProvider.abortTransactionCallCount)
    }

    @Test
    fun getRoomAfterFinalizationThroughCommitTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        Assertions.assertDoesNotThrow { transaction.room }
        transaction.commit()
        Assertions.assertThrows(IllegalStateException::class.java) { transaction.room }
    }

    @Test
    fun getRoomAfterFinalizationThroughAbortTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!

        Assertions.assertDoesNotThrow { transaction.room }
        transaction.abort()
        Assertions.assertThrows(IllegalStateException::class.java) { transaction.room }
    }

    @Test
    override fun notEqualsTest() {
        val object1 = newObjectUnderTest()
        val object2 = newObjectUnderTest()
        Assertions.assertNotEquals(object1, object2)
    }
}

class DummyRoomProvider : MemoryRoomProvider() {
    var commitTransactionCallCount = 0
        private set

    var abortTransactionCallCount = 0
        private set

    override fun commitTransactionImpl(roomTransaction: RoomTransaction) {
        super.commitTransactionImpl(roomTransaction)
        commitTransactionCallCount++
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
        super.abortTransaction(roomTransaction)
        abortTransactionCallCount++
    }
}
