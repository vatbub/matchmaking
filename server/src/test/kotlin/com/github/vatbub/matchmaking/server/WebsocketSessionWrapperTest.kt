package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WebsocketSessionWrapperTest {
    @Test
    fun sendSyncTest() {
        val session = MockSession()
        val sessionWrapper = WebsocketSessionWrapper(session)
        val request = DummyRequest()
        sessionWrapper.sendObjectSync(request)
        Assertions.assertEquals(1, session.mockBasicRemote.textData.size)
        val stringSent = session.mockBasicRemote.textData[0]!!
        val deserializedRequest = InteractionConverter.deserializeRequest<DummyRequest>(stringSent)
        Assertions.assertEquals(request, deserializedRequest)
    }

    @Test
    fun sendAsyncTest() {
        val session = MockSession()
        val sessionWrapper = WebsocketSessionWrapper(session)
        val request = DummyRequest()
        sessionWrapper.sendObjectAsync(request)
        Assertions.assertEquals(1, session.mockBasicRemote.textData.size)
        val stringSent = session.mockBasicRemote.textData[0]!!
        val deserializedRequest = InteractionConverter.deserializeRequest<DummyRequest>(stringSent)
        Assertions.assertEquals(request, deserializedRequest)
    }
}