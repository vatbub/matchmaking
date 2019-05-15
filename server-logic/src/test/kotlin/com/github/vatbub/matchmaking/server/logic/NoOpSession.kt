package com.github.vatbub.matchmaking.server.logic

import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.server.logic.sockets.Session

class NoOpSession : Session() {
    override fun sendObjectSync(objectToSend: ServerInteraction) {}
    override fun sendObjectAsync(objectToSend: ServerInteraction) {}
}