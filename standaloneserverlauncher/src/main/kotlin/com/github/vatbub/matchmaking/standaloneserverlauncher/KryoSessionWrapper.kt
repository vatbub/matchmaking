package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.esotericsoftware.kryonet.Connection
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.server.logic.sockets.Session

class KryoSessionWrapper(private val connection: Connection) : Session() {
    override fun sendObjectSync(objectToSend: ServerInteraction) {
        try {
            connection.sendUDP(objectToSend)
        } catch (e: IllegalStateException) {
            connection.sendTCP(objectToSend)
        }
    }

    override fun sendObjectAsync(objectToSend: ServerInteraction) {
        Thread { sendObjectSync(objectToSend) }.start()
    }
}