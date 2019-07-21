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

import com.github.vatbub.matchmaking.common.toJson
import com.github.vatbub.matchmaking.server.logic.JndiTestUtils
import com.github.vatbub.matchmaking.server.logic.idprovider.JdbcIdProvider
import com.github.vatbub.matchmaking.server.logic.idprovider.MemoryIdProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.JdbcRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.FileWriter
import java.nio.file.Path
import javax.naming.InitialContext

class ConfigurationTest : KotlinTestSuperclass<ConfigurationManager>() {
    override fun getCloneOf(instance: ConfigurationManager): ConfigurationManager {
        val result = ConfigurationManager()
        instance.onChangeListeners.forEach { result.onChangeListeners.add(it) }
        result.currentConfiguration = instance.currentConfiguration
        return result
    }

    override fun newObjectUnderTest() = ConfigurationManager.getInstance()

    @BeforeEach
    fun beforeEachTest() {
        ConfigurationManager.resetInstance()
    }

    @AfterEach
    fun resetJndi() {
        JndiHelper.context = InitialContext()
    }

    @Test
    fun sameInstanceTest() {
        val instance1 = ConfigurationManager.getInstance()
        val instance2 = ConfigurationManager.getInstance()
        Assertions.assertSame(instance1, instance2)
    }

    @Test
    fun resetInstanceTest() {
        val instance1 = ConfigurationManager.getInstance()
        ConfigurationManager.resetInstance()
        val instance2 = ConfigurationManager.getInstance()
        Assertions.assertNotSame(instance1, instance2)
    }

    @Test
    fun changeListenerTest() {
        var listenerCalled = false
        val oldConfiguration = ConfigurationManager.currentConfiguration
        val newConfiguration = Configuration()
        ConfigurationManager.onChangeListeners.add { oldValue, newValue ->
            listenerCalled = true
            Assertions.assertSame(oldConfiguration, oldValue)
            Assertions.assertSame(newConfiguration, newValue)
        }

        ConfigurationManager.currentConfiguration = newConfiguration
        Assertions.assertTrue(listenerCalled)
    }

    @Test
    fun getAsServerContextMemoryConfigTest() {
        val configuration = Configuration()
        val serverContext = configuration.getAsServerContext()
        Assertions.assertTrue(serverContext.connectionIdProvider is MemoryIdProvider)
        Assertions.assertTrue(serverContext.roomProvider is MemoryRoomProvider)
    }

    @Test
    fun getAsServerContextJdbcConfigTest() {
        val jdbcConfig = JdbcConfig("jdbc:h2:mem:configTest", "SA", "")
        val configuration = Configuration(IdProviderConfig(ProviderType.Jdbc, jdbcConfig), RoomProviderConfig(ProviderType.Jdbc, jdbcConfig))
        val serverContext = configuration.getAsServerContext()
        Assertions.assertTrue(serverContext.connectionIdProvider is JdbcIdProvider)
        Assertions.assertTrue(serverContext.roomProvider is JdbcRoomProvider)
    }

    @Test
    fun getAsServerContextIllegalIdProviderJdbcConfig() {
        val configuration = Configuration(IdProviderConfig(ProviderType.Jdbc))
        Assertions.assertThrows(IllegalArgumentException::class.java) { configuration.getAsServerContext() }
    }

    @Test
    fun getAsServerContextIllegalRoomProviderJdbcConfig() {
        val configuration = Configuration(roomProviderConfig = RoomProviderConfig(ProviderType.Jdbc))
        Assertions.assertThrows(IllegalArgumentException::class.java) { configuration.getAsServerContext() }
    }

    @Test
    fun readNonExistentConfigFileTest(@TempDir tempDir: Path) {
        val nonExistentFile = tempDir.resolve("nonExistentConfig.json").toFile()!!
        Assertions.assertFalse(nonExistentFile.exists())
        Assertions.assertNull(ConfigurationManager.readConfigurationFile(nonExistentFile))
    }

    @Test
    fun readDirectoryAsConfigFileTest(@TempDir tempDir: Path) {
        val directory = tempDir.toFile()!!
        Assertions.assertTrue(directory.exists())
        Assertions.assertTrue(directory.isDirectory)
        Assertions.assertNull(ConfigurationManager.readConfigurationFile(directory))
    }

    @Test
    fun readConfigFileTest(@TempDir tempDir: Path) {
        val configFile = tempDir.resolve("config.json").toFile()!!
        val originalConfiguration = Configuration()
        FileWriter(configFile, false).use { it.write(toJson(originalConfiguration)) }
        Assertions.assertTrue(configFile.exists())
        Assertions.assertTrue(configFile.isFile)
        val readResult = ConfigurationManager.readConfigurationFile(configFile)
        Assertions.assertNotNull(readResult)
        Assertions.assertEquals(originalConfiguration, readResult)
    }

    @Test
    fun readDefaultConfigFromJndiParameterTest() {
        JndiTestUtils.mockContext(mapOf("configFile" to File(this.javaClass.getResource("dummyConfig.json").toURI())))
        val configuration = ConfigurationManager.currentConfiguration
        Assertions.assertEquals(ProviderType.Jdbc, configuration.idProviderConfig.providerType)
        Assertions.assertEquals("jdbc:postgresql://manny.db.elephantsql.com:1111/1234", configuration.idProviderConfig.jdbcConfig!!.connectionString)
        Assertions.assertEquals("user", configuration.idProviderConfig.jdbcConfig!!.dbUser)
        Assertions.assertEquals("password", configuration.idProviderConfig.jdbcConfig!!.dbPassword)

        Assertions.assertEquals(ProviderType.Jdbc, configuration.roomProviderConfig.providerType)
        Assertions.assertEquals("jdbc:postgresql://manny.db.elephantsql.com:1111/1234", configuration.roomProviderConfig.jdbcConfig!!.connectionString)
        Assertions.assertEquals("user", configuration.roomProviderConfig.jdbcConfig!!.dbUser)
        Assertions.assertEquals("password", configuration.roomProviderConfig.jdbcConfig!!.dbPassword)
    }

    @Test
    override fun notEqualsTest() {
        val manager1 = ConfigurationManager()
        val manager2 = ConfigurationManager()
        manager2.onChangeListeners.add { _, _ -> print("") }
        Assertions.assertNotEquals(manager1, manager2)
    }
}
