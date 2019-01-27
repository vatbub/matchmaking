package com.github.vatbub.matchmaking.common.responses

class UnknownConnectionIdException(message: String? = null, connectionId: String? = null) :
    ServerInteractionException(message, 404, connectionId, UnknownConnectionIdException::class.qualifiedName!!)