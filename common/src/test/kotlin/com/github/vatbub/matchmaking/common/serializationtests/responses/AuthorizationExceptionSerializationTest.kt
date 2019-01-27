package com.github.vatbub.matchmaking.common.serializationtests.responses

import com.github.vatbub.matchmaking.common.responses.AuthorizationException
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass

class AuthorizationExceptionSerializationTest :
    SerializationTestSuperclass<AuthorizationException>(AuthorizationException::class.java) {
    override fun newObjectUnderTest(): AuthorizationException {
        return AuthorizationException()
    }
}