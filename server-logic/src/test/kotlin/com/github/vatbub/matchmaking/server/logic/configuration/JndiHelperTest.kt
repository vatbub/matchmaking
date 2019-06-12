/*-
 * #%L
 * matchmaking.server-logic
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
package com.github.vatbub.matchmaking.server.logic.configuration

import com.github.vatbub.matchmaking.server.logic.JndiTestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JndiHelperTest {
    @AfterEach
    fun tearDown() {
        JndiTestUtils.resetContext()
    }

    @Test
    fun noEnvironmentTest() {
        val result = JndiHelper.readJndi<String>("certainlyUnknownTestParam")
        Assertions.assertNull(result)
    }

    @Test
    fun readEnvironmentTest() {
        val key = "myKey"
        val value = "myValue"
        JndiTestUtils.mockContext(mapOf(key to value))
        val result = JndiHelper.readJndi<String>(key)
        Assertions.assertEquals(value, result)
    }
}
