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
package com.github.vatbub.matchmaking.server

import javax.websocket.ContainerProvider

class WebsocketEndpointTest {
    private val tomcatPort: Int = 9999
    private val websocketEndpoint: String = "websocket"
    private val serverContext = ServerContext()
    // private val tomcatTestUtils = TomcatTestUtils(tomcatPort, "", "ServerServlet", api, "/$websocketEndpoint")


    private val container = ContainerProvider.getWebSocketContainer()
    private val clientEndpoint = container.connectToServer(WebsocketEndpoint::class.java, null)
}
