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
package com.github.vatbub.matchmaking.server.roomproviders.database

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class ConnectionPoolWrapper private constructor(
    connectionString: String,
    dbUser: String?,
    dbPassword: String?,
    connectionProperties: Properties?
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

    val connectionPoolDataSource = ComboPooledDataSource()
    var connectionCount = 0
        private set

    init {
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
            if (transaction.invoke(connection))
                connection.commit()
            else
                connection.rollback()
        } finally {
            connection.close()
        }
    }
}
