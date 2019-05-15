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

import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ObservableMutableListTestSuperclass<T> : KotlinTestSuperclass<ObservableMutableList<T>>() {
    override fun getCloneOf(instance: ObservableMutableList<T>) = ObservableMutableList(instance, instance.onAdd, instance.onSet, instance.onRemove, instance.onClear)
    override fun newObjectUnderTest() = ObservableMutableList<T>(0)
    abstract fun getNewTestElement(): T

    @Test
    fun onAddTest() {
        var listenerCalled = false
        val expectedElement = getNewTestElement()
        val expectedIndex = 0
        val observableMutableList = ObservableMutableList<T>(onAdd = { element, index ->
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
        val dummyValues = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        var listenerCalled = false
        val expectedElement = getNewTestElement()
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
        val expectedElements = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())

        val observableMutableList = ObservableMutableList<T>(onAdd = { element, index ->
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
        val dummyValues = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        var listenerCallCount = 0
        val expectedElements = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
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
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        var listenerCalled = false

        val observableMutableList = ObservableMutableList(values, onClear = { listenerCalled = true })

        Assertions.assertEquals(values.size, observableMutableList.size)
        observableMutableList.clear()
        Assertions.assertEquals(0, observableMutableList.size)
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun onRemoveTest() {
        val expectedElement = getNewTestElement()
        val expectedIndex = 1
        val values = listOf(getNewTestElement(), expectedElement, getNewTestElement())
        var listenerCalled = false

        val observableMutableList =
                ObservableMutableList(values, onRemove = { element, index ->
                    Assertions.assertEquals(expectedElement, element)
                    Assertions.assertEquals(expectedIndex, index)
                    listenerCalled = true
                })

        Assertions.assertEquals(values.size, observableMutableList.size)
        Assertions.assertTrue(observableMutableList.remove(expectedElement))
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun onRemoveNonExistingItemTest() {
        val expectedElement = getNewTestElement()

        val observableMutableList =
                ObservableMutableList<T>(onRemove = { _, _ ->
                    Assertions.fail("Listener should not be called")
                })

        Assertions.assertFalse(observableMutableList.remove(expectedElement))
    }

    @Test
    fun onRemoveAllTest() {
        val elementsToBeDeleted = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val values = mutableListOf<T>()
        for (element in elementsToBeDeleted)
            values.add(element)
        values.add(getNewTestElement())

        var listenerCallCount = 0

        val observableMutableList =
                ObservableMutableList(values, onRemove = { element, index ->
                    Assertions.assertEquals(values[index], element)
                    Assertions.assertTrue(element in elementsToBeDeleted)
                    listenerCallCount++
                })

        Assertions.assertEquals(values.size, observableMutableList.size)
        Assertions.assertTrue(observableMutableList.removeAll(elementsToBeDeleted))
        Assertions.assertEquals(elementsToBeDeleted.size, listenerCallCount)
        Assertions.assertEquals(values.size - elementsToBeDeleted.size, observableMutableList.size)
    }

    @Test
    fun onRemoveAtTest() {
        val expectedElement = getNewTestElement()
        val expectedIndex = 1
        val values = listOf(getNewTestElement(), expectedElement, getNewTestElement())
        var listenerCalled = false

        val observableMutableList =
                ObservableMutableList(values, onRemove = { element, index ->
                    Assertions.assertEquals(expectedElement, element)
                    Assertions.assertEquals(expectedIndex, index)
                    listenerCalled = true
                })

        Assertions.assertEquals(values.size, observableMutableList.size)
        Assertions.assertEquals(expectedElement, observableMutableList.removeAt(expectedIndex))
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun retainAllTest() {
        val elementsToBeRetained = listOf(getNewTestElement(), getNewTestElement())
        val values = mutableListOf<T>()
        values.add(getNewTestElement())
        for (element in elementsToBeRetained)
            values.add(element)
        values.add(getNewTestElement())

        var listenerCallCount = 0

        val observableMutableList =
                ObservableMutableList(values, onRemove = { element, index ->
                    Assertions.assertEquals(values[index], element)
                    Assertions.assertFalse(element in elementsToBeRetained)
                    listenerCallCount++
                })

        Assertions.assertEquals(values.size, observableMutableList.size)
        Assertions.assertTrue(observableMutableList.retainAll(elementsToBeRetained))
        Assertions.assertEquals(values.size - elementsToBeRetained.size, listenerCallCount)
        Assertions.assertEquals(elementsToBeRetained.size, observableMutableList.size)
    }

    @Test
    fun onSetTest() {
        val expectedOldValue = getNewTestElement()
        val expectedNewValue = getNewTestElement()
        val expectedIndex = 0
        var listenerCalled = false
        val observableMutableList =
                ObservableMutableList(listOf(expectedOldValue), onSet = { oldValue, newValue, index ->
                    Assertions.assertEquals(expectedOldValue, oldValue)
                    Assertions.assertEquals(expectedNewValue, newValue)
                    Assertions.assertEquals(expectedIndex, index)
                    listenerCalled = true
                })

        Assertions.assertEquals(expectedOldValue, observableMutableList.set(expectedIndex, expectedNewValue))
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun defaultConstructorTest() {
        val observableMutableList = ObservableMutableList<T>()
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
        val list = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList(list)
        Assertions.assertEquals(list.size, observableMutableList.size)
        for (i in 0 until list.size)
            Assertions.assertEquals(list[i], observableMutableList[i])

        assertNoListenersSpecified(observableMutableList)
    }

    @Test
    fun collectionCopyConstructorWithListenersTest() {
        val list = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
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
        val observableMutableList = ObservableMutableList<T>(initialCapacity)

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
    private fun defaultOnAdd(element: T, index: Int) {

    }

    @Suppress("UNUSED_PARAMETER")
    private fun defaultOnSet(oldElement: T, newElement: T, index: Int) {

    }

    @Suppress("UNUSED_PARAMETER")
    private fun defaultOnRemove(element: T, index: Int) {

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
        val value = getNewTestElement()
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.add(value)
        Assertions.assertTrue(observableMutableList.contains(value))
    }

    @Test
    fun negativeContainsTest() {
        val value = getNewTestElement()
        val observableMutableList = ObservableMutableList<T>()
        Assertions.assertFalse(observableMutableList.contains(value))
    }

    @Test
    fun positiveContainsAllTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)
        Assertions.assertTrue(observableMutableList.containsAll(values))
    }

    @Test
    fun negativeContainsAllTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        Assertions.assertFalse(observableMutableList.containsAll(values))
    }

    @Test
    fun getTest() {
        val value = getNewTestElement()
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.add(value)
        Assertions.assertEquals(value, observableMutableList[0])
    }

    @Test
    fun positiveIndexOfTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(1, observableMutableList.indexOf(values[1]))
    }

    @Test
    fun negativeIndexOfTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(-1, observableMutableList.indexOf(getNewTestElement()))
    }

    @Test
    fun positiveIsEmptyTest() {
        val observableMutableList = ObservableMutableList<T>()
        Assertions.assertTrue(observableMutableList.isEmpty())
    }

    @Test
    fun negativeIsEmptyTest() {
        val value = getNewTestElement()
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.add(value)
        Assertions.assertFalse(observableMutableList.isEmpty())
    }

    @Test
    fun iteratorTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
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
    fun listIteratorTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        val iterator = observableMutableList.listIterator()

        for (value in values) {
            Assertions.assertTrue(iterator.hasNext())
            Assertions.assertEquals(value, iterator.next())
        }

        Assertions.assertFalse(iterator.hasNext())
        Assertions.assertThrows(NoSuchElementException::class.java) { iterator.next() }
    }

    @Test
    fun listIteratorWithIndexTest() {
        val iteratorStartIndex = 1
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        val iterator = observableMutableList.listIterator(iteratorStartIndex)

        for (i in iteratorStartIndex until values.size) {
            Assertions.assertTrue(iterator.hasNext())
            Assertions.assertEquals(values[i], iterator.next())
        }

        Assertions.assertFalse(iterator.hasNext())
        Assertions.assertThrows(NoSuchElementException::class.java) { iterator.next() }
    }

    @Test
    fun positiveLastIndexOfTest() {
        val second = getNewTestElement()
        val values = listOf(getNewTestElement(), second, second, getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(2, observableMutableList.lastIndexOf(values[2]))
    }

    @Test
    fun negativeLastIndexOfTest() {
        val second = getNewTestElement()
        val values = listOf(getNewTestElement(), second, second, getNewTestElement())
        val observableMutableList = ObservableMutableList<T>()
        observableMutableList.addAll(values)

        Assertions.assertEquals(-1, observableMutableList.lastIndexOf(getNewTestElement()))
    }

    @Test
    fun subListTest() {
        val values = listOf(getNewTestElement(), getNewTestElement(), getNewTestElement(), getNewTestElement())
        val subListStart = 1
        val subListEnd = 3

        val observableMutableList = ObservableMutableList(
                values,
                this::defaultOnAdd,
                this::defaultOnSet,
                this::defaultOnRemove,
                this::defaultOnClear
        )

        val subList = observableMutableList.subList(subListStart, subListEnd)

        Assertions.assertEquals(subListEnd - subListStart, subList.size)
        for (value in subList.withIndex())
            Assertions.assertEquals(values[value.index + subListStart], value.value)

        Assertions.assertSame(observableMutableList.onAdd, subList.onAdd)
        Assertions.assertSame(observableMutableList.onSet, subList.onSet)
        Assertions.assertSame(observableMutableList.onRemove, subList.onRemove)
        Assertions.assertSame(observableMutableList.onClear, subList.onClear)
    }
}
