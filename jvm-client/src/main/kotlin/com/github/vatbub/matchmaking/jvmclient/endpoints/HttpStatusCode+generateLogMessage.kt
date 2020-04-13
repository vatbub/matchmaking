package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.HttpStatusCode

internal fun HttpStatusCode.generateLogMessage() =
        "Websocket session opened with status code ${this.statusCode} ${this.meaning}"