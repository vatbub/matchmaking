package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response

internal class ResponseHandlerWrapper<T : Response>(val request: Request, val handler: (T) -> Unit)