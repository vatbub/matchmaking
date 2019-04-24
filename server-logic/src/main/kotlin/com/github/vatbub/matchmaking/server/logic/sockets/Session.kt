package com.github.vatbub.matchmaking.server.logic.sockets

import com.github.vatbub.matchmaking.common.ServerInteraction

abstract class Session {
    abstract fun sendObjectSync(objectToSend: ServerInteraction)
    abstract fun sendObjectAsync(objectToSend: ServerInteraction)
}