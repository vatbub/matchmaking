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

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.UserListMode
import com.github.vatbub.matchmaking.server.roomproviders.data.ObservableRoom
import com.github.vatbub.matchmaking.server.roomproviders.data.RoomTransaction
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types
import java.util.*
import kotlin.random.Random

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

    private val pendingTransactions = mutableMapOf<RoomTransaction, Connection>()

    private val roomIdLength = 8
    private val varcharMax = 500
    private val nullString = "NULL"
    private val userListDelimiter = ","

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
        connectionPoolDataSource.maxPoolSize = 999999999
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

    private fun getConnection(): Connection {
        val connection = connectionPoolDataSource.connection
        connection.autoCommit = false
        return connection
    }

    private fun getConnectionAndCommit(transaction: ((connection: Connection) -> Boolean)) {
        val connection = getConnection()
        if (transaction.invoke(connection))
            connection.commit()
        else
            connection.rollback()
    }

    // TODO: CASCADING CHANGES
    private fun initializeSchema() {
        getConnectionAndCommit {
            it.createStatement().executeUpdate("DROP TABLE IF EXISTS $roomsTableName")
            it.createStatement().executeUpdate("DROP TABLE IF EXISTS $usersTableName")
            it.createStatement().executeUpdate("DROP TABLE IF EXISTS $gameDataTableName")
            it.createStatement().executeUpdate("DROP TABLE IF EXISTS $gameDataContentsTableName")
            it.createStatement().executeUpdate("DROP TABLE IF EXISTS $dataToBeSentToHostTableName")

            it.createStatement()
                .executeUpdate("CREATE TABLE $gameDataTableName ($gameDataIdColumnName INT PRIMARY KEY)")
            it.createStatement()
                .executeUpdate("CREATE TABLE $roomsTableName ($roomIdColumnName CHAR($roomIdLength) PRIMARY KEY, $hostUserConnectionIdColumnName VARCHAR($varcharMax), $configuredUserNameListColumnName VARCHAR($varcharMax), $configuredUserNameListModeColumnName INT, $minRoomSizeColumnName INT, $maxRoomSizeColumnName INT, $gameStateIdColumnName INT REFERENCES $gameDataTableName($gameDataIdColumnName) ON DELETE CASCADE, $gameStartedColumnName INT)")
            it.createStatement()
                .executeUpdate("CREATE TABLE $usersTableName ($usersConnectionIdColumnName VARCHAR($varcharMax) PRIMARY KEY, $usersUserNameColumnName VARCHAR($varcharMax), $usersIpv4ColumnName VARCHAR($varcharMax), $usersIpv6ColumnName VARCHAR($varcharMax), $usersConnectedToRoomColumnName CHAR($roomIdLength) REFERENCES $roomsTableName($roomIdColumnName) ON DELETE CASCADE)")
            it.createStatement()
                .executeUpdate("CREATE TABLE $gameDataContentsTableName ($gameDataContentsIdColumnName INT PRIMARY KEY, $gameDataContentsBelongsToGameDataColumnName INT REFERENCES $gameDataTableName($gameDataIdColumnName) ON DELETE CASCADE, $gameDataContentsKeyColumnName VARCHAR($varcharMax), $gameDataContentsValueColumnName VARCHAR($varcharMax))")
            it.createStatement()
                .executeUpdate("CREATE TABLE $dataToBeSentToHostTableName ($dataToBeSentToHostIdColumnName INT PRIMARY KEY, $dataToBeSentToHostBelongsToRoomColumnName CHAR($roomIdLength) REFERENCES $roomsTableName($roomIdColumnName) ON DELETE CASCADE, $dataToBeSentToHostGameDataIdColumnName INT REFERENCES $gameDataTableName($gameDataIdColumnName) ON DELETE CASCADE)")
            true
        }
    }

    override val supportsConcurrentTransactionsOnSameRoom = true

    private fun createNewGameData(connection: Connection): Int {
        var gameDataId = 0
        do {
            gameDataId++
        } while (connection.createStatement().executeQuery("SELECT * FROM $gameDataTableName WHERE $gameDataIdColumnName = $gameDataId").next())
        connection.createStatement()
            .executeUpdate("INSERT INTO $gameDataTableName ($gameDataIdColumnName) VALUES ($gameDataId)")
        return gameDataId
    }

    private fun encodeUserList(userList: List<String>?): String {
        if (userList == null)
            return nullString
        return userList.joinToString(userListDelimiter)
    }

    private fun decodeUserList(userListString: String): List<String>? {
        if (userListString == nullString)
            return null
        return userListString.split(userListDelimiter)
    }

    private fun getRoom(id: String, connection: Connection): Room? {
        val queryResult =
            connection.createStatement().executeQuery("SELECT * FROM $roomsTableName WHERE $roomIdColumnName = '$id'")
        if (!queryResult.next())
            return null

        val result = Room(
            id,
            queryResult.getString(hostUserConnectionIdColumnName),
            decodeUserList(queryResult.getString(configuredUserNameListColumnName)),
            UserListMode.values()[queryResult.getInt(configuredUserNameListModeColumnName)],
            queryResult.getInt(minRoomSizeColumnName),
            queryResult.getInt(maxRoomSizeColumnName)
        )
        result.connectedUsers.addAll(getConnectedUsers(id, connection))
        result.gameState = getGameData(queryResult.getInt(gameStateIdColumnName), connection)
        result.gameStarted = queryResult.getInt(gameStartedColumnName) != 0
        result.dataToBeSentToTheHost.addAll(getDataToBeSentToHost(id, connection))
        return result
    }

    private fun getConnectedUsers(roomId: String, connection: Connection): List<User> {
        val queryResult = connection.createStatement()
            .executeQuery("SELECT * FROM $usersTableName WHERE $usersConnectedToRoomColumnName = '$roomId'")
        val result = mutableListOf<User>()
        while (queryResult.next()) {
            result.add(
                User(
                    queryResult.getString(usersConnectionIdColumnName), queryResult.getString(usersUserNameColumnName),
                    InetAddress.getByName(queryResult.getString(usersIpv4ColumnName)) as Inet4Address,
                    InetAddress.getByName(queryResult.getString(usersIpv6ColumnName)) as Inet6Address
                )
            )
        }
        return result
    }

    private fun getGameData(gameDataId: Int, connection: Connection): GameData {
        val queryResult = connection.createStatement()
            .executeQuery("SELECT * FROM $gameDataContentsTableName WHERE $gameDataContentsBelongsToGameDataColumnName = $gameDataId")
        val gameData = GameData()
        while (queryResult.next()) {
            gameData[queryResult.getString(gameDataContentsKeyColumnName)] =
                queryResult.getString(gameDataContentsValueColumnName)
        }
        return gameData
    }

    private fun getDataToBeSentToHost(roomId: String, connection: Connection): List<GameData> {
        val queryResult = connection.createStatement()
            .executeQuery("SELECT * FROM $dataToBeSentToHostTableName WHERE $dataToBeSentToHostBelongsToRoomColumnName = '$roomId'")
        val result = mutableListOf<GameData>()
        while (queryResult.next()) {
            result.add(getGameData(queryResult.getInt(dataToBeSentToHostGameDataIdColumnName), connection))
        }
        return result
    }

    override fun createNewRoom(
        hostUserConnectionId: String,
        configuredUserNameList: List<String>?,
        configuredUserNameListMode: UserListMode,
        minRoomSize: Int,
        maxRoomSize: Int
    ): Room {
        var room: Room? = null
        getConnectionAndCommit {
            var roomIdAsString: String
            do {
                var roomId = Random.nextInt()
                if (roomId < 0)
                    roomId = -roomId

                roomIdAsString = roomId.toString(16)
            } while (containsRoom(roomIdAsString))

            val gameStateId = createNewGameData(it)
            val encodedUserList = encodeUserList(configuredUserNameList)
            val encodedUserListMode = configuredUserNameListMode.ordinal
            it.createStatement()
                .executeUpdate("INSERT INTO $roomsTableName ($roomIdColumnName, $hostUserConnectionIdColumnName, $configuredUserNameListColumnName, $configuredUserNameListModeColumnName, $minRoomSizeColumnName, $maxRoomSizeColumnName, $gameStateIdColumnName, $gameStartedColumnName) VALUES ('$roomIdAsString', '$hostUserConnectionId', '$encodedUserList', $encodedUserListMode, $minRoomSize, $maxRoomSize, $gameStateId, 0)")

            room = getRoom(roomIdAsString, it)
            true
        }

        return room!!
    }

    override fun get(id: String): Room? {
        var room: Room? = null
        getConnectionAndCommit {
            room = getRoom(id, it)
            true
        }
        return room
    }

    override fun beginTransactionWithRoom(id: String): RoomTransaction? {
        val connection = getConnection()
        val room = getRoom(id, connection)
        if (room == null) {
            connection.rollback()
            return null
        }
        val roomTransaction = RoomTransaction(ObservableRoom(room), this)
        pendingTransactions[roomTransaction] = connection
        return roomTransaction
    }

    override fun commitTransaction(roomTransaction: RoomTransaction) {
        pendingTransactions[roomTransaction]?.commit()
        pendingTransactions.remove(roomTransaction)
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
        pendingTransactions[roomTransaction]?.rollback()
        pendingTransactions.remove(roomTransaction)
    }

    override fun deleteRoom(id: String): Room? {
        var room: Room? = null
        getConnectionAndCommit {
            val roomResult =
                it.createStatement().executeQuery("SELECT * FROM $roomsTableName WHERE $roomIdColumnName = '$id'")!!
            if (!roomResult.next()) return@getConnectionAndCommit false
            room = getRoom(id, it)
            val gameStateId = roomResult.getRowId(gameStateIdColumnName)
            it.createStatement()
                .executeUpdate("DELETE FROM $gameDataTableName WHERE $gameDataIdColumnName = $gameStateId")
            true
        }
        return room
    }

    override fun clearRooms() {
        getConnectionAndCommit {
            it.createStatement().executeUpdate("DELETE FROM $gameDataTableName")
            true
        }
    }

    override fun containsRoom(id: String): Boolean {
        var result = false
        getConnectionAndCommit {
            val room = getRoom(id, it)
            result = room != null
            true
        }
        return result
    }

    override fun getAllRooms(): Collection<Room> {
        val rooms = mutableListOf<Room>()
        getConnectionAndCommit {
            val roomIdQueryResult = it.createStatement().executeQuery("SELECT * FROM $roomsTableName")
            while (roomIdQueryResult.next()) {
                rooms.add(getRoom(roomIdQueryResult.getString(roomIdColumnName), it)!!)
            }
            true
        }
        return rooms
    }
}
