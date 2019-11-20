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

import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import org.apache.mina.core.future.WriteFuture
import org.apache.mina.core.session.IoSession

class KryoSessionWrapper(val ioSession: IoSession) : Session() {
    override fun sendObjectSync(objectToSend: ServerInteraction) {
        // try {
        val future = internalSendObjectAsync(objectToSend)
        future.await()
        // } catch (e: IllegalStateException) {
        // }
    }

    override fun sendObjectAsync(objectToSend: ServerInteraction) {
        internalSendObjectAsync(objectToSend)
    }

    private fun internalSendObjectAsync(objectToSend: ServerInteraction): WriteFuture {
        logger.debug("Sending object: $objectToSend")
        return ioSession.write(objectToSend)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KryoSessionWrapper) return false

        if (ioSession != other.ioSession) return false

        return true
    }

    override fun hashCode(): Int {
        return ioSession.hashCode()
    }
}
