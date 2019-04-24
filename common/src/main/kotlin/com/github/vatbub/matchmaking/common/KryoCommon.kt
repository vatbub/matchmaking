/*-
 * #%L
 * matchmaking.common
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
