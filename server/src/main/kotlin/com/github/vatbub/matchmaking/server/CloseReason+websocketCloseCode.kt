package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.WebsocketCloseCode
import javax.websocket.CloseReason

internal val CloseReason.websocketCloseCode: WebsocketCloseCode
    get() = WebsocketCloseCode.fromCode(this.closeCode.code)