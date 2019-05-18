/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class RequestHandlerTestSuperclass<T : RequestHandler<*>> : KotlinTestSuperclass<T>() {
    @Test
    abstract fun handleTest()

    @Test
    abstract fun positiveCanHandleTest()

    @Test
    abstract fun negativeCanHandleTest()

    @Test
    abstract fun needsAuthenticationTest()
}

abstract class RequestHandlerWithRoomProviderAccessTestSuperclass<T : RequestHandlerWithRoomProviderAccess<*>> : RequestHandlerTestSuperclass<T>() {
    override fun newObjectUnderTest() = newObjectUnderTest(MemoryRoomProvider())
    abstract fun newObjectUnderTest(roomProvider: RoomProvider): T

    @Test
    override fun notEqualsTest() {
        val roomProvider1 = MemoryRoomProvider()
        val roomProvider2 = MemoryRoomProvider()
        roomProvider2.createNewRoom(TestUtils.defaultConnectionId)
        val handler1 = newObjectUnderTest(roomProvider1)
        val handler2 = newObjectUnderTest(roomProvider2)
        Assertions.assertNotEquals(handler1, handler2)
    }
}
