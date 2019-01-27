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
package com.github.vatbub.matchmaking.server.idprovider

import java.util.*
import kotlin.random.Random

/**
 * Implementation of [ConnectionIdProvider] which keeps a list of used connection ids in memory.
 * Therefore, connection ids are erased once the server is shut down and are not synced over multiple nodes
 * To be used when the server is only running on a single node.
 */
open class MemoryIdProvider : ConnectionIdProvider {
    override fun get(id: String): Id? {
        return _connectionIdsInUse[id]
    }

    private val _connectionIdsInUse = mutableMapOf<String, Id>()

    val connectionIdsInUse: Map<String, Id>
        get() {
            return Collections.unmodifiableMap(_connectionIdsInUse)
        }

    override fun getNewId(): Id {
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
        _connectionIdsInUse.put(connectionIdAsString, result)
        return result
    }

    override fun deleteId(id: String): Id? {
        return _connectionIdsInUse.remove(id)
    }

    override fun containsId(id: String?): Boolean {
        return _connectionIdsInUse.contains(id)
    }

    override fun reset() {
        _connectionIdsInUse.clear()
    }
}
