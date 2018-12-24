package com.github.vatbub.matchmaking.common.responses

class InternalServerErrorException(message: String?, connectionId: String? = null) :
    ServerInteractionException(message, 500, connectionId, InternalServerErrorException::class.qualifiedName!!)