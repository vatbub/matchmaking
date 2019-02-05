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
package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import kotlin.properties.Delegates

/**
 * This class wraps a [Room] and allows other entities to subscribe to changes to the room.
 * This allows more efficient database interactions.
 * **Important:** The room data is copied within the constructor. Later changes to `fromRoom` will not be reflected by this class.
 * @param fromRoom The room too copy the data from
 */
class ObservableRoom(
    fromRoom: Room,
    var onGameStartedChange: ((Boolean) -> Unit)? = null
) {
    val id = fromRoom.id
    val hostUserConnectionId = fromRoom.hostUserConnectionId
    val configuredUserNameList = fromRoom.configuredUserNameList
    val configuredUserNameListMode = fromRoom.configuredUserNameListMode
    val minRoomSize = fromRoom.minRoomSize
    val maxRoomSize = fromRoom.maxRoomSize

    val connectedUsers = ObservableMutableList(fromRoom.connectedUsers)
    val gameState = ObservableGameData(fromRoom.gameState)

    var gameStarted: Boolean by Delegates.observable(false) { _, _, newValue -> onGameStartedChange?.invoke(newValue) }

    val dataToBeSentToTheHost = ObservableMutableList<GameData>()

    /**
     * Constructs a new [Room] object which contains all data of `this` object.
     */
    fun toRoom(): Room {
        val result =
            Room(id, hostUserConnectionId, configuredUserNameList, configuredUserNameListMode, minRoomSize, maxRoomSize)
        result.connectedUsers.clear()
        result.connectedUsers.addAll(connectedUsers)
        result.gameState = gameState.backingGameData
        result.gameStarted = gameStarted
        result.dataToBeSentToTheHost.clear()
        result.dataToBeSentToTheHost.addAll(dataToBeSentToTheHost)
        return result
    }
}

/**
 * This class wraps a [GameData] object and allows other entities to subscribe to changes it.
 * This allows more efficient database interactions.
 * **Important:** The [GameData] is copied within the constructor. Later changes to `fromGameData` will not be reflected by this class.
 * @param fromGameData The [GameData] too copy the data from
 */
class ObservableGameData(
    fromGameData: GameData,
    var onReplace: ((oldGameData: GameData, newGameData: GameData) -> Unit)? = null,
    var onSet: ((key: String, oldValue: Any?, newValue: Any) -> Unit)? = null,
    val onRemove: ((key: String, element: Any?) -> Unit)? = null
) {

    var backingGameData: GameData by Delegates.observable(fromGameData.copy()) { _, oldValue, newValue ->
        onReplace?.invoke(
            oldValue,
            newValue
        )
    }

    operator fun <T : Any> set(key: String, content: T) {
        val oldValue = backingGameData.get<T>(key)
        backingGameData[key] = content
        onSet?.invoke(key, oldValue, content)
    }

    operator fun <T : Any> get(key: String, defaultValue: T? = null, typeClass: Class<T>? = null): T? {
        return backingGameData[key, defaultValue, typeClass]
    }

    fun contains(key: String): Boolean {
        return backingGameData.contains(key)
    }

    fun <T : Any> remove(key: String): T? {
        val result = backingGameData.remove<T>(key)
        onRemove?.invoke(key, result)
        return result
    }
}

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
        c: MutableCollection<out E>?,
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
        val result = backingList.addAll(elements)

        val index = size
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
        return ObservableMutableList(onAdd, onSet, onRemove, onClear, backingList.subList(fromIndex, toIndex))
    }
}
