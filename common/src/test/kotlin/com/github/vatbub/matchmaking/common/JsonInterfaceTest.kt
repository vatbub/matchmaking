package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader

private data class DummyTestClass1(val property1: String, val property2: String)
    : Request(TestUtils.defaultConnectionId, TestUtils.defaultPassword, DummyTestClass1::class.qualifiedName!!, null)

class JsonInterfaceTest {
    private val objectUnderTest = DummyTestClass1("hello", "world")
    private val notPrettyJson = "{\"property1\":\"${objectUnderTest.property1}\",\"property2\":\"${objectUnderTest.property2}\",\"protocolVersion\":\"${objectUnderTest.protocolVersion}\",\"connectionId\":\"${objectUnderTest.connectionId}\",\"password\":\"${objectUnderTest.password}\",\"className\":\"${objectUnderTest.className}\"}"
    private val prettyJson = "{\n" +
            "  \"property1\": \"${objectUnderTest.property1}\",\n" +
            "  \"property2\": \"${objectUnderTest.property2}\",\n" +
            "  \"protocolVersion\": \"${objectUnderTest.protocolVersion}\",\n" +
            "  \"connectionId\": \"${objectUnderTest.connectionId}\",\n" +
            "  \"password\": \"${objectUnderTest.password}\",\n" +
            "  \"className\": \"${objectUnderTest.className}\"\n" +
            "}"

    @Test
    fun toJsonNoPrettifyTest() {
        val json = objectUnderTest.toJson()
        println(json)
        Assertions.assertFalse(json.contains(Regex("[\n\r]")))
        Assertions.assertEquals(notPrettyJson, json)
    }

    @Test
    fun toJsonPrettifyTest() {
        val json = objectUnderTest.toJson(true)
        println(json)
        Assertions.assertTrue(json.contains(Regex("[\n\r]")))
        Assertions.assertEquals(prettyJson, json)
    }

    @Test
    fun fromJsonStringClazzTest() {
        Assertions.assertEquals(objectUnderTest, fromJson(notPrettyJson, DummyTestClass1::class.java))
    }

    @Test
    fun fromJsonReaderClazzTest() {
        Assertions.assertEquals(objectUnderTest, fromJson(StringReader(notPrettyJson), DummyTestClass1::class.java))
    }

    @Test
    fun fromJsonStringTypeTest() {
        val abstractRequest = fromJson(notPrettyJson, Request::class.java)
        val requestClass = Class.forName(abstractRequest.className)
        Assertions.assertEquals(objectUnderTest, fromJson<DummyTestClass1>(notPrettyJson, requestClass))
    }

    @Test
    fun fromJsonReaderTypeTest() {
        val abstractRequest = fromJson(notPrettyJson, Request::class.java)
        val requestClass = Class.forName(abstractRequest.className)
        Assertions.assertEquals(objectUnderTest, fromJson<DummyTestClass1>(StringReader(notPrettyJson), requestClass))
    }
}