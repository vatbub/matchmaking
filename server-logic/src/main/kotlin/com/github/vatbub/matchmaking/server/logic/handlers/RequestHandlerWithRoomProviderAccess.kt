package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider

abstract class RequestHandlerWithRoomProviderAccess<T : Request>(val roomProvider: RoomProvider) : RequestHandler<T> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RequestHandlerWithRoomProviderAccess<*>) return false

        if (roomProvider != other.roomProvider) return false

        return true
    }

    override fun hashCode(): Int {
        return roomProvider.hashCode()
    }
}