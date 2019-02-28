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

import com.github.vatbub.matchmaking.common.data.GameData
import java.time.Instant
import kotlin.properties.Delegates

/**
 * This class wraps a [GameData] object and allows other entities to subscribe to changes it.
 * This allows more efficient database interactions.
 * **Important:** The [GameData] is copied within the constructor. Later changes to `fromGameData` will not be reflected by this class.
 * @param fromGameData The [GameData] too copy the data from
 */
class ObservableGameData(
    fromGameData: GameData,
    var onSet: ((key: String, oldValue: Any?, newValue: Any) -> Unit)? = null,
    var onRemove: ((key: String, element: Any?) -> Unit)? = null,
    var onTimestampChanged: ((newTimestamp: Instant) -> Unit)? = null
) {
    val size: Int
        get() = backingGameData.size

    val backingGameData = fromGameData.copy()

    val createdByConnectionId = fromGameData.createdByConnectionId

    var createdAtUtc by Delegates.observable(fromGameData.createdAtUtc) { _, _, newValue ->
        backingGameData.createdAtUtc = newValue
        onTimestampChanged?.invoke(newValue)
    }

    fun replaceContents(newContents: GameData) {
        createdAtUtc = newContents.createdAtUtc
        for (key in backingGameData.keys) {
            remove<Any>(key)
        }
        for (key in newContents.keys) {
            set(key, newContents[key]!!)
        }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObservableGameData

        if (backingGameData != other.backingGameData) return false

        return true
    }

    override fun hashCode(): Int {
        return backingGameData.hashCode()
    }
}
