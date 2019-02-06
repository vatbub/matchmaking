package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RoomTransactionTest : KotlinTestSuperclass() {
    @Test
    fun equalityTest() {
        val roomProvider = DummyRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val transaction1 = roomProvider.beginTransactionWithRoom(room.id)
        val transaction2 = roomProvider.beginTransactionWithRoom(room.id)

        Assertions.assertEquals(transaction1, transaction2)
        Assert.assertEquals(transaction1.hashCode(), transaction2.hashCode())
    }

    @Test
    fun inequalityTest() {
        val roomProvider = DummyRoomProvider()
        val room1 = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val room2 = roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))

        val transaction1 = roomProvider.beginTransactionWithRoom(room1.id)
        val transaction2 = roomProvider.beginTransactionWithRoom(room2.id)

        Assertions.assertNotEquals(transaction1, transaction2)
        Assert.assertNotEquals(transaction1.hashCode(), transaction2.hashCode())
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
}

class DummyRoomProvider : MemoryRoomProvider() {
    var commitTransactionCallCount = 0
        private set

    var abortTransactionCallCount = 0
        private set

    override fun commitTransaction(roomTransaction: RoomTransaction) {
        super.commitTransaction(roomTransaction)
        commitTransactionCallCount++
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
        super.abortTransaction(roomTransaction)
        abortTransactionCallCount++
    }
}