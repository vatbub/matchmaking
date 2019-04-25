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
package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DummyRequestHandler
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStream
import java.io.Writer
import java.net.URI
import java.nio.ByteBuffer
import java.security.Principal
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.websocket.*

class WebsocketEndpointTest {
    private lateinit var websocketEndpoint: WebsocketEndpoint
    private lateinit var websocketSession: MockSession
    private lateinit var websocketEndpointConfig: EndpointConfig

    @BeforeEach
    fun instantiateAndCallOpen() {
        websocketEndpoint = WebsocketEndpoint()
        websocketSession = MockSession()
        websocketEndpointConfig = MockEndpointConfig()
        websocketEndpoint.open(websocketSession, websocketEndpointConfig)
    }

    @AfterEach
    fun callOnSessionClose() {
        websocketEndpoint.onSessionClose(websocketSession, CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test finished"))
    }

    private fun sendRequest(request: Request): Response {
        val serializedRequest = InteractionConverter.serialize(request)
        val serializedResponse = sendRequest(serializedRequest)
        return InteractionConverter.deserializeResponse(serializedResponse)
    }

    private fun sendRequest(request: String): String {
        websocketSession.mockBasicRemote.textData.clear()
        websocketEndpoint.onTextMessage(websocketSession, request)
        return websocketSession.mockBasicRemote.textData.joinToString("\n")
    }

    @Test
    fun positiveHandleTest() {
        websocketEndpoint.serverContext.messageDispatcher.removeAllHandlers()
        websocketEndpoint.serverContext.messageDispatcher.registerHandler(DummyRequestHandler())
        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val response = sendRequest(request)
        Assertions.assertTrue(response is DummyResponse)
        response as DummyResponse
        Assertions.assertEquals(request.connectionId, response.connectionId)
    }

    private class MockSession : Session {
        val mockBasicRemote = MockBasicEndpoint()
        val mockAsyncRemote = MockAsyncEndpoint(mockBasicRemote)

        override fun getPathParameters(): MutableMap<String, String> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getId(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getProtocolVersion(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUserProperties(): MutableMap<String, Any> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun removeMessageHandler(handler: MessageHandler?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addMessageHandler(handler: MessageHandler?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T : Any?> addMessageHandler(clazz: Class<T>?, handler: MessageHandler.Whole<T>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <T : Any?> addMessageHandler(clazz: Class<T>?, handler: MessageHandler.Partial<T>?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getQueryString(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMaxIdleTimeout(): Long {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setMaxIdleTimeout(milliseconds: Long) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getNegotiatedExtensions(): MutableList<Extension> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUserPrincipal(): Principal {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isOpen(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun close() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun close(closeReason: CloseReason?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getNegotiatedSubprotocol(): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMaxTextMessageBufferSize(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMaxBinaryMessageBufferSize(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getAsyncRemote() = mockAsyncRemote

        override fun getBasicRemote() = mockBasicRemote

        override fun getRequestParameterMap(): MutableMap<String, MutableList<String>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getRequestURI(): URI {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getMessageHandlers(): MutableSet<MessageHandler> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setMaxTextMessageBufferSize(length: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setMaxBinaryMessageBufferSize(length: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getContainer(): WebSocketContainer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun isSecure(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getOpenSessions(): MutableSet<Session> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private class MockEndpointConfig : EndpointConfig {
        override fun getEncoders(): MutableList<Class<out Encoder>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getDecoders(): MutableList<Class<out Decoder>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getUserProperties()=mutableMapOf<String, Any>()
    }

    private class MockBasicEndpoint : RemoteEndpoint.Basic {
        val binaryData = mutableListOf<ByteBuffer?>()
        val pingData = mutableListOf<ByteBuffer?>()
        val pongData = mutableListOf<ByteBuffer?>()
        val objectData = mutableListOf<Any?>()
        val textData = mutableListOf<String?>()
        private var batchingAllowed = false
        override fun sendBinary(data: ByteBuffer?) {
            binaryData.add(data)
        }

        override fun sendBinary(partialByte: ByteBuffer?, isLast: Boolean) {
            binaryData.add(partialByte)
        }

        override fun sendPong(applicationData: ByteBuffer?) {
            pongData.add(applicationData)
        }

        override fun getSendWriter(): Writer {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun sendObject(data: Any?) {
            objectData.add(data)
        }

        override fun getBatchingAllowed() = batchingAllowed

        override fun setBatchingAllowed(allowed: Boolean) {
            batchingAllowed = allowed
        }

        override fun getSendStream(): OutputStream {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun flushBatch() {}

        override fun sendPing(applicationData: ByteBuffer?) {
            pingData.add(applicationData)
        }

        override fun sendText(text: String?) {
            textData.add(text)
        }

        override fun sendText(partialMessage: String?, isLast: Boolean) {
            textData.add(partialMessage)
        }
    }

    private class MockAsyncEndpoint(val synchronousEndpoint: RemoteEndpoint.Basic) : RemoteEndpoint.Async {
        private fun run(function: (() -> Unit)): Future<Void> {
            val task = FutureTask<Void> { function.invoke(); null }
            task.run()
            return task
        }

        override fun sendBinary(data: ByteBuffer?) = run { synchronousEndpoint.sendBinary(data) }

        override fun sendBinary(data: ByteBuffer?, handler: SendHandler?) {
            synchronousEndpoint.sendBinary(data)
        }

        override fun sendPong(applicationData: ByteBuffer?) {
            synchronousEndpoint.sendPong(applicationData)
        }

        override fun getSendTimeout(): Long {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun sendObject(data: Any?) = run { synchronousEndpoint.sendObject(data) }

        override fun sendObject(data: Any?, handler: SendHandler?) {
            synchronousEndpoint.sendObject(data)
        }

        override fun setSendTimeout(timeoutmillis: Long) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getBatchingAllowed(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun setBatchingAllowed(allowed: Boolean) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun flushBatch() {}

        override fun sendPing(applicationData: ByteBuffer?) {
            synchronousEndpoint.sendPing(applicationData)
        }

        override fun sendText(text: String?, handler: SendHandler?) {
            synchronousEndpoint.sendText(text)
        }

        override fun sendText(text: String?) = run { synchronousEndpoint.sendText(text) }
    }
}
