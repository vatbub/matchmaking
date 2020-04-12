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
package com.github.vatbub.matchmaking.common.testing.kryo

import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.initializeMinLogRedirect
import com.github.vatbub.matchmaking.common.registerClasses
import java.net.InetAddress

class KryoTestServer(listener: Listener, val tcpPort: Int = KryoCommon.defaultTcpPort, val udpPort: Int? = null) {
    val server = Server()
    val ipAddress = InetAddress.getLocalHost()!!

    init {
        initializeMinLogRedirect()
        if (udpPort == null)
            server.bind(tcpPort)
        else
            server.bind(tcpPort, udpPort)
        server.kryo.registerClasses()
        server.addListener(listener)
        server.start()
    }
}
