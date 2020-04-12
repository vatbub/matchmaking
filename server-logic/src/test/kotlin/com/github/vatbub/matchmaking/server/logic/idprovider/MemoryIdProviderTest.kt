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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MemoryIdProviderTest : ConnectionIdProviderTest<MemoryIdProvider>() {
    override fun getCloneOf(instance: MemoryIdProvider): MemoryIdProvider {
        val result = MemoryIdProvider()
        result.internalConnectionIdsInUse.putAll(instance.connectionIdsInUse)
        return result
    }

    override fun newObjectUnderTest(): MemoryIdProvider = MemoryIdProvider()

    @Test
    fun unmodifiableConnectionIdsInUseList() {
        val connectionIds = newObjectUnderTest().connectionIdsInUse as MutableMap
        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            connectionIds["id"] = Id("id", "password")
        }
    }

    @Test
    override fun notEqualsTest() {
        val provider1 = newObjectUnderTest()
        val provider2 = newObjectUnderTest()
        provider2.getNewId()
        Assertions.assertNotEquals(provider1, provider2)
    }
}
