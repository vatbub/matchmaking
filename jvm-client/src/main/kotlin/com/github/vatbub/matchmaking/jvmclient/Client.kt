package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.*
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result.*
import com.github.vatbub.matchmaking.common.responses.Result.Nothing
import java.net.ConnectException
import java.net.URL

enum class PollInterval(timeToSleepBetweenUpdatesInMilliSeconds: Int) {
    Fast(250),
    Medium(500),
    Slow(1000),
    VerySlow(5000),
    ExtremelySlow(10000)
}

sealed class EndpointConfiguration {
    class WebsocketEndpointConfig(hostUrl: URL) : EndpointConfiguration() {
        private val suffix = "websocket"
        val finalUrl = URL(hostUrl, suffix)
    }

    class HttpPollingEndpointConfig(hostUrl: URL, val pollInterval: PollInterval = PollInterval.Medium) : EndpointConfiguration() {
        private val suffix = "matchmaking"
        val finalUrl = URL(hostUrl, suffix)
    }

    class KryoEndpointConfiguration(val host: String, val tcpPort: Int, val udpPort: Int? = null) : EndpointConfiguration()
}

class Client(
        configurations: List<EndpointConfiguration>,
        private val onConnectedUsersChange: (oldValue: List<User>?, newValue: List<User>) -> Unit,
        private val onGameStateChange: (oldValue: GameData?, newValue: GameData) -> Unit,
        private val onGameStarted: () -> Unit,
        private val onDataToBeSentToHostChange: (oldValue: List<GameData>?, newValue: List<GameData>) -> Unit
) {
    var connectionId: String? = null
        private set
    var password: String? = null
        private set
    private val safeConnectionId: String
        get() = connectionId
                ?: throw IllegalStateException("ConnectionId unknown, use requestConnectionId() to request a new connection id and password from the server")
    private val safePassword: String
        get() = connectionId
                ?: throw IllegalStateException("Password unknown, use requestConnectionId() to request a new connection id and password from the server")
    private var currentRoomId: String? = null
    private val safeCurrentRoomId: String
        get() = currentRoomId
                ?: throw IllegalStateException("CurrentRoomId unknown, use joinRoom(...) or joinOrCreateRoom(...) to join a room")
    var currentRoom: Room? = null
        private set
    val connected: Boolean
        get() = connectionId != null && password != null
    val roomConnected: Boolean
        get() = currentRoomId != null

    private val endpoint: ClientEndpoint<out EndpointConfiguration>

    init {
        var tempEndpoint: ClientEndpoint<out EndpointConfiguration>? = null
        configurations.forEach {
            try {
                tempEndpoint = when (it) {
                    is EndpointConfiguration.WebsocketEndpointConfig -> ClientEndpoint.WebsocketEndpoint(it)
                    is EndpointConfiguration.HttpPollingEndpointConfig -> ClientEndpoint.HttpPollingEndpoint(it)
                    is EndpointConfiguration.KryoEndpointConfiguration -> ClientEndpoint.KryoEndpoint(it)
                }
                return@forEach
            } catch (e: Exception) {
                System.err.println("Unable to connect using this configuration, trying the next configuration (if there's more specified)...")
                e.printStackTrace() // TODO: Logging framework
            }
        }
        endpoint = tempEndpoint
                ?: throw ConnectException("Unable to connect using any of the specified configurations. See the log for more details.")
    }

    fun requestConnectionId() {
        synchronized(this) {
            if (connected) {
                endpoint.abortRequestsOfType(GetConnectionIdRequest())
                return
            }
            endpoint.sendRequest<GetConnectionIdResponse>(GetConnectionIdRequest()) {
                connectionId = it.connectionId
                password = it.password
            }
        }
    }

    fun joinRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.JoinRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    fun createRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.CreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    fun joinOrCreateRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.JoinOrCreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    private fun joinOrCreateRoom(operation: Operation, userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) {
        synchronized(this) {
            if (roomConnected) {
                endpoint.abortRequestsOfType(JoinOrCreateRoomRequest("", "", Operation.JoinOrCreateRoom, ""))
                throw IllegalStateException("Client is already connected to a room")
            }
            endpoint.sendRequest<JoinOrCreateRoomResponse>(JoinOrCreateRoomRequest(safeConnectionId, safePassword, operation, userName, whitelist, blacklist, minRoomSize, maxRoomSize)) {
                when (it.result) {
                    RoomCreated -> println("Room created: ${it.roomId}") // TODO: Logging framework
                    RoomJoined -> {
                        currentRoomId = it.roomId!!
                        endpoint.subscribeToRoom(safeConnectionId, safePassword, safeCurrentRoomId, this::newRoomDataHandler)
                    }
                    Nothing -> println("Result of JoinOrCreateRoomRequest: Nothing")
                }
            }
        }
    }

    fun disconnect() {
        synchronized(this) {
            if (!connected) {
                endpoint.terminateConnection()
                return
            }

            endpoint.sendRequest<DisconnectResponse>(DisconnectRequest(safeConnectionId, safePassword)) {
                endpoint.terminateConnection()
            }
        }
    }

    fun sendDataToHost(data: List<GameData>) {
        synchronized(this) {
            endpoint.sendRequest<GetRoomDataResponse>(SendDataToHostRequest(safeConnectionId, safePassword, safeCurrentRoomId, data)) { processGetGameDataResponse(it) }
        }
    }

    private fun verifyIsHost() {
        val currentRoomCopy = currentRoom
                ?: throw IllegalStateException("Room data not yet retrieved, please wait with calling this method until currentRoom != null")
        if (!currentRoomCopy.amITheHost)
            throw IllegalStateException("You are not the host of the room, only the host is allowed to perform this action!")
    }

    fun startGame() {
        synchronized(this) {
            verifyIsHost()
            endpoint.sendRequest<GetRoomDataResponse>(StartGameRequest(safeConnectionId, safePassword, safeCurrentRoomId)) { processGetGameDataResponse(it) }
        }
    }

    fun updateGameState(newGameState: GameData, processedData: List<GameData>) {
        synchronized(this) {
            verifyIsHost()
            endpoint.sendRequest<GetRoomDataResponse>(UpdateGameStateRequest(safeConnectionId, safePassword, safeCurrentRoomId, newGameState, processedData)) { processGetGameDataResponse(it) }
        }
    }

    private fun processGetGameDataResponse(getGameDataResponse: GetRoomDataResponse) {
        val room = getGameDataResponse.room
                ?: throw IllegalArgumentException("Room unknown to server, try reconnecting")
        newRoomDataHandler(Room(safeConnectionId, room))
    }

    private fun newRoomDataHandler(room: Room) {
        val oldRoom = currentRoom
        currentRoom = room

        if (oldRoom == null || !oldRoom.connectedUsers.toTypedArray().contentEquals(room.connectedUsers.toTypedArray()))
            onConnectedUsersChange(oldRoom?.connectedUsers, room.connectedUsers)
        if (oldRoom == null || oldRoom.gameState != room.gameState)
            onGameStateChange(oldRoom?.gameState, room.gameState)
        if (oldRoom == null || (!oldRoom.gameStarted && room.gameStarted))
            onGameStarted.invoke()
        if (oldRoom == null || !oldRoom.dataToBeSentToTheHost.toTypedArray().contentEquals(room.dataToBeSentToTheHost.toTypedArray()))
            onDataToBeSentToHostChange(oldRoom?.dataToBeSentToTheHost, room.dataToBeSentToTheHost)
    }
}