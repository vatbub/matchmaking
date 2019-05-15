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

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.registerClasses
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestClient
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestServer
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.google.gson.GsonBuilder
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.concurrent.TimeUnit

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
        kryoClient?.client?.stop()
        kryoServer?.server?.stop()
    }

    @Test
    fun serializationTest() {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val originalObject = newObjectUnderTest()
        val json = gson.toJson(originalObject)
        val deserializedObject: T = gson.fromJson<T>(json, clazz)
        Assertions.assertEquals(originalObject, deserializedObject)
    }

    @Test
    fun kryoSerializationTest(@TempDir tempDir: Path) {
        val kryo = Kryo()
        kryo.registerClasses()
        val originalObject = newObjectUnderTest()
        val outputFile = nextObjectPath(tempDir).toFile()
        Output(FileOutputStream(outputFile)).use {
            kryo.writeObject(it, originalObject)
        }

        Input(FileInputStream(outputFile)).use {
            val deserializedObject = kryo.readObject(it, clazz)
            Assertions.assertEquals(originalObject, deserializedObject)
        }
    }

    @Test
    fun kryoNetSerializationTest() {
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
        await().atMost(5, TimeUnit.SECONDS).until { listener2Called }
    }

    @Test
    fun isClassRegisteredInKryo() {
        val kryo = Kryo()
        kryo.registerClasses()
        kryo.isRegistrationRequired = true
        Assertions.assertDoesNotThrow { kryo.getRegistration(clazz) }
    }
}
