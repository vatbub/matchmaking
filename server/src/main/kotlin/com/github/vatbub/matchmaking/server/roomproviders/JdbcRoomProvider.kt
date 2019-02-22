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
package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.UserListMode
import com.github.vatbub.matchmaking.server.roomproviders.data.RoomTransaction
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.DriverManager
import java.sql.Types
import java.util.*

class JdbcRoomProvider private constructor(
    connectionString: String,
    dbUser: String?,
    dbPassword: String?,
    connectionProperties: Properties?
) : RoomProvider() {
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

    private val connectionPoolDataSource = ComboPooledDataSource()

    private val roomIdLength = 8
    private val varcharMax = 500

    private val roomsTableName = "rooms"
    private val roomIdColumnName = "id"
    private val hostUserConnectionIdColumnName = "host_user_connection_id"
    private val configuredUserNameListColumnName = "configured_user_name_list"
    private val configuredUserNameListModeColumnName = "configured_user_name_list_mode"
    private val minRoomSizeColumnName = "min_room_size"
    private val maxRoomSizeColumnName = "max_room_size"
    private val gameStateIdColumnName = "game_state_id"
    private val gameStartedColumnName = "game_started"

    private val usersTableName = "users"
    private val usersConnectionIdColumnName = "connection_id"
    private val usersUserNameColumnName = "username"
    private val usersIpv4ColumnName = "ipv4"
    private val usersIpv6ColumnName = "ipv6"
    private val usersConnectedToRoomColumnName = "connected_to"

    private val gameDataTableName = "game_data"
    private val gameDataIdColumnName = "id"

    private val gameDataContentsTableName = "game_data_contents"
    private val gameDataContentsIdColumnName = "id"
    private val gameDataContentsBelongsToGameDataColumnName = "belongs_to_game_data"
    private val gameDataContentsKeyColumnName = "key"
    private val gameDataContentsValueColumnName = "value"

    private val dataToBeSentToHostTableName = "data_to_be_sent_to_host"
    private val dataToBeSentToHostIdColumnName = "id"
    private val dataToBeSentToHostBelongsToRoomColumnName = "belongs_to_room"
    private val dataToBeSentToHostGameDataIdColumnName = "game_data_id"

    init {
        val driver = DriverManager.getDriver(connectionString)!!
        connectionPoolDataSource.driverClass = driver.javaClass.name
        connectionPoolDataSource.jdbcUrl = connectionString
        if (dbUser != null)
            connectionPoolDataSource.user = dbUser
        if (dbPassword != null)
            connectionPoolDataSource.password = dbPassword
        if (connectionProperties != null)
            connectionPoolDataSource.properties = connectionProperties

        if (!checkSchemaIntegrity())
            initializeSchema()
    }

    private fun checkSchemaIntegrity(): Boolean {
        if (!hasTable(roomsTableName))
            return false
        if (!hasTable(usersTableName))
            return false
        if (!hasTable(gameDataTableName))
            return false
        if (!hasTable(gameDataContentsTableName))
            return false
        if (!hasTable(dataToBeSentToHostTableName))
            return false

        if (!hasColumn(roomsTableName, roomIdColumnName, Types.CHAR)) return false
        if (!hasColumn(roomsTableName, hostUserConnectionIdColumnName, Types.VARCHAR)) return false
        if (!hasColumn(roomsTableName, configuredUserNameListColumnName, Types.VARCHAR)) return false
        if (!hasColumn(roomsTableName, configuredUserNameListModeColumnName, Types.INTEGER)) return false
        if (!hasColumn(roomsTableName, minRoomSizeColumnName, Types.INTEGER)) return false
        if (!hasColumn(roomsTableName, maxRoomSizeColumnName, Types.INTEGER)) return false
        if (!hasColumn(roomsTableName, gameStateIdColumnName, Types.INTEGER)) return false
        if (!hasColumn(roomsTableName, gameStartedColumnName, Types.BOOLEAN)) return false

        if (!hasColumn(usersTableName, usersConnectionIdColumnName, Types.VARCHAR)) return false
        if (!hasColumn(usersTableName, usersUserNameColumnName, Types.VARCHAR)) return false
        if (!hasColumn(usersTableName, usersIpv4ColumnName, Types.VARCHAR)) return false
        if (!hasColumn(usersTableName, usersIpv6ColumnName, Types.VARCHAR)) return false
        if (!hasColumn(usersTableName, usersConnectedToRoomColumnName, Types.CHAR)) return false

        if (!hasColumn(gameDataTableName, gameDataIdColumnName, Types.INTEGER)) return false

        if (!hasColumn(gameDataContentsTableName, gameDataContentsIdColumnName, Types.INTEGER)) return false
        if (!hasColumn(
                gameDataContentsTableName,
                gameDataContentsBelongsToGameDataColumnName,
                Types.INTEGER
            )
        ) return false
        if (!hasColumn(gameDataContentsTableName, gameDataContentsKeyColumnName, Types.VARCHAR)) return false
        if (!hasColumn(gameDataContentsTableName, gameDataContentsValueColumnName, Types.VARCHAR)) return false

        if (!hasColumn(dataToBeSentToHostTableName, dataToBeSentToHostIdColumnName, Types.INTEGER)) return false
        if (!hasColumn(dataToBeSentToHostTableName, dataToBeSentToHostBelongsToRoomColumnName, Types.CHAR)) return false
        if (!hasColumn(dataToBeSentToHostTableName, dataToBeSentToHostGameDataIdColumnName, Types.INTEGER)) return false

        return true
    }

    private fun hasTable(tableName: String): Boolean {
        val tableResultSet = connectionPoolDataSource.connection.metaData.getTables(null, null, "%$tableName%", null)!!
        return tableResultSet.next()
    }

    private fun hasColumn(tableName: String, columnName: String, expectedDataType: Int): Boolean {
        val columnResultSet =
            connectionPoolDataSource.connection.metaData.getColumns(null, null, "%$tableName%", "%$columnName%")!!
        val type = columnResultSet.getInt("DATA_TYPE")
        return columnResultSet.next() && expectedDataType == type
    }

    // TODO: CASCADING CHANGES
    private fun initializeSchema() {
        val connection = connectionPoolDataSource.connection!!
        val autoCommit = connection.autoCommit
        connection.autoCommit = false
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $roomsTableName")
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $usersTableName")
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $gameDataTableName")
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $gameDataContentsTableName")
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS $dataToBeSentToHostTableName")

        connection.createStatement()
            .executeUpdate("CREATE TABLE $roomsTableName ($roomIdColumnName CHAR($roomIdLength) PRIMARY KEY, $hostUserConnectionIdColumnName VARCHAR($varcharMax), $configuredUserNameListColumnName VARCHAR($varcharMax), $configuredUserNameListModeColumnName INT, $minRoomSizeColumnName INT, $maxRoomSizeColumnName INT, $gameStateIdColumnName INT REFERENCES $gameDataTableName($gameDataIdColumnName), $gameStartedColumnName BOOLEAN)")
        connection.createStatement()
            .executeUpdate("CREATE TABLE $usersTableName ($usersConnectionIdColumnName VARCHAR($varcharMax) PRIMARY KEY, $usersUserNameColumnName VARCHAR($varcharMax), $usersIpv4ColumnName VARCHAR($varcharMax), $usersIpv6ColumnName VARCHAR($varcharMax), $usersConnectedToRoomColumnName CHAR($roomIdLength))")
        connection.createStatement()
            .executeUpdate("CREATE TABLE $gameDataTableName ($gameDataIdColumnName INT PRIMARY KEY)")
        connection.createStatement()
            .executeUpdate("CREATE TABLE $gameDataContentsTableName ($gameDataContentsIdColumnName INT PRIMARY KEY, $gameDataContentsBelongsToGameDataColumnName INT, $gameDataContentsKeyColumnName VARCHAR($varcharMax), $gameDataContentsValueColumnName VARCHAR($varcharMax))")
        connection.createStatement()
            .executeUpdate("CREATE TABLE $dataToBeSentToHostTableName ($dataToBeSentToHostIdColumnName INT PRIMARY KEY, $dataToBeSentToHostBelongsToRoomColumnName CHAR($roomIdLength), $dataToBeSentToHostGameDataIdColumnName INT)")

        connection.commit()
        connection.autoCommit = autoCommit
    }

    override val supportsConcurrentTransactionsOnSameRoom = true

    override fun createNewRoom(
        hostUserConnectionId: String,
        configuredUserNameList: List<String>?,
        configuredUserNameListMode: UserListMode,
        minRoomSize: Int,
        maxRoomSize: Int
    ): Room {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(id: String): Room? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun beginTransactionWithRoom(id: String): RoomTransaction? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun commitTransaction(roomTransaction: RoomTransaction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteRoom(id: String): Room? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearRooms() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsRoom(id: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAllRooms(): Collection<Room> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
