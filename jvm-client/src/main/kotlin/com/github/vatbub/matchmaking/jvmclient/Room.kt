package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

class Room( ownConnectionId: String, private val wrappedRoom: DataRoom) {
    val id: String
        get() = wrappedRoom.id
    val hostUserConnectionId: String
        get() = wrappedRoom.hostUserConnectionId
    val amITheHost = ownConnectionId == hostUserConnectionId
    val whitelist: List<String>?
        get() = wrappedRoom.whitelist
    val blacklist: List<String>?
        get() = wrappedRoom.blacklist
    val minRoomSize: Int
        get() = wrappedRoom.minRoomSize
    val maxRoomSize: Int
        get() = wrappedRoom.maxRoomSize
    val connectedUsers: List<User>
        get() = wrappedRoom.connectedUsers.toList()
    val gameState: GameData
        get() = wrappedRoom.gameState
    val gameStarted: Boolean
        get() = wrappedRoom.gameStarted
    val dataToBeSentToTheHost: List<GameData>
        get() = wrappedRoom.dataToBeSentToTheHost.toList()
}