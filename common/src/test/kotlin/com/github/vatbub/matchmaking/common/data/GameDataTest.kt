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

class GameDataTest : KotlinTestSuperclass() {
    @Test
    fun byteTest() {
        val key = "sampleByte"
        val sampleByte: Byte = 5
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleByte

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleByte, gameData[key]!!)
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun byteArrayTest() {
        val key = "sampleByteArray"
        val sampleByteArray = ByteArray(3) { i -> i.toByte() }
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleByteArray

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleByteArray, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun charTest() {
        val key = "sampleChar"
        val sampleChar = 'a'
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleChar

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleChar, gameData[key]!!)
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun charArrayTest() {
        val key = "sampleCharArray"
        val sampleCharArray = CharArray(3) { i -> i.toChar() }
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleCharArray

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleCharArray, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun stringTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleString

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleString, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun stringArrayTest() {
        val key = "sampleStringArray"
        val sampleStringArray = arrayOf("Hello", "Test")
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleStringArray

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertArrayEquals(sampleStringArray, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun stringListTest() {
        val key = "sampleStringList"
        val sampleStringList = listOf("Hello", "Test")
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleStringList

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleStringList, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun floatTest() {
        val key = "sampleFloat"
        val sampleFloat = 5.0f
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleFloat

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleFloat, gameData[key]!!)
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun floatArrayTest() {
        val key = "sampleFloatArray"
        val sampleFloatArray = FloatArray(3) { i -> i.toFloat() }
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleFloatArray

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleFloatArray, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun integerListTest() {
        val key = "sampleIntegerList"
        val sampleIntegerList = IntArray(3) { i -> i }
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleIntegerList

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleIntegerList, gameData[key])
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun shortTest() {
        val key = "sampleShort"
        val sampleShort: Short = 5
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleShort

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleShort, gameData[key]!!)
        Assertions.assertEquals(1, gameData.size)
    }

    @Test
    fun shortArrayTest() {
        val key = "sampleShortArray"
        val sampleShortArray = ShortArray(3) { i -> i.toShort() }
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = sampleShortArray

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(sampleShortArray, gameData[key])
        Assertions.assertEquals(1, gameData.size)
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
        val returnedObject: Int? = gameData[key, defaultValue, Int::class.java]
        Assertions.assertEquals(defaultValue, returnedObject)
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
}
