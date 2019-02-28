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
import com.github.vatbub.matchmaking.server.roomproviders.data.ObservableRoom
import com.github.vatbub.matchmaking.server.roomproviders.data.RoomTransaction
import com.github.vatbub.matchmaking.server.roomproviders.database.*
import com.github.vatbub.matchmaking.server.roomproviders.database.OnModificationAction.CASCADE
import com.github.vatbub.matchmaking.server.roomproviders.database.Type.*
import com.google.gson.Gson
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Timestamp
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
    private val gson = Gson()

    private val pendingTransactions = mutableMapOf<RoomTransaction, Connection>()

    private val roomIdLength = 8
    private val varcharMax = 500
    private val userListDelimiter = ","

    private val gameDataTable = Table(
        "game_data", ColumnList(
            listOf(
                Column("id", INTEGER, constraints = listOf(PrimaryKeyConstraint())),
                Column("created_at_utc", TIMESTAMP),
                Column("created_by", VARCHAR, listOf(varcharMax)),
                Column("serialized_contents", VARCHAR, listOf(varcharMax))
            )
        )
    )

    private val roomsTable = Table(
        "rooms", ColumnList(
            listOf(
                Column("id", VARCHAR, listOf(roomIdLength), listOf(PrimaryKeyConstraint())),
                Column("host_user_connection_id", VARCHAR, listOf(varcharMax)),
                Column("whitelist", VARCHAR, listOf(varcharMax)),
                Column("blacklist", VARCHAR, listOf(varcharMax)),
                Column("min_room_size", INTEGER),
                Column("max_room_size", INTEGER),
                Column(
                    "game_state_id",
                    INTEGER,
                    constraints = listOf(ForeignKeyConstraint(gameDataTable, gameDataTable.columns["id"]!!, CASCADE))
                ),
                Column("game_started", INTEGER)
            )
        )
    )

    private val usersTable = Table(
        "users", ColumnList(
            listOf(
                Column("connection_id", VARCHAR, listOf(varcharMax), listOf(PrimaryKeyConstraint())),
                Column("username", VARCHAR, listOf(varcharMax)),
                Column("ipv4", VARCHAR, listOf(varcharMax)),
                Column("ipv6", VARCHAR, listOf(varcharMax)),
                Column(
                    "connected_to",
                    VARCHAR,
                    listOf(roomIdLength),
                    listOf(ForeignKeyConstraint(roomsTable, roomsTable.columns["id"]!!, CASCADE))
                )
            )
        )
    )

    private val dataToBeSentToHostTable = Table(
        "data_to_be_sent_to_host", ColumnList(
            listOf(
                Column("id", INTEGER, constraints = listOf(PrimaryKeyConstraint())),
                Column(
                    "belongs_to_room",
                    VARCHAR,
                    listOf(roomIdLength),
                    listOf(ForeignKeyConstraint(roomsTable, roomsTable.columns["id"]!!, CASCADE))
                ),
                Column("index", INTEGER),
                Column(
                    "game_data_id",
                    INTEGER,
                    constraints = listOf(ForeignKeyConstraint(gameDataTable, gameDataTable.columns["id"]!!, CASCADE))
                )
            )
        )
    )

    private val schema = Schema(
        listOf(
            gameDataTable,
            roomsTable,
            usersTable,
            dataToBeSentToHostTable
        )
    )

    init {
        val driver = DriverManager.getDriver(connectionString)!!
        connectionPoolDataSource.maxPoolSize = 999999999
        // connectionPoolDataSource.maxPoolSize = 5
        connectionPoolDataSource.driverClass = driver.javaClass.name
        connectionPoolDataSource.jdbcUrl = connectionString
        if (dbUser != null)
            connectionPoolDataSource.user = dbUser
        if (dbPassword != null)
            connectionPoolDataSource.password = dbPassword
        if (connectionProperties != null)
            connectionPoolDataSource.properties = connectionProperties

        getConnectionAndCommit { schema.createIfNecessary(it); true }
    }

    private fun getConnection(): Connection {
        val connection = connectionPoolDataSource.connection
        connection.autoCommit = false
        return connection
    }

    private fun getConnectionAndCommit(transaction: ((connection: Connection) -> Boolean)) {
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

    override val supportsConcurrentTransactionsOnSameRoom = true

    private fun saveGameData(connection: Connection, gameDataId: Int? = null, gameData: GameData): Int {
        val json = gson.toJson(gameData.contents)

        if (gameDataId != null) {
            val updateStatement =
                connection.prepareStatement("UPDATE ${gameDataTable.name} SET ${gameDataTable.columns[1].name} = ?, ${gameDataTable.columns[3].name} = ? WHERE id = ?")
            updateStatement.setTimestamp(1, Timestamp.from(gameData.createdAtUtc))
            updateStatement.setString(2, json)
            updateStatement.setInt(3, gameDataId)
            updateStatement.executeUpdate()
            return gameDataId
        }

        var newGameDataId = 0
        val idExistsStatement = connection.prepareStatement("SELECT * FROM ${gameDataTable.name} WHERE id = ?")
        do {
            newGameDataId++
            idExistsStatement.setInt(1, newGameDataId)
        } while (idExistsStatement.executeQuery().next())

        gameDataTable.insertInto(
            connection,
            newGameDataId,
            Timestamp.from(gameData.createdAtUtc),
            gameData.createdByConnectionId,
            json
        )

        return newGameDataId
    }

    private fun encodeUserList(userList: List<String>?): String? {
        if (userList == null)
            return null
        return userList.joinToString(userListDelimiter)
    }

    private fun decodeUserList(userListString: String?) = userListString?.split(userListDelimiter)


    private fun getRoom(id: String, connection: Connection): Room? {
        val queryResult =
            connection.createStatement().executeQuery("SELECT * FROM ${roomsTable.name} WHERE id = '$id'")
        if (!queryResult.next())
            return null

        val result = Room(
            id,
            queryResult.getString(2),
            decodeUserList(queryResult.getString(3)),
            decodeUserList(queryResult.getString(4)),
            queryResult.getInt(5),
            queryResult.getInt(6)
        )
        result.connectedUsers.addAll(getConnectedUsers(id, connection))
        result.gameState = getGameData(queryResult.getInt(7), connection)!!
        result.gameStarted = queryResult.getInt(8) != 0
        result.dataToBeSentToTheHost.addAll(getDataToBeSentToHost(id, connection))
        return result
    }

    private fun getConnectedUsers(roomId: String, connection: Connection): List<User> {
        val statement =
            connection.prepareStatement("SELECT * FROM ${usersTable.name} WHERE ${usersTable.columns[4].name} = ?")
        statement.setString(1, roomId)
        val queryResult = statement.executeQuery()
        val result = mutableListOf<User>()
        while (queryResult.next()) {
            val inet4AddressString = queryResult.getString(3)
            val inet6AddressString = queryResult.getString(4)

            val inet4Address =
                if (inet4AddressString == null) null else InetAddress.getByName(inet4AddressString) as Inet4Address
            val inet6Address =
                if (inet6AddressString == null) null else InetAddress.getByName(inet6AddressString) as Inet6Address
            result.add(
                User(
                    queryResult.getString(1), queryResult.getString(2),
                    inet4Address,
                    inet6Address
                )
            )
        }
        return result
    }

    private fun getGameData(gameDataId: Int, connection: Connection): GameData? {
        val statement = connection.prepareStatement(("SELECT * FROM ${gameDataTable.name} WHERE id = ?"))
        statement.setInt(1, gameDataId)
        val queryResult = statement.executeQuery()
        if (!queryResult.next()) return null

        val contentsJson = queryResult.getString(4)
        val contents = gson.fromJson<LinkedHashMap<String, Any>>(contentsJson, LinkedHashMap::class.java)
        val gameData = GameData(queryResult.getString(3), contents)
        gameData.createdAtUtc = queryResult.getTimestamp(2).toInstant()
        return gameData
    }

    private fun getDataToBeSentToHost(roomId: String, connection: Connection): List<GameData> {
        val statement =
            connection.prepareStatement("SELECT * FROM ${dataToBeSentToHostTable.name} WHERE ${dataToBeSentToHostTable.columns[1].name} = ?")
        statement.setString(1, roomId)
        val queryResult = statement.executeQuery()
        val result = mutableListOf<GameData>()
        while (queryResult.next()) {
            result.add(getGameData(queryResult.getInt(4), connection)!!)
        }
        return result
    }

    override fun createNewRoom(
        hostUserConnectionId: String,
        whitelist: List<String>?,
        blacklist: List<String>?,
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

            val gameStateId = saveGameData(it, gameData = GameData(hostUserConnectionId))
            val encodedWhitelist = encodeUserList(whitelist)
            val encodedBlacklist = encodeUserList(blacklist)
            //  VALUES ('$roomIdAsString', '$hostUserConnectionId', '$encodedUserList', $encodedUserListMode, $minRoomSize, $maxRoomSize, $gameStateId, 0)")

            roomsTable.insertInto(
                it,
                roomIdAsString,
                hostUserConnectionId,
                encodedWhitelist,
                encodedBlacklist,
                minRoomSize,
                maxRoomSize,
                gameStateId,
                0
            )
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
        val roomTransaction =
            RoomTransaction(ObservableRoom(room), this)

        // game started listener
        roomTransaction.room.onGameStartedChange = { gameStarted ->
            val gameStartedAsInt = if (gameStarted) 1 else 0
            val statement =
                pendingTransactions[roomTransaction]!!.prepareStatement("UPDATE ${roomsTable.name} SET ${roomsTable.columns[7].name} = ? WHERE id = ?")
            statement.setInt(1, gameStartedAsInt)
            statement.setString(2, id)
            statement.executeUpdate()
        }

        // connected users listeners
        roomTransaction.room.connectedUsers.onAdd = { element, _ ->
            val ipv4 = element.ipv4Address?.toString()
            val ipv6 = element.ipv6Address?.toString()
            usersTable.insertInto(
                pendingTransactions[roomTransaction]!!,
                element.connectionId,
                element.userName,
                ipv4,
                ipv6,
                roomTransaction.room.id
            )
        }
        roomTransaction.room.connectedUsers.onClear = {
            val statement =
                pendingTransactions[roomTransaction]!!.prepareStatement("DELETE FROM ${usersTable.name} WHERE ${usersTable.columns[4].name} = ?")
            statement.setString(1, roomTransaction.room.id)
            statement.executeUpdate()
        }
        roomTransaction.room.connectedUsers.onRemove = { element, _ ->
            val statement =
                pendingTransactions[roomTransaction]!!.prepareStatement("DELETE FROM ${usersTable.name} WHERE ${usersTable.columns[4].name} = ? AND ${usersTable.columns[0].name} = ?")
            statement.setString(1, roomTransaction.room.id)
            statement.setString(2, element.connectionId)
            statement.executeUpdate()
        }
        roomTransaction.room.connectedUsers.onSet = { oldElement, newElement, _ ->
            val ipv4 = newElement.ipv4Address?.toString()
            val ipv6 = newElement.ipv6Address?.toString()
            val deleteStatement =
                pendingTransactions[roomTransaction]!!.prepareStatement("DELETE FROM ${usersTable.name} WHERE ${usersTable.columns[4].name} = ? AND ${usersTable.columns[0].name} = ?")
            deleteStatement.setString(1, roomTransaction.room.id)
            deleteStatement.setString(2, oldElement.connectionId)
            deleteStatement.executeUpdate()

            usersTable.insertInto(
                pendingTransactions[roomTransaction]!!,
                newElement.connectionId,
                newElement.userName,
                ipv4,
                ipv6,
                roomTransaction.room.id
            )
        }

        // game state listeners
        roomTransaction.room.gameState.onSet = { _, _, _ ->
            saveGameData(
                pendingTransactions[roomTransaction]!!,
                getGameStateIdByRoomId(pendingTransactions[roomTransaction]!!, roomTransaction.room.id),
                roomTransaction.room.gameState.backingGameData
            )
        }

        roomTransaction.room.gameState.onRemove = { _, _ ->
            saveGameData(
                pendingTransactions[roomTransaction]!!,
                getGameStateIdByRoomId(pendingTransactions[roomTransaction]!!, roomTransaction.room.id),
                roomTransaction.room.gameState.backingGameData
            )
        }

        roomTransaction.room.gameState.onTimestampChanged = { _ ->
            saveGameData(
                pendingTransactions[roomTransaction]!!,
                getGameStateIdByRoomId(pendingTransactions[roomTransaction]!!, roomTransaction.room.id),
                roomTransaction.room.gameState.backingGameData
            )
        }

        // data to be sent to host listeners
        roomTransaction.room.dataToBeSentToTheHost.onAdd = { element, index ->
            val gameDataId = saveGameData(pendingTransactions[roomTransaction]!!, gameData = element)

            val dataToBeSentToHostId = createNewDataToBeSentToHostId(pendingTransactions[roomTransaction]!!)
            // VALUES ($dataToBeSentToHostId, '${roomTransaction.room.id}', $gameDataId)")
            dataToBeSentToHostTable.insertInto(
                pendingTransactions[roomTransaction]!!,
                dataToBeSentToHostId,
                roomTransaction.room.id,
                index,
                gameDataId
            )
        }
        roomTransaction.room.dataToBeSentToTheHost.onClear = {
            val dataToBeSentToHostStatement =
                connection.prepareStatement("SELECT * FROM ${dataToBeSentToHostTable.name} WHERE ${dataToBeSentToHostTable.columns[1].name} = ?")
            dataToBeSentToHostStatement.setString(1, roomTransaction.room.id)

            val dataToBeSentToHostResult = dataToBeSentToHostStatement.executeQuery()
            val deleteStatement =
                pendingTransactions[roomTransaction]!!.prepareStatement("DELETE FROM ${gameDataTable.name} WHERE id = ?")

            while (dataToBeSentToHostResult.next()) {
                val gameDataId = dataToBeSentToHostResult.getInt(4)
                deleteStatement.setInt(1, gameDataId)
                deleteStatement.executeUpdate()
            }
        }
        roomTransaction.room.dataToBeSentToTheHost.onRemove = { _, index ->
            val deleteStatement =
                pendingTransactions[roomTransaction]!!.prepareStatement("DELETE FROM ${dataToBeSentToHostTable.name} WHERE ${dataToBeSentToHostTable.columns[1].name} = ? AND ${dataToBeSentToHostTable.columns[2].name} = ?")
            deleteStatement.setString(1, roomTransaction.room.id)
            deleteStatement.setInt(2, index)
            deleteStatement.executeUpdate()
            val updateIndicesStatement =
                pendingTransactions[roomTransaction]!!.prepareStatement("UPDATE ${dataToBeSentToHostTable.name} SET ${dataToBeSentToHostTable.columns[2].name} = ${dataToBeSentToHostTable.columns[2].name} - 1 WHERE ${dataToBeSentToHostTable.columns[1].name} = ? AND ${dataToBeSentToHostTable.columns[2].name} > ?")
            updateIndicesStatement.setString(1, roomTransaction.room.id)
            updateIndicesStatement.setInt(2, index)
            updateIndicesStatement.executeUpdate()
        }
        roomTransaction.room.dataToBeSentToTheHost.onSet = { _, newElement, index ->
            val dataToBeSentToHostStatement =
                pendingTransactions[roomTransaction]!!.prepareStatement("SELECT * FROM ${dataToBeSentToHostTable.name} WHERE ${dataToBeSentToHostTable.columns[1].name} = ? AND ${dataToBeSentToHostTable.columns[2].name} = ?")
            dataToBeSentToHostStatement.setString(1, roomTransaction.room.id)
            dataToBeSentToHostStatement.setInt(2, index)
            val gameDataId = dataToBeSentToHostStatement.executeQuery().getInt(4)
            saveGameData(pendingTransactions[roomTransaction]!!, gameDataId, newElement)
        }

        pendingTransactions[roomTransaction] = connection
        return roomTransaction
    }

    private fun getGameStateIdByRoomId(connection: Connection, roomId: String): Int? {
        val roomsStatement = connection.prepareStatement("SELECT * FROM ${roomsTable.name} WHERE id = ?")
        roomsStatement.setString(1, roomId)
        val roomsResult = roomsStatement.executeQuery()
        if (!roomsResult.next())
            return null
        return roomsResult.getInt(7)
    }

    private fun createNewDataToBeSentToHostId(connection: Connection): Int {
        val queryResult = connection.createStatement()
            .executeQuery("SELECT * FROM ${dataToBeSentToHostTable.name}")
        var newId = 0
        while (queryResult.next()) {
            val currentId = queryResult.getInt(1)
            if (currentId >= newId)
                newId = currentId + 1
        }
        return newId
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
            val gameStateId = getGameStateIdByRoomId(it, id) ?: return@getConnectionAndCommit false
            room = getRoom(id, it)
            val deleteStatement = it.prepareStatement("DELETE FROM ${gameDataTable.name} WHERE id = ?")
            deleteStatement.setInt(1, gameStateId)
            deleteStatement.executeUpdate()
            true
        }
        return room
    }

    override fun clearRooms() {
        getConnectionAndCommit {
            it.createStatement().executeUpdate("DELETE FROM ${gameDataTable.name}")
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
            val roomIdQueryResult = it.createStatement().executeQuery("SELECT * FROM ${roomsTable.name}")
            while (roomIdQueryResult.next()) {
                rooms.add(getRoom(roomIdQueryResult.getString(1), it)!!)
            }
            true
        }
        return rooms
    }
}


