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

/**
 * A list which allows other entities to subscribe to changes
 */
class ObservableMutableList<E> private constructor(
    var onAdd: ((element: E, index: Int) -> Unit)?,
    var onSet: ((oldElement: E, newElement: E, index: Int) -> Unit)?,
    var onRemove: ((element: E, index: Int) -> Unit)?,
    var onClear: (() -> Unit)?,
    private val backingList: MutableList<E>
) : MutableList<E> {
    constructor(
        initialCapacity: Int,
        onAdd: ((element: E, index: Int) -> Unit)? = null,
        onSet: ((oldElement: E, newElement: E, index: Int) -> Unit)? = null,
        onRemove: ((element: E, index: Int) -> Unit)? = null,
        onClear: (() -> Unit)? = null
    ) : this(
        onAdd,
        onSet,
        onRemove,
        onClear,
        ArrayList(initialCapacity)
    )

    constructor(
        c: Collection<E>?,
        onAdd: ((element: E, index: Int) -> Unit)? = null,
        onSet: ((oldElement: E, newElement: E, index: Int) -> Unit)? = null,
        onRemove: ((element: E, index: Int) -> Unit)? = null,
        onClear: (() -> Unit)? = null
    ) : this(onAdd, onSet, onRemove, onClear, ArrayList(c))

    constructor(
        onAdd: ((element: E, index: Int) -> Unit)? = null,
        onSet: ((oldElement: E, newElement: E, index: Int) -> Unit)? = null,
        onRemove: ((element: E, index: Int) -> Unit)? = null,
        onClear: (() -> Unit)? = null
    ) : this(
        onAdd,
        onSet,
        onRemove,
        onClear,
        ArrayList()
    )

    override val size: Int
        get() = backingList.size

    override fun contains(element: E): Boolean {
        return backingList.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return backingList.containsAll(elements)
    }

    override fun get(index: Int): E {
        return backingList[index]
    }

    override fun indexOf(element: E): Int {
        return backingList.indexOf(element)
    }

    override fun isEmpty(): Boolean {
        return backingList.isEmpty()
    }

    override fun iterator(): MutableIterator<E> {
        return backingList.iterator()
    }

    override fun lastIndexOf(element: E): Int {
        return backingList.lastIndexOf(element)
    }

    override fun add(element: E): Boolean {
        val newIndex = size
        val result = backingList.add(element)
        onAdd?.invoke(element, newIndex)
        return result
    }

    override fun add(index: Int, element: E) {
        backingList.add(index, element)
        onAdd?.invoke(element, index)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        val result = backingList.addAll(index, elements)

        for ((i, element) in elements.withIndex()) {
            onAdd?.invoke(element, index + i)
        }

        return result
    }

    override fun addAll(elements: Collection<E>): Boolean {
        val index = size
        val result = backingList.addAll(elements)
        for ((i, element) in elements.withIndex()) {
            onAdd?.invoke(element, index + i)
        }
        return result
    }

    override fun clear() {
        backingList.clear()
        onClear?.invoke()
    }

    override fun listIterator(): MutableListIterator<E> {
        return backingList.listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<E> {
        return backingList.listIterator(index)
    }

    override fun remove(element: E): Boolean {
        val index = indexOf(element)
        if (!backingList.remove(element))
            return false
        onRemove?.invoke(element, index)
        return true
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        val deletedElements = mutableMapOf<Int, E>()
        for (element in elements) {
            val index = indexOf(element)
            if (index >= 0) continue
            deletedElements[index] = element
        }

        val result = backingList.removeAll(elements)

        for (deletedElement in deletedElements) {
            onRemove?.invoke(deletedElement.value, deletedElement.key)
        }

        return result
    }

    override fun removeAt(index: Int): E {
        val result = backingList.removeAt(index)
        onRemove?.invoke(result, index)
        return result
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        val deletedElements = mutableMapOf<Int, E>()
        for ((index, element) in backingList.withIndex()) {
            if (!elements.contains(element))
                continue

            deletedElements[index] = element
        }

        val result = backingList.retainAll(elements)

        for (deletedElement in deletedElements) {
            onRemove?.invoke(deletedElement.value, deletedElement.key)
        }

        return result
    }

    override fun set(index: Int, element: E): E {
        val result = backingList.set(index, element)
        onSet?.invoke(result, element, index)
        return result
    }

    override fun subList(fromIndex: Int, toIndex: Int): ObservableMutableList<E> {
        return ObservableMutableList(
            onAdd,
            onSet,
            onRemove,
            onClear,
            backingList.subList(fromIndex, toIndex)
        )
    }
}
