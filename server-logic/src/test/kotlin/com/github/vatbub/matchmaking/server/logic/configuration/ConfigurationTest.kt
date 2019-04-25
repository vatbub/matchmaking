package com.github.vatbub.matchmaking.server.logic.configuration

import com.github.vatbub.matchmaking.server.logic.idprovider.JdbcIdProvider
import com.github.vatbub.matchmaking.server.logic.idprovider.MemoryIdProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.JdbcRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.google.gson.Gson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.FileWriter
import java.nio.file.Path

class ConfigurationTest {
    @BeforeEach
    fun beforeEachTest() {
        ConfigurationManager.resetInstance()
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
        val jdbcConfig = JdbcConfig("jdbc:hsqldb:mem:configTest", "SA", "")
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
        FileWriter(configFile, false).use { it.write(Gson().toJson(originalConfiguration)) }
        Assertions.assertTrue(configFile.exists())
        Assertions.assertTrue(configFile.isFile)
        val readResult = ConfigurationManager.readConfigurationFile(configFile)
        Assertions.assertNotNull(readResult)
        Assertions.assertEquals(originalConfiguration, readResult)
    }
}