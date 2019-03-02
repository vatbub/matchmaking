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
package com.github.vatbub.matchmaking.common.serializationtests

import com.github.vatbub.matchmaking.common.ServerInteraction
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ServerInteractionSerializationTestSuperclass<T : ServerInteraction>(clazz: Class<T>) :
    SerializationTestSuperclass<T>(clazz) {

    @Test
    fun protocolVersionTest() {
        Assertions.assertEquals(ServerInteraction.defaultProtocolVersion, newObjectUnderTest().protocolVersion)
    }

    @Test
    fun protocolVersionSerializationTest() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val originalObject = newObjectUnderTest()
        val json = gson.toJson(originalObject)
        println(json)
        Assertions.assertTrue(json.contains("\"protocolVersion\": \"${originalObject.protocolVersion}\""))
    }
}
