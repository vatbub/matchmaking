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
import com.github.vatbub.matchmaking.server.logic.roomproviders.database.*
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

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

    private val idsTable = Table(
        "connectionids", ColumnList(
            Column("id", Type.CHAR, listOf(8), listOf(PrimaryKeyConstraint())),
            Column("passwordhash", Type.VARCHAR, listOf(64))
        )
    )

    private val schema = Schema(listOf(idsTable))

    init {
        connectionPoolWrapper.getConnectionAndCommit { schema.createIfNecessary(it);true }
    }

    override fun getNewId(): Id {
        logger.trace("Creating a new id...")
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
        val hashedPassword = generateSha251Hash(result.password!!)

        connectionPoolWrapper.getConnectionAndCommit {
            idsTable.insertInto(
                it,
                result.connectionId,
                hashedPassword
            );true
        }
        return result
    }

    override fun deleteId(id: String): Id? {
        logger.trace("Deleting an id...")
        val result = get(id)
        connectionPoolWrapper.getConnectionAndCommit {
            val statement = it.prepareStatement("DELETE FROM ${idsTable.name} WHERE id = ?")
            statement.setString(1, id)
            statement.executeUpdate()
            true
        }
        return result
    }

    override fun get(id: String): Id? {
        var result: Id? = null

        connectionPoolWrapper.getConnectionAndCommit {
            logger.debug("Getting the id info for a specified connection id...")
            val statement =
                    it.prepareStatement("SELECT * FROM ${idsTable.name} WHERE id = ?")!!
            statement.setString(1, id)
            val resultSet = statement.executeQuery()!!
            if (resultSet.next())
                result = Id(id, resultSet.getString(2))
            true
        }

        return result
    }

    override fun containsId(id: String?): Boolean {
        var result = false
        connectionPoolWrapper.getConnectionAndCommit {
            val statement = it.prepareStatement("SELECT * FROM ${idsTable.name} WHERE id = ?")!!
            statement.setString(1, id)
            result = statement.executeQuery().next()
            true
        }
        return result
    }

    override fun reset() {
        connectionPoolWrapper.getConnectionAndCommit {
            val statement = it.createStatement()
            statement.executeUpdate("DELETE FROM ${idsTable.name}")
            true
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
