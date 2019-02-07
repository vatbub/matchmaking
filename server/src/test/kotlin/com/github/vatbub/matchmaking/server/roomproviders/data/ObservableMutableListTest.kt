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
package com.github.vatbub.matchmaking.server.roomproviders.data

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ObservableMutableListTest {
    @Test
    fun onAddTest() {
        var listenerCalled = false
        val expectedElement = "hello world"
        val expectedIndex = 0
        val observableMutableList = ObservableMutableList<String>(onAdd = { element, index ->
            Assertions.assertEquals(expectedElement, element)
            Assertions.assertEquals(expectedIndex, index)
            listenerCalled = true
        })

        observableMutableList.add(expectedElement)
        Assertions.assertEquals(1, observableMutableList.size)
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun onAddAtIndexTest() {
        val dummyValues = listOf("first", "second", "third")
        var listenerCalled = false
        val expectedElement = "hello world"
        val expectedIndex = 1
        val observableMutableList = ObservableMutableList(dummyValues, onAdd = { element, index ->
            Assertions.assertEquals(expectedElement, element)
            Assertions.assertEquals(expectedIndex, index)
            listenerCalled = true
        })

        observableMutableList.add(1, expectedElement)
        Assertions.assertEquals(4, observableMutableList.size)
        Assertions.assertEquals(dummyValues[0], observableMutableList[0])
        Assertions.assertEquals(expectedElement, observableMutableList[1])
        Assertions.assertEquals(dummyValues[1], observableMutableList[2])
        Assertions.assertEquals(dummyValues[2], observableMutableList[3])
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun onAddAllTest() {
        var listenerCallCount = 0
        val expectedElements = listOf("first", "second", "third")

        val observableMutableList = ObservableMutableList<String>(onAdd = { element, index ->
            Assertions.assertEquals(expectedElements[index], element)
            Assertions.assertEquals(expectedElements.indexOf(element), index)
            listenerCallCount++
        })

        observableMutableList.addAll(expectedElements)
        Assertions.assertEquals(3, observableMutableList.size)
        Assertions.assertEquals(3, listenerCallCount)
    }

    @Test
    fun onAddAllAtIndexTest() {
        val dummyValues = listOf("first", "second", "third")
        var listenerCallCount = 0
        val expectedElements = listOf("other first", "other second", "other third")
        val insertionIndex = 1

        val observableMutableList = ObservableMutableList(dummyValues, onAdd = { element, index ->
            Assertions.assertEquals(expectedElements[index - insertionIndex], element)
            Assertions.assertEquals(expectedElements.indexOf(element) + insertionIndex, index)
            listenerCallCount++
        })

        observableMutableList.addAll(insertionIndex, expectedElements)
        Assertions.assertEquals(6, observableMutableList.size)
        Assertions.assertEquals(dummyValues[0], observableMutableList[0])
        Assertions.assertEquals(expectedElements[0], observableMutableList[1])
        Assertions.assertEquals(expectedElements[1], observableMutableList[2])
        Assertions.assertEquals(expectedElements[2], observableMutableList[3])
        Assertions.assertEquals(dummyValues[1], observableMutableList[4])
        Assertions.assertEquals(dummyValues[2], observableMutableList[5])
        Assertions.assertEquals(3, listenerCallCount)
    }

    @Test
    fun onClearTest() {
        val values = listOf("first", "second", "third")
        var listenerCalled = false

        val observableMutableList = ObservableMutableList(values, onClear = { listenerCalled = true })

        Assertions.assertEquals(values.size, observableMutableList.size)
        observableMutableList.clear()
        Assertions.assertEquals(0, observableMutableList.size)
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun defaultConstructorTest() {
        val observableMutableList = ObservableMutableList<String>()
        assertNoListenersSpecified(observableMutableList)
    }

    @Test
    fun defaultConstructorWithListenersTest() {
        val observableMutableList =
            ObservableMutableList(
                this::defaultOnAdd,
                this::defaultOnSet,
                this::defaultOnRemove,
                this::defaultOnClear
            )

        Assertions.assertEquals(this::defaultOnAdd, observableMutableList.onAdd)
        Assertions.assertEquals(this::defaultOnRemove, observableMutableList.onRemove)
        Assertions.assertEquals(this::defaultOnClear, observableMutableList.onClear)
        Assertions.assertEquals(this::defaultOnSet, observableMutableList.onSet)
    }

    @Test
    fun collectionCopyConstructorTest() {
        val list = listOf("first", "second", "third")
        val observableMutableList = ObservableMutableList(list)
        Assertions.assertEquals(list.size, observableMutableList.size)
        for (i in 0 until list.size)
            Assertions.assertEquals(list[i], observableMutableList[i])

        assertNoListenersSpecified(observableMutableList)
    }

    @Test
    fun collectionCopyConstructorWithListenersTest() {
        val list = listOf("first", "second", "third")
        val observableMutableList =
            ObservableMutableList(
                list,
                this::defaultOnAdd,
                this::defaultOnSet,
                this::defaultOnRemove,
                this::defaultOnClear
            )

        Assertions.assertEquals(list.size, observableMutableList.size)
        for (i in 0 until list.size)
            Assertions.assertEquals(list[i], observableMutableList[i])

        Assertions.assertEquals(this::defaultOnAdd, observableMutableList.onAdd)
        Assertions.assertEquals(this::defaultOnRemove, observableMutableList.onRemove)
        Assertions.assertEquals(this::defaultOnClear, observableMutableList.onClear)
        Assertions.assertEquals(this::defaultOnSet, observableMutableList.onSet)
    }

    @Test
    fun initialCapacityConstructorTest() {
        val initialCapacity = 100
        val observableMutableList = ObservableMutableList<String>(initialCapacity)

        assertNoListenersSpecified(observableMutableList)
    }

    @Test
    fun initialCapacityConstructorWithListenersTest() {
        val initialCapacity = 100
        val observableMutableList =
            ObservableMutableList(
                initialCapacity,
                this::defaultOnAdd,
                this::defaultOnSet,
                this::defaultOnRemove,
                this::defaultOnClear
            )

        Assertions.assertEquals(this::defaultOnAdd, observableMutableList.onAdd)
        Assertions.assertEquals(this::defaultOnRemove, observableMutableList.onRemove)
        Assertions.assertEquals(this::defaultOnClear, observableMutableList.onClear)
        Assertions.assertEquals(this::defaultOnSet, observableMutableList.onSet)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun defaultOnAdd(element: String, index: Int) {

    }

    @Suppress("UNUSED_PARAMETER")
    private fun defaultOnSet(oldElement: String, newElement: String, index: Int) {

    }

    @Suppress("UNUSED_PARAMETER")
    private fun defaultOnRemove(element: String, index: Int) {

    }

    private fun defaultOnClear() {

    }

    private fun assertNoListenersSpecified(observableMutableList: ObservableMutableList<*>) {
        Assertions.assertNull(observableMutableList.onAdd)
        Assertions.assertNull(observableMutableList.onRemove)
        Assertions.assertNull(observableMutableList.onClear)
        Assertions.assertNull(observableMutableList.onSet)
    }

    @Test
    fun positiveContainsTest() {
        val value = "hello_world"
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.add(value)
        Assertions.assertTrue(observableMutableList.contains(value))
    }

    @Test
    fun negativeContainsTest() {
        val value = "hello_world"
        val observableMutableList = ObservableMutableList<String>()
        Assertions.assertFalse(observableMutableList.contains(value))
    }

    @Test
    fun positiveContainsAllTest() {
        val values = listOf("first", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)
        Assertions.assertTrue(observableMutableList.containsAll(values))
    }

    @Test
    fun negativeContainsAllTest() {
        val values = listOf("first", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        Assertions.assertFalse(observableMutableList.containsAll(values))
    }

    @Test
    fun getTest() {
        val value = "hello_world"
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.add(value)
        Assertions.assertEquals(value, observableMutableList[0])
    }

    @Test
    fun positiveIndexOfTest() {
        val values = listOf("first", "second", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(1, observableMutableList.indexOf(values[1]))
    }

    @Test
    fun negativeIndexOfTest() {
        val values = listOf("first", "second", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(-1, observableMutableList.indexOf("something else"))
    }

    @Test
    fun positiveIsEmptyTest() {
        val observableMutableList = ObservableMutableList<String>()
        Assertions.assertTrue(observableMutableList.isEmpty())
    }

    @Test
    fun negativeIsEmptyTest() {
        val value = "hello_world"
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.add(value)
        Assertions.assertFalse(observableMutableList.isEmpty())
    }

    @Test
    fun iteratorTest() {
        val values = listOf("first", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)

        val iterator = observableMutableList.iterator()

        for (value in values) {
            Assertions.assertTrue(iterator.hasNext())
            Assertions.assertEquals(value, iterator.next())
        }

        Assertions.assertFalse(iterator.hasNext())
        Assertions.assertThrows(NoSuchElementException::class.java) { iterator.next() }
    }

    @Test
    fun positiveLastIndexOfTest() {
        val values = listOf("first", "second", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(2, observableMutableList.lastIndexOf(values[2]))
    }

    @Test
    fun negativeLastIndexOfTest() {
        val values = listOf("first", "second", "second", "third")
        val observableMutableList = ObservableMutableList<String>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(-1, observableMutableList.lastIndexOf("something else"))
    }
}
