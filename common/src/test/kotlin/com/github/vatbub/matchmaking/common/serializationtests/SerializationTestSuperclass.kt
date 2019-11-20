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

import com.github.vatbub.matchmaking.common.fromJson
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestClient
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestServer
import com.github.vatbub.matchmaking.common.toJson
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Path

private var lastFileCounter = -1
internal val nextFileCounter: Int
    get() {
        lastFileCounter++
        return lastFileCounter
    }
internal val nextObjectFileName: String
    get() = "$nextFileCounter.bin"

internal fun nextObjectPath(root: Path) = root.resolve(nextObjectFileName)

abstract class SerializationTestSuperclass<T : Any>(private val clazz: Class<T>) :
        KotlinTestSuperclass<T>() {
    private var kryoServer: KryoTestServer? = null
    private var kryoClient: KryoTestClient? = null

    @AfterEach
    fun stopKryo() {
        kryoClient?.stop()
        kryoServer?.stop()
    }

    @Test
    fun serializationTest() {
        val originalObject = newObjectUnderTest()
        val json = toJson(originalObject, prettify = true)
        val deserializedObject: T = fromJson(json, clazz)
        Assertions.assertEquals(originalObject, deserializedObject)
    }

    @Test
    fun kryoSerializationTest(@TempDir tempDir: Path) {
        val originalObject = newObjectUnderTest()
        val outputFile = nextObjectPath(tempDir).toFile()
        ObjectOutputStream(FileOutputStream(outputFile)).use {
            it.writeObject(originalObject)
        }

        ObjectInputStream(FileInputStream(outputFile)).use {
            val deserializedObject = it.readObject()
            Assertions.assertEquals(originalObject, deserializedObject)
        }
    }

    @Test
    @Disabled
    fun kryoNetSerializationTest() {
        Assertions.fail<String>("Not implemented yet")
        /*
        val originalObject1 = newObjectUnderTest()
        val originalObject2 = newObjectUnderTest()
        var listener1Called = false
        var listener2Called = false
        kryoServer = KryoTestServer(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                listener1Called = true
                Assertions.assertEquals(originalObject1, receivedObject)
                Assertions.assertEquals(originalObject1.hashCode(), receivedObject.hashCode())
                connection?.sendTCP(originalObject2)
            }
        })
        kryoClient = KryoTestClient(kryoTestServer = kryoServer!!, listener = object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                listener2Called = true
                Assertions.assertEquals(originalObject2, receivedObject)
            }
        })

        kryoClient?.client?.sendTCP(originalObject1)
        await().atMost(5, TimeUnit.SECONDS).until { listener1Called }
        await().atMost(5, TimeUnit.SECONDS).until { listener2Called }*/
    }
}
