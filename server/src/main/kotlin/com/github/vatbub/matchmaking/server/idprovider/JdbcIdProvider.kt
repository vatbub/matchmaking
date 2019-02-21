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
package com.github.vatbub.matchmaking.server.idprovider

import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types
import java.util.*
import kotlin.random.Random

class JdbcIdProvider private constructor(
    connectionString: String,
    dbUser: String?,
    dbPassword: String?,
    connectionProperties: Properties?
) :
    ConnectionIdProvider {
    constructor(connectionString: String) : this(connectionString, null, null, null)
    constructor(connectionString: String, dbUser: String, dbPassword: String) : this(
        connectionString,
        dbUser,
        dbPassword,
        null
    )

    constructor(connectionString: String, connectionProperties: Properties) : this(
        connectionString,
        null,
        null,
        connectionProperties
    )

    private val connection: Connection = if (connectionProperties != null)
        DriverManager.getConnection(connectionString, connectionProperties)!!
    else
        DriverManager.getConnection(connectionString, dbUser, dbPassword)!!

    private val tableName = "connectionids"
    private val idColumnName = "id"
    private val passwordHashColumnName = "passwordhash"

    /*
    Schema:
    connectionIds { id: char(8) primary key; passwordHash: char(8)}
     */

    init {
        connection.autoCommit = false
        if (!checkSchemaIntegrity())
            initializeSchema()
    }

    private fun checkSchemaIntegrity(): Boolean {
        val tables = connection.metaData.getTables(null, null, "%$tableName%", null)!!
        if (!tables.next())
            return false

        val columns = connection.metaData.getColumns(null, null, tableName, "%")!!
        var idColumnFound = false
        var passwordHashColumnFound = false
        var columnCount = 0
        while (columns.next()) {
            columnCount++
            if (columns.getString("COLUMN_NAME") == idColumnName) {
                idColumnFound = true
                if (columns.getInt("DATA_TYPE") != Types.CHAR)
                    return false
            }
            if (columns.getString("COLUMN_NAME") == passwordHashColumnName) {
                passwordHashColumnFound = true
                if (columns.getInt("DATA_TYPE") != Types.VARCHAR)
                    return false
            }
        }

        return idColumnFound && passwordHashColumnFound && columnCount == 2
    }

    private fun initializeSchema() {
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $tableName")
        connection.createStatement()
            .executeUpdate("CREATE TABLE $tableName ( $idColumnName CHAR(8) PRIMARY KEY, $passwordHashColumnName VARCHAR(64) )")
        connection.commit()
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
        val hashedPassword = generateSha251Hash(result.password!!)

        val statement = connection.createStatement()
        statement.executeUpdate("INSERT INTO $tableName ($idColumnName, $passwordHashColumnName) VALUES ('${result.connectionId}', '$hashedPassword')")
        connection.commit()

        return result
    }

    override fun deleteId(id: String): Id? {
        val result = get(id)
        val statement = connection.prepareStatement("DELETE FROM $tableName WHERE id = ?")
        statement.setString(1, id)
        statement.executeUpdate()
        connection.commit()
        return result
    }

    override fun get(id: String): Id? {
        val statement = connection.prepareStatement("SELECT $passwordHashColumnName FROM $tableName WHERE id = ?")!!
        statement.setString(1, id)
        val resultSet = statement.executeQuery()!!
        if (resultSet.next())
            return Id(id, resultSet.getString("passwordHash"))
        return null
    }

    override fun containsId(id: String?): Boolean {
        val statement = connection.prepareStatement("SELECT * FROM $tableName WHERE id = ?")!!
        statement.setString(1, id)
        return statement.executeQuery().next()
    }

    override fun reset() {
        val statement = connection.createStatement()
        statement.executeUpdate("DELETE FROM $tableName")
        connection.commit()
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
        if (id.connectionId == null)
            return AuthorizationResult.NotAuthorized
        val lookUpResult = this[id.connectionId] ?: return AuthorizationResult.NotFound
        if (lookUpResult.password == null && id.password == null)
            return AuthorizationResult.Authorized
        if (lookUpResult.password == null || id.password == null)
            return AuthorizationResult.NotAuthorized

        if (lookUpResult.password != generateSha251Hash(id.password))
            return AuthorizationResult.NotAuthorized
        return AuthorizationResult.Authorized
    }
}
