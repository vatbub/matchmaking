package com.github.vatbub.matchmaking.common.serializationtests.responses

import com.github.vatbub.matchmaking.common.responses.AuthorizationException
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass

class AuthorizationExceptionSerializationTest :
    ServerInteractionSerializationTestSuperclass<AuthorizationException>(AuthorizationException::class.java) {
    override fun newObjectUnderTest(): AuthorizationException {
        return AuthorizationException()
    }
}