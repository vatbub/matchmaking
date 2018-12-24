package com.github.vatbub.matchmaking.server.dummies

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.server.RequestHandler

class DynamicRequestHandler(
    val canHandleFun: (request: Request) -> Boolean,
    val handleFun: (request: Request) -> Response
) : RequestHandler {
    override fun handle(request: Request): Response {
        return handleFun(request)
    }

    override fun canHandle(request: Request): Boolean {
        return canHandleFun(request)
    }
}