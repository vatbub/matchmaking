package com.github.vatbub.matchmaking.common.serializationtests.responses

import com.github.vatbub.matchmaking.common.responses.UnknownConnectionIdException
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass

class UnknownConnectionIdExceptionSerializationTest :
    SerializationTestSuperclass<UnknownConnectionIdException>(UnknownConnectionIdException::class.java) {
    override fun newObjectUnderTest(): UnknownConnectionIdException {
        return UnknownConnectionIdException()
    }
}