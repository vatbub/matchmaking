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
package com.github.vatbub.matchmaking.common

import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

object InteractionConverter {
    fun serialize(serverInteraction: ServerInteraction): String = serverInteraction.toJson()

    fun <T : ServerInteraction> deserialize(json: String): T {
        val className: String
        try {
            val jsonTree = JsonParser.parseString(json)
            className = jsonTree.asJsonObject["className"]?.asString
                    ?: throw IllegalArgumentException("Illegal json string submitted: classname could not be found.")
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("Illegal json string submitted: Could not convert to JsonObject", e)
        } catch (e: UnsupportedOperationException) {
            throw IllegalArgumentException("Illegal json string submitted: className is not a string", e)
        }

        val clazz = Class.forName(className)
        return fromJson<T>(json, clazz)
    }
}
