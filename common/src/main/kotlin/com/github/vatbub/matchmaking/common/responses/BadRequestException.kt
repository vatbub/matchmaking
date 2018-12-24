package com.github.vatbub.matchmaking.common.responses

class BadRequestException(message: String?, connectionId: String? = null) :
    ServerInteractionException(message, 400, connectionId, BadRequestException::class.qualifiedName!!)