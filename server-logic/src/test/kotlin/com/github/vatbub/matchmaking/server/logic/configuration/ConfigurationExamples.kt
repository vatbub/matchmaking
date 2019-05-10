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

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

class ConfigurationExamples {
    private val gson = GsonBuilder().setPrettyPrinting().create()!!

    @Test
    fun elephantPostgresExample() {
        val jdbcConfig = JdbcConfig(
                "jdbc:postgresql://manny.db.elephantsql.com:5432/ehlblzzc",
                "ehlblzzc",
                "WLwb_lRqRPB8wkXl6yg37OyaciD1T2Ny"
        )

        val configuration = Configuration(
                IdProviderConfig(ProviderType.Jdbc, jdbcConfig),
                RoomProviderConfig(ProviderType.Jdbc, jdbcConfig)
        )
        println(gson.toJson(configuration))
    }
}
