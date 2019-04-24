package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.server.logic.sockets.Session


class WebsocketSessionWrapper(private val session: javax.websocket.Session) : Session() {
    override fun sendObjectSync(objectToSend: ServerInteraction) {
        session.basicRemote.sendText(InteractionConverter.serialize(objectToSend))
    }

    override fun sendObjectAsync(objectToSend: ServerInteraction) {
        session.asyncRemote.sendText(InteractionConverter.serialize(objectToSend))
    }

}