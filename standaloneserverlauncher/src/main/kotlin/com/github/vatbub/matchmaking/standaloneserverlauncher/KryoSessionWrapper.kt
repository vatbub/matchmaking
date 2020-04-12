/*-
 * #%L
 * matchmaking.standalone-server-launcher
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
package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.esotericsoftware.kryonet.Connection
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.sockets.Session

class KryoSessionWrapper(val connection: Connection) : Session() {
    override fun sendObjectSync(objectToSend: ServerInteraction) {
        try {
            logger.debug { "Sending object: $objectToSend" }
            connection.sendUDP(objectToSend)
        } catch (e: IllegalStateException) {
            connection.sendTCP(objectToSend)
        }
    }

    override fun sendObjectAsync(objectToSend: ServerInteraction) {
        Thread { sendObjectSync(objectToSend) }.start()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KryoSessionWrapper) return false

        if (connection != other.connection) return false

        return true
    }

    override fun hashCode(): Int {
        return connection.hashCode()
    }
}
