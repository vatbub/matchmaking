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
package com.github.vatbub.matchmaking.server.idprovider

import com.github.vatbub.matchmaking.server.ConnectionIdProvider
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Test

abstract class ConnectionIdProviderTest(@Suppress("MemberVisibilityCanBePrivate") val connectionIdProvider: ConnectionIdProvider) : KotlinTestSuperclass() {

    @Before
    fun setUp() {
        connectionIdProvider.reset()
    }

    @Test
    fun createIdTest() {
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<String>()
        for (i in 1..numberOfIdsToCreate) {
            val newId = connectionIdProvider.getNewId()
            Assert.assertFalse(createdIds.contains(newId))
            createdIds.add(newId)
        }
    }

    @Test
    fun negativeContainsIdTest() {
        val testValue = (4567876543).toString(16)
        Assert.assertFalse(connectionIdProvider.containsId(testValue))
    }

    @Test
    fun positiveContainsIdTest() {
        val id = connectionIdProvider.getNewId()
        Assert.assertTrue(connectionIdProvider.containsId(id))
    }

    @Test
    fun resetTest() {
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<String>()
        for (i in 1..numberOfIdsToCreate)
            createdIds.add(connectionIdProvider.getNewId())

        connectionIdProvider.reset()

        for (id in createdIds)
            Assert.assertFalse(connectionIdProvider.containsId(id))
    }

    @Test
    fun positiveDeleteIdTest() {
        val numberOfIdsToCreate = 10
        val createdIds = mutableListOf<String>()
        for (i in 1..numberOfIdsToCreate)
            createdIds.add(connectionIdProvider.getNewId())

        val deletedId = createdIds.removeAt(0)
        Assert.assertTrue(connectionIdProvider.deleteId(deletedId))

        // The remaining ones should still be there
        for (id in createdIds)
            Assert.assertTrue(connectionIdProvider.containsId(id))
    }

    @Test
    fun negativeDeleteIdTest() {
        val testValue = (4567876543).toString(16)
        Assert.assertFalse(connectionIdProvider.deleteId(testValue))
    }
}
