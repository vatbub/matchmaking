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
package com.github.vatbub.matchmaking.server.logic.idprovider

import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.roomproviders.database.ConnectionPoolWrapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.*

class JdbcIdProvider internal constructor(internal val connectionPoolWrapper: ConnectionPoolWrapper) :
        ConnectionIdProvider {
    constructor(connectionString: String) : this(ConnectionPoolWrapper(connectionString))
    constructor(connectionString: String, dbUser: String?, dbPassword: String?) : this(
            ConnectionPoolWrapper(
                    connectionString,
                    dbUser,
                    dbPassword
            )
    )

    constructor(connectionString: String, connectionProperties: Properties) : this(
            ConnectionPoolWrapper(
                    connectionString,
                    connectionProperties
            )
    )

    object ConnectionIDs : org.jetbrains.exposed.sql.Table("connectionids") {
        val id = varchar("id", 8).primaryKey()
        val passwordHash = varchar("passwordhash", 64)
    }

    init {
        transaction(connectionPoolWrapper.exposedDatabase) {
            SchemaUtils.createMissingTablesAndColumns(ConnectionIDs)
        }
    }

    override fun saveNewId(id: Id) {
        val hashedPassword = generateSha251Hash(id.password!!)

        transaction(connectionPoolWrapper.exposedDatabase) {
            ConnectionIDs.insert {
                it[ConnectionIDs.id] = id.connectionId!!
                it[passwordHash] = hashedPassword
            }
        }
    }

    override fun deleteId(id: String): Id? {
        logger.trace("Deleting an id...")
        val result = get(id)
        transaction(connectionPoolWrapper.exposedDatabase) {
            ConnectionIDs.deleteWhere {
                ConnectionIDs.id eq id
            }
        }
        return result
    }

    override fun get(id: String): Id? {
        var result: Id? = null

        transaction(connectionPoolWrapper.exposedDatabase) {
            logger.debug("Getting the id info for a specified connection id...")
            val query = ConnectionIDs.select { ConnectionIDs.id eq id }
            result = (if (query.empty()) null else Id(id, query.first()[ConnectionIDs.passwordHash]))
        }
        return result
    }

    override fun containsId(id: String?) = if (id == null) false else get(id) != null

    override fun reset() {
        transaction(connectionPoolWrapper.exposedDatabase) {
            ConnectionIDs.deleteAll()
        }
    }

    private fun generateSha251Hash(input: String): String {
        val hexChars = "0123456789ABCDEF"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val i = it.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }

    override fun isAuthorized(id: Id): AuthorizationResult {
        logger.debug("Checking authorization...")
        val result = isAuthorizedImpl(id)
        logger.debug("Authorization check result: $result")
        return result
    }

    private fun isAuthorizedImpl(id: Id): AuthorizationResult {
        if (id.connectionId == null)
            return AuthorizationResult.NotAuthorized
        val lookUpResult = this[id.connectionId] ?: return AuthorizationResult.NotFound
        if (id.password == null)
            return AuthorizationResult.NotAuthorized
        if (lookUpResult.password != generateSha251Hash(id.password))
            return AuthorizationResult.NotAuthorized
        return AuthorizationResult.Authorized
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JdbcIdProvider) return false

        if (connectionPoolWrapper != other.connectionPoolWrapper) return false

        return true
    }

    override fun hashCode(): Int {
        return connectionPoolWrapper.hashCode()
    }
}
