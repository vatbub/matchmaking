package com.github.vatbub.matchmaking.common.serializationtests.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class RequestSerializationTestSuperclass<T : Request>(clazz: Class<T>) : ServerInteractionSerializationTestSuperclass<T>(clazz) {
    @Test
    fun copyTest() {
        val request = newObjectUnderTest()
        val copy = request.copy()
        Assertions.assertEquals(request, copy)
        Assertions.assertNotSame(request, copy)
    }

    @Test
    fun connectionIdNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = Request(TestUtils.getRandomHexString(request1.connectionId), request1.password, request1.className, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun classNameNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = Request(request1.connectionId, request1.password, TestUtils.getRandomHexString(request1.className), request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun requestIdNotEqualTest() {
        val request1 = newObjectUnderTest()
        val request2 = Request(request1.connectionId, request1.password, request1.className, TestUtils.getRandomHexString(request1.requestId))
        Assertions.assertNotEquals(request1, request2)
    }
}

class RequestSerializationTest : RequestSerializationTestSuperclass<Request>(Request::class.java) {
    override fun newObjectUnderTest() = Request(TestUtils.defaultConnectionId, TestUtils.defaultPassword, Request::class.qualifiedName!!, TestUtils.getRandomHexString())

    // Already tested in the super class
    override fun notEqualsTest() {}
}