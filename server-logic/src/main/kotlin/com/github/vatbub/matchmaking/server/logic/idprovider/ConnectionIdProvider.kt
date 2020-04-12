/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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
package com.github.vatbub.matchmaking.server.logic.idprovider

import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.idprovider.AuthorizationResult.*
import kotlin.random.Random

/**
 * Provides and stores connection ids.
 * Implementations are expected to produce connection ids and store them for later use, e. g. in memory or in a database.
 * See [MemoryIdProvider] for a reference implementation
 */
interface ConnectionIdProvider {

    /**
     * Called when a new connection id is requested. The implementation is expected to store the generated connection id
     * automatically
     */
    fun getNewId(): Id {
        logger.trace { "Creating a new id..." }
        var connectionIdAsString: String
        do {
            var connectionId = Random.nextInt()
            if (connectionId < 0)
                connectionId = -connectionId

            connectionIdAsString = connectionId.toString(16)
        } while (containsId(connectionIdAsString))

        var passwordAsInt = Random.nextInt()
        if (passwordAsInt < 0)
            passwordAsInt = -passwordAsInt

        val result = Id(connectionIdAsString, passwordAsInt.toString(16))
        saveNewId(result)
        return result
    }

    /**
     * Saves the new id. This method is called by [getNewId] and must not be called directly. Use [getNewId] instead.
     * @see [getNewId]
     * @param id The id to save
     */
    fun saveNewId(id: Id)

    /**
     * Deletes the specified id if it exists
     * @return The deleted id or `null` if it didn't exist
     */
    fun deleteId(id: String): Id?

    /**
     * Deletes the specified id if it exists
     * @return The deleted id or `null` if it didn't exist
     */
    fun deleteId(id: Id): Id? {
        if (id.connectionId == null)
            return null
        return deleteId(id.connectionId)
    }

    operator fun get(id: String): Id?

    fun containsId(id: String?): Boolean

    /**
     * Deletes all known ids
     */
    fun reset()

    fun isAuthorized(id: Id): AuthorizationResult {
        logger.debug { "Checking authorization..." }
        val result = isAuthorizedImpl(id)
        logger.debug { "Authorization check result: $result" }
        return result
    }

    private fun isAuthorizedImpl(id: Id): AuthorizationResult {
        if (id.connectionId == null)
            return NotAuthorized
        val lookUpResult = this[id.connectionId] ?: return NotFound
        if (lookUpResult.password != id.password)
            return NotAuthorized
        return Authorized
    }
}

data class Id(val connectionId: String?, val password: String?)

enum class AuthorizationResult {
    NotFound, Authorized, NotAuthorized
}
