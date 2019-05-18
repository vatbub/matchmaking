/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.data

import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameDataTest : KotlinTestSuperclass<GameData>() {
    override fun getCloneOf(instance: GameData) = instance.copy()

    override fun newObjectUnderTest() = GameData(TestUtils.defaultConnectionId)

    private fun <T : Any> testGameData(key: String, value: T, clazz: Class<T>? = null, expectedPrimitiveType: JavaPrimitive<T>? = null) {
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = value

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(value, gameData[key]!!)
        if (clazz != null)
            Assertions.assertEquals(value, gameData[key, null, clazz])
        if (expectedPrimitiveType != null)
            Assertions.assertEquals(value, gameData[key, null, null, expectedPrimitiveType])
        Assertions.assertEquals(1, gameData.size)
        Assertions.assertTrue(gameData.keys.contains(key))
        Assertions.assertTrue(gameData.values.contains(value))

        val stringConversion = gameData.toString()
        Assertions.assertTrue(stringConversion.contains(key), "String '$stringConversion' does not contain $key")
    }

    @Test
    fun byteTest() =
            testGameData("sampleByte", 5.toByte(), expectedPrimitiveType = ExpectedByte)

    @Test
    fun byteArrayTest() =
            testGameData("sampleByteArray", ByteArray(3) { i -> i.toByte() }, ByteArray::class.java)

    @Test
    fun charTest() =
            testGameData("sampleChar", 'a', expectedPrimitiveType = ExpectedChar)

    @Test
    fun charArrayTest() =
            testGameData("sampleCharArray", CharArray(3) { i -> i.toChar() }, CharArray::class.java)

    @Test
    fun stringTest() =
            testGameData("sampleString", "Hello", String::class.java)

    @Test
    fun stringArrayTest() =
            testGameData("sampleStringArray", arrayOf("Hello", "Test"), Array<String>::class.java)

    @Test
    fun stringListTest() {
        val list = listOf("Hello", "Test")
        testGameData("sampleStringList", list, list.javaClass)
    }

    @Test
    fun floatTest() =
            testGameData("sampleFloat", 5.0f, expectedPrimitiveType = ExpectedFloat)

    @Test
    fun intTest() =
            testGameData("sampleInt", 5, expectedPrimitiveType = ExpectedInt)

    @Test
    fun longTest() =
            testGameData("sampleLong", 5.toLong(), expectedPrimitiveType = ExpectedLong)

    @Test
    fun doubleTest() =
            testGameData("sampleDouble", 5.0, expectedPrimitiveType = ExpectedDouble)

    @Test
    fun booleanTest() =
            testGameData("sampleBoolean", true, expectedPrimitiveType = ExpectedBoolean)

    @Test
    fun floatArrayTest() =
            testGameData("sampleFloatArray", FloatArray(3) { i -> i.toFloat() }, FloatArray::class.java)

    @Test
    fun integerArrayTest() =
            testGameData("sampleIntegerList", IntArray(3) { i -> i }, IntArray::class.java)

    @Test
    fun shortTest() =
            testGameData("sampleShort", 5.toShort(), expectedPrimitiveType = ExpectedShort)

    @Test
    fun shortArrayTest() =
            testGameData("sampleShortArray", ShortArray(3) { i -> i.toShort() }, ShortArray::class.java)

    @Test
    fun typeMismatchForPrimitivesTest() {
        val key = "sampleShort"
        val value = 5.toShort()
        val defaultValue = 6.toFloat()
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = value
        Assertions.assertEquals(defaultValue, gameData[key, defaultValue, null, ExpectedFloat])
    }

    @Test
    fun typeMismatchTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleString

        Assertions.assertTrue(gameData.contains(key))
        val returnedObject: Int? = gameData[key, null, Int::class.java]
        Assertions.assertNull(returnedObject)
    }

    @Test
    fun typeMismatchWithDefaultValueTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val defaultValue = 5
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleString

        Assertions.assertTrue(gameData.contains(key))
        val returnedObject: Int? = gameData[key, defaultValue, Integer.TYPE]
        Assertions.assertEquals(defaultValue, returnedObject)
    }

    @Test
    fun typeMismatchNoTypeSpecifiedTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val defaultValue = 5
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleString

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertThrows(ClassCastException::class.java) { @Suppress("UNUSED_VARIABLE") val returnedObject: Int? = gameData[key, defaultValue] }
    }

    @Test
    fun notFoundTest() {
        val key = "sampleString"
        val gameData = GameData(TestUtils.defaultConnectionId)

        Assertions.assertFalse(gameData.contains(key))
        Assertions.assertNull(gameData[key])
    }

    @Test
    fun notFoundWithDefaultValueTest() {
        val key = "sampleString"
        val defaultValue = "DefaultValue"
        val gameData = GameData(TestUtils.defaultConnectionId)

        Assertions.assertFalse(gameData.contains(key))
        Assertions.assertEquals(defaultValue, gameData[key, defaultValue])
    }

    @Test
    fun removeTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleString

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleString, gameData.remove(key))
        Assertions.assertFalse(gameData.contains(key))
        Assertions.assertEquals(0, gameData.size)
    }

    @Test
    fun copyTest() {
        val original = GameData(TestUtils.defaultConnectionId)
        original["key1"] = "value1"
        original["key2"] = "value2"
        original["key3"] = "value3"

        val copy = original.copy()

        Assertions.assertEquals(original, copy)
        Assertions.assertEquals(original.hashCode(), copy.hashCode())
        Assertions.assertNotSame(original, copy)
    }

    @Test
    override fun notEqualsTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        val instance2 = GameData(TestUtils.getRandomHexString(instance1.createdByConnectionId))
        Assertions.assertNotEquals(instance1, instance2)
    }

    @Test
    fun contentsNotEqualTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        val instance2 = GameData(instance1.createdByConnectionId)
        instance2["key"] = "value"
        Assertions.assertNotEquals(instance1, instance2)
    }

    @Test
    fun creationTimestampNotEqualTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        Thread.sleep(1000)
        val instance2 = GameData(instance1.createdByConnectionId)
        Assertions.assertNotEquals(instance1, instance2)
    }

    @Test
    fun createdByConnectionIdHashCodeNotEqualTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        val instance2 = GameData(TestUtils.getRandomHexString(instance1.createdByConnectionId))
        Assertions.assertNotEquals(instance1.hashCode(), instance2.hashCode())
    }

    @Test
    fun contentsHashCodeNotEqualTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        val instance2 = GameData(instance1.createdByConnectionId)
        instance2["key"] = "value"
        Assertions.assertNotEquals(instance1.hashCode(), instance2.hashCode())
    }

    @Test
    fun creationTimestampHashCodeNotEqualTest() {
        val instance1 = GameData(TestUtils.defaultConnectionId)
        Thread.sleep(1000)
        val instance2 = GameData(instance1.createdByConnectionId)
        Assertions.assertNotEquals(instance1.hashCode(), instance2.hashCode())
    }
}
