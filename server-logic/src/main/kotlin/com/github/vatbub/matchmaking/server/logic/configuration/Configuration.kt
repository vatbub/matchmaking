/*-
 * #%L
 * matchmaking.server
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

import com.github.vatbub.matchmaking.common.fromJson
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.ProviderType.Jdbc
import com.github.vatbub.matchmaking.server.logic.configuration.ProviderType.Memory
import com.github.vatbub.matchmaking.server.logic.idprovider.JdbcIdProvider
import com.github.vatbub.matchmaking.server.logic.idprovider.MemoryIdProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.JdbcRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import java.io.File
import java.io.FileReader
import kotlin.properties.Delegates

class ConfigurationManager {
    companion object {
        private var instance: ConfigurationManager? = null
        @JvmStatic
        fun getInstance(): ConfigurationManager {
            var instanceCopy = instance
            if (instanceCopy == null) {
                instanceCopy = ConfigurationManager()
                instance = instanceCopy
            }
            return instanceCopy
        }

        @JvmStatic
        fun resetInstance() {
            instance = null
        }

        var currentConfiguration: Configuration
            get() = getInstance().currentConfiguration
            set(value) {
                getInstance().currentConfiguration = value
            }

        val onChangeListeners
            get() = getInstance().onChangeListeners

        fun readConfigurationFile(fileToRead: File) =
                getInstance().readConfigurationFile(fileToRead)
    }

    var currentConfiguration by Delegates.observable(initializeConfig()) { _, oldValue, newValue ->
        onChangeListeners.forEach {
            it.invoke(oldValue, newValue)
        }
    }
    val onChangeListeners =
            mutableListOf<((oldValue: Configuration, newValue: Configuration) -> Unit)>()

    private fun initializeConfig(): Configuration {
        val configFromFile = readDefaultConfigurationFileIfExists()
        return configFromFile ?: Configuration()
    }

    private fun readDefaultConfigurationFileIfExists(): Configuration? {
        val configFile = JndiHelper.readJndi("configFile") ?: File("matchmakingConfig.json")

        logger.info { "Looking for the default configuration file at: ${configFile.absoluteFile}" }
        return readConfigurationFile(configFile)
    }

    fun readConfigurationFile(fileToRead: File): Configuration? {
        if (!fileToRead.exists()) {
            logger.warn { "The configuration file ${fileToRead.absolutePath} does not exist!" }
            return null
        }

        if (!fileToRead.isFile) {
            logger.warn { "The configuration file ${fileToRead.absolutePath} is not a file!" }
            return null
        }

        logger.info { "File found, reading config file... " }
        var result: Configuration? = null
        FileReader(fileToRead).use { result = fromJson(it, Configuration::class.java) }
        return result!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigurationManager

        if (onChangeListeners != other.onChangeListeners) return false

        return true
    }

    override fun hashCode(): Int {
        return onChangeListeners.hashCode()
    }
}

data class Configuration(
        val idProviderConfig: IdProviderConfig = IdProviderConfig(),
        val roomProviderConfig: RoomProviderConfig = RoomProviderConfig()
) {
    fun getAsServerContext(): ServerContext {
        val idProvider = when (idProviderConfig.providerType) {
            Memory -> MemoryIdProvider()
            Jdbc -> {
                val jdbcConfig = idProviderConfig.jdbcConfig
                        ?: throw IllegalArgumentException("Jdbc config must not be null when using a JdbcIdProvider")
                JdbcIdProvider(jdbcConfig.connectionString, jdbcConfig.dbUser, jdbcConfig.dbPassword)
            }
        }

        val roomProvider = when (roomProviderConfig.providerType) {
            Memory -> MemoryRoomProvider()
            Jdbc -> {
                val jdbcConfig = roomProviderConfig.jdbcConfig
                        ?: throw IllegalArgumentException("Jdbc config must not be null when using a JdbcRoomProvider")
                JdbcRoomProvider(jdbcConfig.connectionString, jdbcConfig.dbUser, jdbcConfig.dbPassword)
            }
        }

        return ServerContext(connectionIdProvider = idProvider, roomProvider = roomProvider)
    }
}

data class IdProviderConfig(val providerType: ProviderType = Memory, val jdbcConfig: JdbcConfig? = null)

data class RoomProviderConfig(val providerType: ProviderType = Memory, val jdbcConfig: JdbcConfig? = null)

data class JdbcConfig(val connectionString: String, val dbUser: String? = null, val dbPassword: String? = null)

enum class ProviderType {
    Memory, Jdbc
}
