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
package com.github.vatbub.matchmaking.server.logic.roomproviders.data

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class ObservableGameDataTest : KotlinTestSuperclass<ObservableGameData>() {
    override fun newObjectUnderTest() = ObservableGameData(GameData(TestUtils.defaultConnectionId))

    @Test
    fun immutabilityTest() {
        val originalGameData = GameData(TestUtils.defaultConnectionId)
        Thread.sleep(2000)
        val observableGameData = ObservableGameData(originalGameData)
        observableGameData["foo"] = "bar"
        observableGameData.createdAtUtc = Instant.now()

        Assertions.assertEquals(0, originalGameData.size)
        Assertions.assertEquals(1, observableGameData.size)
        Assertions.assertNotEquals(originalGameData.createdAtUtc, observableGameData.createdAtUtc)
    }

    @Test
    fun onReplaceBackingGameData() {
        val originalBackingGameData = GameData(TestUtils.defaultConnectionId)
        val expectedOriginalKey = "originalKey"
        val expectedOriginalValue = "originalValue"
        originalBackingGameData[expectedOriginalKey] = expectedOriginalValue
        val newGameData = GameData(TestUtils.defaultConnectionId)
        val expectedNewKey = "newKey"
        val expectedNewValue = "newValue"
        newGameData[expectedNewKey] = expectedNewValue
        var onRemoveCalled = false
        var onSetCalled = false

        val observableGameData = ObservableGameData(originalBackingGameData, onRemove = { key, value ->
            Assertions.assertEquals(expectedOriginalKey, key)
            Assertions.assertEquals(expectedOriginalValue, value)
            onRemoveCalled = true
        }, onSet = { key, oldValue, newValue ->
            Assertions.assertEquals(expectedNewKey, key)
            Assertions.assertNull(oldValue)
            Assertions.assertEquals(expectedNewValue, newValue)
            onSetCalled = true
        })

        observableGameData.replaceContents(newGameData)
        Assertions.assertEquals(newGameData, observableGameData.backingGameData)
        Assert.assertTrue(onRemoveCalled)
        Assert.assertTrue(onSetCalled)
    }

    @Test
    fun onSetTest() {
        val expectedKey = "key"
        val expectedValue = "value"
        var callbackCalled = false

        val observableGameData =
                ObservableGameData(GameData(TestUtils.defaultConnectionId), onSet = { key, oldValue, newValue ->
                    Assertions.assertEquals(expectedKey, key)
                    Assertions.assertNull(oldValue)
                    Assertions.assertEquals(expectedValue, newValue)
                    callbackCalled = true
                })

        observableGameData[expectedKey] = expectedValue
        Assert.assertTrue(callbackCalled)
    }

    @Test
    fun onSetReplacementTest() {
        val expectedKey = "key"
        val expectedOldValue = "old_value"
        val expectedNewValue = "new_value"
        var callbackCalled = false

        val gameData = GameData(TestUtils.defaultConnectionId)
        gameData[expectedKey] = expectedOldValue

        val observableGameData = ObservableGameData(gameData, onSet = { key, oldValue, newValue ->
            Assertions.assertEquals(expectedKey, key)
            Assertions.assertEquals(expectedOldValue, oldValue)
            Assertions.assertEquals(expectedNewValue, newValue)
            callbackCalled = true
        })

        observableGameData[expectedKey] = expectedNewValue
        Assert.assertTrue(callbackCalled)
    }

    @Test
    fun positiveGetTest() {
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        val key = "key"
        val expectedValue = "value"
        observableGameData[key] = expectedValue
        Assertions.assertSame(expectedValue, observableGameData[key])
    }

    @Test
    fun typeMismatchTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        observableGameData[key] = sampleString

        Assertions.assertTrue(observableGameData.contains(key))
        val returnedObject: Int? = observableGameData[key, null, Int::class.java]
        Assertions.assertNull(returnedObject)
    }

    @Test
    fun typeMismatchWithDefaultValueTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val defaultValue = 5
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        observableGameData[key] = sampleString

        Assertions.assertTrue(observableGameData.contains(key))
        val returnedObject: Int? = observableGameData[key, defaultValue, Int::class.java]
        Assertions.assertEquals(defaultValue, returnedObject)
    }

    @Test
    fun notFoundTest() {
        val key = "sampleString"
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))

        Assertions.assertFalse(observableGameData.contains(key))
        Assertions.assertNull(observableGameData[key])
    }

    @Test
    fun notFoundWithDefaultValueTest() {
        val key = "sampleString"
        val defaultValue = "DefaultValue"
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))

        Assertions.assertFalse(observableGameData.contains(key))
        Assertions.assertEquals(defaultValue, observableGameData[key, defaultValue])
    }

    @Test
    fun positiveContainsTest() {
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        val key = "key"
        val value = "value"
        observableGameData[key] = value
        Assertions.assertTrue(observableGameData.contains(key))
    }

    @Test
    fun negativeContainsTest() {
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        val key = "key"
        Assertions.assertFalse(observableGameData.contains(key))
    }

    @Test
    fun removeTest() {
        val key = "sampleString"
        val sampleString = "Hello"
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId))
        observableGameData[key] = sampleString

        Assertions.assertTrue(observableGameData.contains(key))
        Assertions.assertEquals(sampleString, observableGameData.remove(key))
        Assertions.assertFalse(observableGameData.contains(key))
        Assertions.assertEquals(0, observableGameData.size)
    }

    @Test
    fun onRemoveTest() {
        val expectedKey = "sampleString"
        val expectedValue = "Hello"
        var callbackCalled = false
        val observableGameData = ObservableGameData(GameData(TestUtils.defaultConnectionId), onRemove = { key, value ->
            Assertions.assertEquals(expectedKey, key)
            Assertions.assertEquals(expectedValue, value)
            callbackCalled = true
        })
        observableGameData[expectedKey] = expectedValue

        observableGameData.remove<Any>(expectedKey)
        Assert.assertTrue(callbackCalled)
    }
}
