package com.github.vatbub.matchmaking.common.responses

class AuthorizationException(message: String? = null, connectionId: String? = null) :
    ServerInteractionException(message, 401, connectionId, AuthorizationException::class.qualifiedName!!)