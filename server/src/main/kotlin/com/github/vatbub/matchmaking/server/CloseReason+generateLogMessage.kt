package com.github.vatbub.matchmaking.server

import javax.websocket.CloseReason

internal fun CloseReason.generateLogMessage(): String =
        "A websocket was closed. Close code: ${this.websocketCloseCode.code} ${this.websocketCloseCode.meaning}; Close phrase: ${this.reasonPhrase}"