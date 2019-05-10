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
    override fun newObjectUnderTest() = GameData(TestUtils.defaultConnectionId)

    private fun <T : Any> testGameData(key: String, value: T) {
        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[key] = value

        Assertions.assertTrue(gameData.contains(key))
        Assertions.assertEquals(value, gameData[key]!!)
        Assertions.assertEquals(1, gameData.size)
        Assertions.assertTrue(gameData.keys.contains(key))
        Assertions.assertTrue(gameData.values.contains(value))

        val stringConversion = gameData.toString()
        Assertions.assertTrue(stringConversion.contains(key), "String '$stringConversion' does not contain $key")
    }

    @Test
    fun byteTest() =
            testGameData("sampleByte", 5)

    @Test
    fun byteArrayTest() =
            testGameData("sampleByteArray", ByteArray(3) { i -> i.toByte() })

    @Test
    fun charTest() =
            testGameData("sampleChar", 'a')

    @Test
    fun charArrayTest() =
            testGameData("sampleCharArray", CharArray(3) { i -> i.toChar() })

    @Test
    fun stringTest() =
            testGameData("sampleString", "Hello")

    @Test
    fun stringArrayTest() =
            testGameData("sampleStringArray", arrayOf("Hello", "Test"))

    @Test
    fun stringListTest() =
            testGameData("sampleStringList", listOf("Hello", "Test"))

    @Test
    fun floatTest() =
            testGameData("sampleFloat", 5.0f)

    @Test
    fun floatArrayTest() =
            testGameData("sampleFloatArray", FloatArray(3) { i -> i.toFloat() })

    @Test
    fun integerListTest() =
            testGameData("sampleIntegerList", IntArray(3) { i -> i })

    @Test
    fun shortTest() {
        val sampleShort: Short = 5
        testGameData("sampleShort", sampleShort)
    }

    @Test
    fun shortArrayTest() {
        testGameData("sampleShortArray", ShortArray(3) { i -> i.toShort() })
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
