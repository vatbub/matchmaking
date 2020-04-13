package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.WebsocketCloseCode

internal fun WebsocketCloseCode.generateLogMessage(closeReason: String, closedByRemote: Boolean, tryReconnect:Boolean): String {
    val builder = StringBuilder("A websocket was closed. ")
    if (closedByRemote)
        builder.append("The socket was closed by the remote. ")
    if (tryReconnect)
        builder.append("An automatic reconnect will be attempted now. ")
    builder.append("Close code: ${this.code} ${this.meaning}; Close phrase: $closeReason")
    return builder.toString()
}