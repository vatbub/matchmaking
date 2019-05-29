package com.github.vatbub.matchmaking.server

import java.io.OutputStream
import java.io.Writer
import java.net.URI
import java.nio.ByteBuffer
import java.security.Principal
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import javax.websocket.*

class MockSession : Session {
    val mockBasicRemote = MockBasicEndpoint()
    val mockAsyncRemote = MockAsyncEndpoint(mockBasicRemote)

    override fun getPathParameters(): MutableMap<String, String> {
        throw NotImplementedError()
    }

    override fun getId(): String {
        throw NotImplementedError()
    }

    override fun getProtocolVersion(): String {
        throw NotImplementedError()
    }

    override fun getUserProperties(): MutableMap<String, Any> {
        throw NotImplementedError()
    }

    override fun removeMessageHandler(handler: MessageHandler?) {
        throw NotImplementedError()
    }

    override fun addMessageHandler(handler: MessageHandler?) {
        throw NotImplementedError()
    }

    override fun <T : Any?> addMessageHandler(clazz: Class<T>?, handler: MessageHandler.Whole<T>?) {
        throw NotImplementedError()
    }

    override fun <T : Any?> addMessageHandler(clazz: Class<T>?, handler: MessageHandler.Partial<T>?) {
        throw NotImplementedError()
    }

    override fun getQueryString(): String {
        throw NotImplementedError()
    }

    override fun getMaxIdleTimeout(): Long {
        throw NotImplementedError()
    }

    override fun setMaxIdleTimeout(milliseconds: Long) {
        throw NotImplementedError()
    }

    override fun getNegotiatedExtensions(): MutableList<Extension> {
        throw NotImplementedError()
    }

    override fun getUserPrincipal(): Principal {
        throw NotImplementedError()
    }

    override fun isOpen(): Boolean {
        throw NotImplementedError()
    }

    override fun close() {
        throw NotImplementedError()
    }

    override fun close(closeReason: CloseReason?) {
        throw NotImplementedError()
    }

    override fun getNegotiatedSubprotocol(): String {
        throw NotImplementedError()
    }

    override fun getMaxTextMessageBufferSize(): Int {
        throw NotImplementedError()
    }

    override fun getMaxBinaryMessageBufferSize(): Int {
        throw NotImplementedError()
    }

    override fun getAsyncRemote() = mockAsyncRemote

    override fun getBasicRemote() = mockBasicRemote

    override fun getRequestParameterMap(): MutableMap<String, MutableList<String>> {
        throw NotImplementedError()
    }

    override fun getRequestURI(): URI {
        throw NotImplementedError()
    }

    override fun getMessageHandlers(): MutableSet<MessageHandler> {
        throw NotImplementedError()
    }

    override fun setMaxTextMessageBufferSize(length: Int) {
        throw NotImplementedError()
    }

    override fun setMaxBinaryMessageBufferSize(length: Int) {
        throw NotImplementedError()
    }

    override fun getContainer(): WebSocketContainer {
        throw NotImplementedError()
    }

    override fun isSecure(): Boolean {
        throw NotImplementedError()
    }

    override fun getOpenSessions(): MutableSet<Session> {
        throw NotImplementedError()
    }
}

class MockEndpointConfig : EndpointConfig {
    override fun getEncoders(): MutableList<Class<out Encoder>> {
        throw NotImplementedError()
    }

    override fun getDecoders(): MutableList<Class<out Decoder>> {
        throw NotImplementedError()
    }

    override fun getUserProperties() = mutableMapOf<String, Any>()
}

class MockBasicEndpoint : RemoteEndpoint.Basic {
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
        throw NotImplementedError()
    }

    override fun sendObject(data: Any?) {
        objectData.add(data)
    }

    override fun getBatchingAllowed() = batchingAllowed

    override fun setBatchingAllowed(allowed: Boolean) {
        batchingAllowed = allowed
    }

    override fun getSendStream(): OutputStream {
        throw NotImplementedError()
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

class MockAsyncEndpoint(val synchronousEndpoint: RemoteEndpoint.Basic) : RemoteEndpoint.Async {
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
        throw NotImplementedError()
    }

    override fun sendObject(data: Any?) = run { synchronousEndpoint.sendObject(data) }

    override fun sendObject(data: Any?, handler: SendHandler?) {
        synchronousEndpoint.sendObject(data)
    }

    override fun setSendTimeout(timeoutmillis: Long) {
        throw NotImplementedError()
    }

    override fun getBatchingAllowed(): Boolean {
        throw NotImplementedError()
    }

    override fun setBatchingAllowed(allowed: Boolean) {
        throw NotImplementedError()
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