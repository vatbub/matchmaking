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
package com.github.vatbub.matchmaking.server.logic.idprovider

import com.github.vatbub.matchmaking.server.logic.idprovider.AuthorizationResult.*
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ConnectionIdProviderTest<T : ConnectionIdProvider> :
        KotlinTestSuperclass<T>() {

    @Test
    fun createIdTest() {
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<Id>()
        val connectionIdProvider = newObjectUnderTest()
        for (i in 1..numberOfIdsToCreate) {
            val newId = connectionIdProvider.getNewId()
            Assertions.assertNotNull(newId.connectionId)
            Assertions.assertNotNull(newId.password)
            Assertions.assertFalse(createdIds.contains(newId))
            createdIds.add(newId)
        }
    }

    @Test
    fun negativeContainsIdTest() {
        val testValue = (4567876543).toString(16)
        Assertions.assertFalse(newObjectUnderTest().containsId(testValue))
    }

    @Test
    fun positiveContainsIdTest() {
        val connectionIdProvider = newObjectUnderTest()
        val id = connectionIdProvider.getNewId()
        Assertions.assertTrue(connectionIdProvider.containsId(id.connectionId))
    }

    @Test
    fun resetTest() {
        val connectionIdProvider = newObjectUnderTest()
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<Id>()
        for (i in 1..numberOfIdsToCreate)
            createdIds.add(connectionIdProvider.getNewId())

        connectionIdProvider.reset()

        for (id in createdIds)
            Assertions.assertFalse(connectionIdProvider.containsId(id.connectionId))
    }

    @Test
    fun positiveDeleteIdTest() {
        val connectionIdProvider = newObjectUnderTest()
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<Id>()
        for (i in 1..numberOfIdsToCreate)
            createdIds.add(connectionIdProvider.getNewId())

        val deletedId = createdIds.removeAt(0)
        Assertions.assertNotNull(connectionIdProvider.deleteId(deletedId))

        // The remaining ones should still be there
        for (id in createdIds)
            Assertions.assertTrue(connectionIdProvider.containsId(id.connectionId))
    }

    @Test
    fun negativeDeleteIdTest() {
        val testValue = (4567876543).toString(16)
        Assertions.assertNull(newObjectUnderTest().deleteId(testValue))
    }

    @Test
    fun deleteIdConnectionIdNullTest() {
        Assertions.assertNull(newObjectUnderTest().deleteId(Id(null, null)))
    }

    @Test
    fun isAuthorizedConnectionIdNullTest() {
        Assertions.assertEquals(NotAuthorized, newObjectUnderTest().isAuthorized(Id(null, null)))
    }

    @Test
    fun isAuthorizedPasswordNullTest() {
        val connectionIdProvider = newObjectUnderTest()
        val id = connectionIdProvider.getNewId()
        Assertions.assertEquals(NotAuthorized, connectionIdProvider.isAuthorized(Id(id.connectionId, null)))
    }

    @Test
    fun negativeIsAuthorizedTest() {
        val connectionIdProvider = newObjectUnderTest()
        val id = connectionIdProvider.getNewId()
        Assertions.assertEquals(
            NotAuthorized,
            connectionIdProvider.isAuthorized(Id(id.connectionId, TestUtils.getRandomHexString(id.password)))
        )
    }

    @Test
    fun isAuthorizedConnectionIdNotFoundTest() {
        val connectionIdProvider = newObjectUnderTest()
        val id = connectionIdProvider.getNewId()
        Assertions.assertEquals(
            NotFound,
            connectionIdProvider.isAuthorized(Id(TestUtils.getRandomHexString(id.connectionId), id.password))
        )
    }

    @Test
    fun positiveIsAuthorizedTest() {
        val connectionIdProvider = newObjectUnderTest()
        val id = connectionIdProvider.getNewId()
        Assertions.assertEquals(Authorized, connectionIdProvider.isAuthorized(id))
    }
}
