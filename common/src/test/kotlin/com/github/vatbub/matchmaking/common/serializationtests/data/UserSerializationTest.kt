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
package com.github.vatbub.matchmaking.common.serializationtests.data

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.registerClasses
import com.github.vatbub.matchmaking.common.serializationtests.SerializationTestSuperclass
import com.github.vatbub.matchmaking.common.serializationtests.nextObjectPath
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.Inet4Address
import java.net.Inet6Address
import java.nio.file.Path

class UserSerializationTest :
        SerializationTestSuperclass<User>(User::class.java) {
    override fun getCloneOf(instance: User) = instance.copy()

    override fun newObjectUnderTest(): User {
        return User(
                TestUtils.getRandomHexString(),
                "vatbub",
                Inet4Address.getByName("129.187.211.162") as Inet4Address?,
                Inet6Address.getByName("2001:4ca0:2fff:11:0:0:0:25") as Inet6Address?
        )
    }

    @Test
    fun kryoSerializationReplaceDefaultValuesTest(@TempDir tempDir: Path) {
        val kryo = Kryo()
        kryo.registerClasses()
        val originalObject = newObjectUnderTest()
        val outputFile = nextObjectPath(tempDir).toFile()
        Output(FileOutputStream(outputFile)).use {
            kryo.writeObject(it, originalObject)
        }

        Input(FileInputStream(outputFile)).use {
            val deserializedObject = kryo.readObject(it, User::class.java)
            Assertions.assertNotEquals(KryoCommon.defaultStringValueForInstantiation, deserializedObject.connectionId)
            Assertions.assertNotEquals(KryoCommon.defaultStringValueForInstantiation, deserializedObject.userName)
        }
    }

    // Already tested in UserTest
    override fun notEqualsTest() {}
}
