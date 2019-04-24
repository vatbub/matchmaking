package com.github.vatbub.matchmaking.common

import com.esotericsoftware.kryo.Kryo
import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.*
import com.github.vatbub.matchmaking.common.responses.*
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse

object KryoCommon {
    fun registerClasses(kryo: Kryo){
        // data
        kryo.register(GameData::class.java)
        kryo.register(Room::class.java)
        kryo.register(User::class.java)

        // requests
        kryo.register(DestroyRoomRequest::class.java)
        kryo.register(DisconnectRequest::class.java)
        kryo.register(GetConnectionIdRequest::class.java)
        kryo.register(GetRoomDataRequest::class.java)
        kryo.register(JoinOrCreateRoomRequest::class.java)
        kryo.register(SendDataToHostRequest::class.java)
        kryo.register(StartGameRequest::class.java)
        kryo.register(SubscribeToRoomRequest::class.java)
        kryo.register(UpdateGameStateRequest::class.java)

        // responses
        kryo.register(AuthorizationException::class.java)
        kryo.register(BadRequestException::class.java)
        kryo.register(DestroyRoomResponse::class.java)
        kryo.register(DisconnectResponse::class.java)
        kryo.register(GetConnectionIdResponse::class.java)
        kryo.register(GetRoomDataResponse::class.java)
        kryo.register(InternalServerErrorException::class.java)
        kryo.register(JoinOrCreateRoomResponse::class.java)
        kryo.register(NotAllowedException::class.java)
        kryo.register(ServerInteractionException::class.java)
        kryo.register(SubscribeToRoomResponse::class.java)
        kryo.register(UnknownConnectionIdException::class.java)

        // testing.dummies
        kryo.register(DummyRequest::class.java)
        kryo.register(DummyResponse::class.java)
    }
}