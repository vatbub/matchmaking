package com.github.vatbub.matchmaking.server.logic.roomproviders

import com.github.vatbub.matchmaking.server.logic.roomproviders.data.RoomTransaction

internal fun RoomTransaction.canUserJoinOrAbort(userName: String,
                                                whitelist: List<String>?,
                                                blacklist: List<String>?,
                                                minRoomSize: Int,
                                                maxRoomSize: Int): Boolean {
    /*
     * We have to check again even though we have the filter in place
     * because room.gameStarted might have changed between the call to
     * the filter and the beginning of the transaction (multithreading magic :/ )
     */
    if (this.room.gameStarted) {
        this.abort()
        return false
    }

    if ((this.room.connectedUsers.size + 1) > this.room.maxRoomSize) {
        this.abort()
        return false
    }
    if (this.room.minRoomSize < minRoomSize) {
        this.abort()
        return false
    }
    if (this.room.maxRoomSize > maxRoomSize) {
        this.abort()
        return false
    }

    // check the supplied user list
    if (whitelist != null && this.room.connectedUsers.any { !whitelist.contains(it.userName) }) {
        this.abort()
        return false
    }
    if (blacklist != null && this.room.connectedUsers.any { blacklist.contains(it.userName) }) {
        this.abort()
        return false
    }

    // check the room's user list
    if (this.room.whitelist?.contains(userName) == false) {
        this.abort()
        return false
    }
    if (this.room.blacklist?.contains(userName) == true) {
        this.abort()
        return false
    }

    return true
}