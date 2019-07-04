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
package com.github.vatbub.matchmaking.server.logic.roomproviders.database

import com.github.vatbub.matchmaking.common.logger
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.jetbrains.exposed.sql.Database
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class ConnectionPoolWrapper private constructor(
        private val connectionString: String,
        private val dbUser: String?,
        private val dbPassword: String?,
        private val connectionProperties: Properties?
) {
    constructor(connectionString: String) : this(connectionString, null, null, null)
    constructor(connectionString: String, dbUser: String?, dbPassword: String?) : this(
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

    private val connectionPoolDataSource = ComboPooledDataSource()
    val exposedDatabase by lazy { Database.connect(connectionPoolDataSource) }
    var connectionCount = 0
        private set

    init {
        logger.debug("Creating a new database connection...")
        val driver = DriverManager.getDriver(connectionString)!!
        // connectionPoolDataSource.maxPoolSize = 999999999
        connectionPoolDataSource.maxPoolSize = 5
        connectionPoolDataSource.acquireIncrement = 1
        // connectionPoolDataSource.maxPoolSize = 5
        connectionPoolDataSource.driverClass = driver.javaClass.name
        connectionPoolDataSource.jdbcUrl = connectionString
        if (dbUser != null)
            connectionPoolDataSource.user = dbUser
        if (dbPassword != null)
            connectionPoolDataSource.password = dbPassword
        if (connectionProperties != null)
            connectionPoolDataSource.properties = connectionProperties
    }

    fun getConnection(): Connection {
        logger.debug("Getting a connection from the pool...")
        val connection = connectionPoolDataSource.connection
        connection.autoCommit = false
        connectionCount++
        return ConnectionWrapper(connection) {
            connectionCount--
        }
    }

    fun getConnectionAndCommit(transaction: ((connection: Connection) -> Boolean)) {
        val connection = getConnection()
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            if (transaction.invoke(connection)) {
                logger.debug("Committing the transaction...")
                connection.commit()
            } else {
                logger.debug("Rolling the database transaction back...")
                connection.rollback()
            }
        } finally {
            connection.close()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionPoolWrapper

        if (connectionString != other.connectionString) return false
        if (dbUser != other.dbUser) return false
        if (dbPassword != other.dbPassword) return false
        if (connectionProperties != other.connectionProperties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = connectionString.hashCode()
        result = 31 * result + (dbUser?.hashCode() ?: 0)
        result = 31 * result + (dbPassword?.hashCode() ?: 0)
        result = 31 * result + (connectionProperties?.hashCode() ?: 0)
        return result
    }
}
