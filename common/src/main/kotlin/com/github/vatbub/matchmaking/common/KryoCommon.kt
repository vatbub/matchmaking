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
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

object KryoCommon {
    const val defaultTcpPort = 872
    const val defaultStringValueForInstantiation = "hgvjzftr7i680zogh__kryoDefaultValue__hgcfdtzre657t8zui"
}

inline fun <reified T> kryoSafeListOf(vararg elements: T): List<T> = List(elements.size) { elements[it] }

fun Kryo.registerClasses() {
    // data
    val instantRegistration = this.register(Instant::class.java)!!
    instantRegistration.setInstantiator { Instant.now() }
    this.register(GameData::class.java)
    val roomRegistration = this.register(Room::class.java)
    roomRegistration.setInstantiator { Room(KryoCommon.defaultStringValueForInstantiation, KryoCommon.defaultStringValueForInstantiation) }
    val userRegistration = this.register(User::class.java)
    userRegistration.setInstantiator { User(KryoCommon.defaultStringValueForInstantiation, KryoCommon.defaultStringValueForInstantiation) }

    // requests
    this.register(DestroyRoomRequest::class.java)
    this.register(DisconnectRequest::class.java)
    this.register(GetConnectionIdRequest::class.java)
    this.register(GetRoomDataRequest::class.java)
    this.register(JoinOrCreateRoomRequest::class.java)
    this.register(SendDataToHostRequest::class.java)
    this.register(StartGameRequest::class.java)
    this.register(SubscribeToRoomRequest::class.java)
    this.register(UpdateGameStateRequest::class.java)

    // responses
    this.register(AuthorizationException::class.java)
    this.register(BadRequestException::class.java)
    this.register(DestroyRoomResponse::class.java)
    this.register(DisconnectResponse::class.java)
    this.register(GetConnectionIdResponse::class.java)
    this.register(GetRoomDataResponse::class.java)
    this.register(InternalServerErrorException::class.java)
    this.register(JoinOrCreateRoomResponse::class.java)
    this.register(NotAllowedException::class.java)
    this.register(ServerInteractionException::class.java)
    this.register(SubscribeToRoomResponse::class.java)
    this.register(UnknownConnectionIdException::class.java)

    // other
    this.register(Operation::class.java)
    val listRegistration = this.register(ArrayList::class.java)
    listRegistration.setInstantiator { ArrayList<Any>() }
    val inet4AddressRegistration = this.register(Inet4Address::class.java)
    inet4AddressRegistration.setInstantiator { InetAddress.getByName("129.187.211.162") }
    val inet6AddressRegistration = this.register(Inet6Address::class.java)
    inet6AddressRegistration.setInstantiator { InetAddress.getByName("2001:4ca0:2fff:11:0:0:0:25") }
    // inner class Collections$UnmodifiableRandomAccessList is not public. We therefore need to get an instance of that
    // class and get its javaClass-object
    val unmodifiableRandomAccessListRegistration = this.register(Collections.unmodifiableList(listOf<Any>()).javaClass)
    unmodifiableRandomAccessListRegistration.setInstantiator { Collections.unmodifiableList(listOf<Any>()) }

    // testing.dummies
    this.register(DummyRequest::class.java)
    this.register(DummyResponse::class.java)
}
