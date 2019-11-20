package com.github.vatbub.matchmaking.standaloneserverlauncher

import org.apache.mina.core.filterchain.IoFilterChain
import org.apache.mina.core.future.CloseFuture
import org.apache.mina.core.future.ReadFuture
import org.apache.mina.core.future.WriteFuture
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.service.IoService
import org.apache.mina.core.service.TransportMetadata
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.core.session.IoSessionConfig
import org.apache.mina.core.write.WriteRequest
import org.apache.mina.core.write.WriteRequestQueue
import java.net.SocketAddress

/**
 * An implementation of [IoSession] which throws an [Exception] when calling any of its methods.
 */
open class DummyIOSession : IoSession {
    override fun isActive(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun updateThroughput(currentTime: Long, force: Boolean) {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastWriterIdleTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getId(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun write(message: Any?): WriteFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun write(message: Any?, destination: SocketAddress?): WriteFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWrittenBytes(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastIdleTime(status: IdleStatus?): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWrittenBytesThroughput(): Double {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastBothIdleTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getReadMessagesThroughput(): Double {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getCreationTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun suspendRead() {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getReadBytes(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getHandler(): IoHandler {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun closeOnFlush(): CloseFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getScheduledWriteMessages(): Int {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun close(immediately: Boolean): CloseFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun close(): CloseFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getCurrentWriteRequest(): WriteRequest {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setAttributeIfAbsent(key: Any?, value: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setAttributeIfAbsent(key: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWriteRequestQueue(): WriteRequestQueue {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isReaderIdle(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getIdleCount(status: IdleStatus?): Int {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isBothIdle(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getServiceAddress(): SocketAddress {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun resumeWrite() {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getAttribute(key: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getAttribute(key: Any?, defaultValue: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getConfig(): IoSessionConfig {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isWriteSuspended(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun closeNow(): CloseFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getCloseFuture(): CloseFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getReadBytesThroughput(): Double {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getRemoteAddress(): SocketAddress {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWrittenMessagesThroughput(): Double {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getBothIdleCount(): Int {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWrittenMessages(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getWriterIdleCount(): Int {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getTransportMetadata(): TransportMetadata {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getAttributeKeys(): MutableSet<Any> {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getReadMessages(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isWriterIdle(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getAttachment(): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getScheduledWriteBytes(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isConnected(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isReadSuspended(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setAttachment(attachment: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun suspendWrite() {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isSecured(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun read(): ReadFuture {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastReadTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun containsAttribute(key: Any?): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getCurrentWriteMessage(): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastWriteTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLocalAddress(): SocketAddress {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun removeAttribute(key: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun removeAttribute(key: Any?, value: Any?): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isClosing(): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun replaceAttribute(key: Any?, oldValue: Any?, newValue: Any?): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastIoTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getService(): IoService {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun resumeRead() {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getReaderIdleCount(): Int {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getFilterChain(): IoFilterChain {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setAttribute(key: Any?, value: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setAttribute(key: Any?): Any {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun getLastReaderIdleTime(): Long {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun setCurrentWriteRequest(currentWriteRequest: WriteRequest?) {
        throw Exception("Dummy implementation, no functionality")
    }

    override fun isIdle(status: IdleStatus?): Boolean {
        throw Exception("Dummy implementation, no functionality")
    }

}