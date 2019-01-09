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
import org.junit.Assert
import org.junit.jupiter.api.Test

class GameDataTest : KotlinTestSuperclass() {
    @Test
    fun byteTest() {
        val key = "sampleByte"
        val sampleByte: Byte = 5
        val gameData = GameData()
        gameData[key] = sampleByte

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleByte, gameData[key])
    }

    @Test
    fun byteArrayTest() {
        val key = "sampleByteArray"
        val sampleByteArray = ByteArray(3) { i -> i.toByte() }
        val gameData = GameData()
        gameData[key] = sampleByteArray

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleByteArray, gameData[key])
    }

    @Test
    fun charTest() {
        val key = "sampleChar"
        val sampleChar = 'a'
        val gameData = GameData()
        gameData[key] = sampleChar

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleChar, gameData[key])
    }

    @Test
    fun charArrayTest() {
        val key = "sampleCharArray"
        val sampleCharArray = CharArray(3) { i -> i.toChar() }
        val gameData = GameData()
        gameData[key] = sampleCharArray

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleCharArray, gameData[key])
    }

    @Test
    fun stringTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData()
        gameData[key] = sampleString

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleString, gameData[key])
    }

    @Test
    fun stringArrayTest() {
        val key = "sampleStringArray"
        val sampleStringArray = arrayOf("Hello", "Test")
        val gameData = GameData()
        gameData[key] = sampleStringArray

        Assert.assertTrue(gameData.contains(key))
        Assert.assertArrayEquals(sampleStringArray, gameData[key])
    }

    @Test
    fun stringListTest() {
        val key = "sampleStringList"
        val sampleStringList = listOf("Hello", "Test")
        val gameData = GameData()
        gameData[key] = sampleStringList

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleStringList, gameData[key])
    }

    @Test
    fun floatTest() {
        val key = "sampleFloat"
        val sampleFloat = 5.0f
        val gameData = GameData()
        gameData[key] = sampleFloat

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleFloat, gameData[key])
    }

    @Test
    fun floatArrayTest() {
        val key = "sampleFloatArray"
        val sampleFloatArray = FloatArray(3) { i -> i.toFloat() }
        val gameData = GameData()
        gameData[key] = sampleFloatArray

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleFloatArray, gameData[key])
    }

    @Test
    fun integerListTest() {
        val key = "sampleIntegerList"
        val sampleIntegerList = IntArray(3) { i -> i }
        val gameData = GameData()
        gameData[key] = sampleIntegerList

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleIntegerList, gameData[key])
    }

    @Test
    fun shortTest() {
        val key = "sampleShort"
        val sampleShort: Short = 5
        val gameData = GameData()
        gameData[key] = sampleShort

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleShort, gameData[key])
    }

    @Test
    fun shortArrayTest() {
        val key = "sampleShortArray"
        val sampleShortArray = ShortArray(3) { i -> i.toShort() }
        val gameData = GameData()
        gameData[key] = sampleShortArray

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleShortArray, gameData[key])
    }

    @Test
    fun typeMismatchTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData()
        gameData[key] = sampleString

        Assert.assertTrue(gameData.contains(key))
        val returnedObject: Int? = gameData[key, null, Int::class.java]
        Assert.assertNull(returnedObject)
    }

    @Test
    fun typeMismatchWithDefaultValueTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val defaultValue = 5
        val gameData = GameData()
        gameData[key] = sampleString

        Assert.assertTrue(gameData.contains(key))
        val returnedObject: Int? = gameData[key, defaultValue, Int::class.java]
        Assert.assertEquals(defaultValue, returnedObject)
    }

    @Test
    fun notFoundTest() {
        val key = "sampleString"
        val gameData = GameData()

        Assert.assertFalse(gameData.contains(key))
        Assert.assertNull(gameData[key])
    }

    @Test
    fun notFoundWithDefaultValueTest() {
        val key = "sampleString"
        val defaultValue = "DefaultValue"
        val gameData = GameData()

        Assert.assertFalse(gameData.contains(key))
        Assert.assertEquals(defaultValue, gameData[key, defaultValue])
    }

    @Test
    fun removeTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val gameData = GameData()
        gameData[key] = sampleString

        Assert.assertTrue(gameData.contains(key))
        Assert.assertEquals(sampleString, gameData.remove(key))
        Assert.assertFalse(gameData.contains(key))
    }
}
