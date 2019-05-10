package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.esotericsoftware.kryonet.Connection
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class KryoSessionWrapperTest {
    private var previousExceptionHandler: Thread.UncaughtExceptionHandler? = null
    private var mainThread: Thread? = null
    private val uncaughtExceptions = mutableListOf<Throwable>()
    private val testExceptionHandler = object : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(t: Thread?, e: Throwable?) {
            if (e == null) return
            if (t == mainThread)
                throw e
            uncaughtExceptions.add(e)
        }
    }

    @BeforeEach
    fun setExceptionHandler() {
        uncaughtExceptions.clear()
        mainThread = Thread.currentThread()
        previousExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(testExceptionHandler)
    }

    @AfterEach
    fun throwExceptionsFromOtherThreadsAndResetExceptionHandler(){
        uncaughtExceptions.forEach { throw it }
        uncaughtExceptions.clear()
        Thread.setDefaultUncaughtExceptionHandler(previousExceptionHandler)
    }

    @Test
    fun sendObjectTcpSyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(expectedTcpObject = tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(tcpObject)
        Assertions.assertEquals(1, connection.sendTcpCallCount)
        Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectUdpSyncTest() {
        val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(udpObject)
        Assertions.assertEquals(0, connection.sendTcpCallCount)
        Assertions.assertEquals(1, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectTcpAsyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(expectedTcpObject = tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(tcpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.sendTcpCallCount == 1 }
        Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectUdpAsyncTest() {
        val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(udpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.sendUdpCallCount == 1 }
        Assertions.assertEquals(0, connection.sendTcpCallCount)
    }
}

class MockConnection(private val allowUdp: Boolean = false, private val expectedTcpObject: Any? = null, private val expectedUdpObject: Any? = null) : Connection() {
    var sendTcpCallCount = 0
        private set
    var sendUdpCallCount = 0
        private set

    override fun sendTCP(objectToSend: Any?): Int {
        sendTcpCallCount++
        Assertions.assertEquals(expectedTcpObject, objectToSend)
        return 0
    }

    override fun sendUDP(objectToSend: Any?): Int {
        if (!allowUdp) throw IllegalStateException("Udp not allowed")
        sendUdpCallCount++
        Assertions.assertEquals(expectedUdpObject, objectToSend)
        return 0
    }
}
