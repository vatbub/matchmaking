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

import com.google.gson.GsonBuilder
import java.time.Instant
import java.time.ZoneOffset

/**
 * Contains the current state of the game as defined by the game host.
 * The data in this class can be stored and retrieved by using key-value-pairs.
 * In theory, this class can store data of any type (including your own types), but it has been tested with the following:
 * - [Byte]
 * - [ByteArray]
 * - [Char]
 * - [CharArray]
 * - [String]
 * - Arrays of [String]s
 * - [List]<[String]>
 * - [Float]
 * - [FloatArray]
 * - [List]<[Integer]>
 * - [Short]
 * - [ShortArray]
 */
class GameData(val createdByConnectionId: String, val contents: MutableMap<String, Any>) {
    constructor(createdByConnectionId: String) : this(createdByConnectionId, mutableMapOf())
    @Deprecated("For internal use only", ReplaceWith("GameData(createdByConnectionId)"), DeprecationLevel.HIDDEN)
    constructor() : this("")

    companion object {
        @JvmStatic
        private val gson = GsonBuilder().setPrettyPrinting().create()
    }

    var createdAtUtc = Instant.now().atOffset(ZoneOffset.UTC).toInstant()!!

    val keys
        get() = contents.keys
    val values
        get() = contents.values

    val size: Int
        get() = contents.size

    /**
     * Stores the specified value together with its key.
     * @param key The key to be used to store the data.
     * @param content The data associated with the key.
     */
    operator fun <T : Any> set(key: String, content: T) {
        contents[key] = content
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * **Important: Type safety can only be guaranteed if [typeClass] is specified. If [typeClass] is not specified
     * and the value associated with the key is not assignable to [T], a [ClassCastException] will be thrown.**
     * @param key The key of the data to get.
     * @param defaultValue The value to be returned if the key is not found or if the data associated with the key is
     * not of type [T] (see the note on type safety above)
     * @param typeClass The [Class] that represents the type of data to be returned. This must be specified in order to
     * guarantee type safety (see the note above)
     * **Important: If you expect a primitive value, use [expectedPrimitiveType]
     * instead!**
     * @param expectedPrimitiveType Same purpose as [typeClass], but to be used if a primitive type is expected
     * @return The data associated with the key or [defaultValue] if the key was not found or the data associated with
     * the key is not of type [T] (see the note on type safety above) or `null` if [defaultValue] is not specified.
     */
    operator fun <T : Any> get(key: String, defaultValue: T? = null, typeClass: Class<T>? = null, expectedPrimitiveType: JavaPrimitive? = null): T? {
        val result = contents[key] ?: return defaultValue
        if (expectedPrimitiveType?.isAssignableFrom(result) == false)
            return defaultValue
        if (typeClass?.isAssignableFrom(result.javaClass) == false)
            return defaultValue

        @Suppress("UNCHECKED_CAST")
        return result as T?

    }

    /**
     * Checks whether the specified key has any data associated with it.
     * @param key The key to be looked up.
     * @return `true` if the key has any data associated with it, `false` otherwise.
     */
    fun contains(key: String): Boolean {
        return contents.containsKey(key)
    }


    /**
     * Removes the specified key and its corresponding value from this map.
     *
     * @return the previous value associated with the key, or `null` if the key was not present in the map.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> remove(key: String): T? {
        return contents.remove(key) as T?
    }

    fun copy(): GameData {
        val result = GameData(createdByConnectionId, HashMap(contents))
        result.createdAtUtc = createdAtUtc
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameData

        if (createdByConnectionId != other.createdByConnectionId) return false
        if (contents != other.contents) return false
        if (createdAtUtc != other.createdAtUtc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createdByConnectionId.hashCode()
        result = 31 * result + contents.hashCode()
        result = 31 * result + createdAtUtc.hashCode()
        return result
    }

    override fun toString(): String {
        return gson.toJson(this)
    }
}

enum class JavaPrimitive {
    Byte, Short, Int, Long, Float, Double, Boolean, Char;

    fun isAssignableFrom(value: Any) = when (this) {
        Byte -> value is kotlin.Byte
        Short -> value is kotlin.Short
        Int -> value is kotlin.Int
        Long -> value is kotlin.Long
        Float -> value is kotlin.Float
        Double -> value is kotlin.Double
        Boolean -> value is kotlin.Boolean
        Char -> value is kotlin.Char
    }
}
