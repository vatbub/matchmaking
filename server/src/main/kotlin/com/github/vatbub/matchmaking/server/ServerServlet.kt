/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.IpAddressHelper
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.Configuration
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ServerServlet(initialServerContext: ServerContext? = null) :
    HttpServlet() {

    private val encoding = "UTF-8"
    var serverContext: ServerContext
        private set

    init {
        serverContext = if (initialServerContext != null)
            initialServerContext
        else {
            ConfigurationManager.onChangeListeners.add(this::reloadConfiguration)
            ConfigurationManager.currentConfiguration.getAsServerContext()
        }
        serverContext.resetMessageHandlers()
    }

    override fun destroy() {
        logger.debug("ServerServlet is being destroyed")
        super.destroy()
        ConfigurationManager.onChangeListeners.remove(this::reloadConfiguration)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reloadConfiguration(oldConfiguration: Configuration, newConfiguration: Configuration) {
        logger.info("Loading new configuration into ServerServlet...")
        serverContext = newConfiguration.getAsServerContext()
        serverContext.resetMessageHandlers()
    }

    public override fun doPost(request: HttpServletRequest?, response: HttpServletResponse?) {
        if (request == null) {
            logger.warn("Received a request that was null, please report this issue to the project maintainers!")
            return
        }
        if (response == null) {
            logger.warn("Response object was null, please report this issue to the project maintainers!")
            return
        }


        val requestBodyBuilder = StringBuilder()
        request.reader.lines().forEachOrdered { line -> requestBodyBuilder.append(line) }
        val json = requestBodyBuilder.toString()
        logger.debug("Received a HTTP POST request")
        logger.trace("Received the following request: $json")
        val concreteRequest = InteractionConverter.deserializeRequest<Request>(json)
        logger.debug("Request parsed.")

        val responseInteraction = serverContext.messageDispatcher.dispatchOrCreateException(
            concreteRequest,
            IpAddressHelper.convertToIpv4(request.remoteAddr),
            IpAddressHelper.convertToIpv6(request.remoteAddr)
        )

        logger.debug("Response generated, now serializing...")

        val responseJson = InteractionConverter.serialize(responseInteraction)
        logger.trace("The following response was generated: $responseJson")
        response.contentType = "application/json"
        response.characterEncoding = encoding
        response.status = responseInteraction.httpStatusCode

        val responseBytes = responseJson.toByteArray(charset(encoding))
        response.outputStream.write(responseBytes)

        response.outputStream.flush()
        response.outputStream.close()
        logger.debug("Response sent")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerServlet) return false

        if (serverContext != other.serverContext) return false

        return true
    }

    override fun hashCode(): Int {
        return serverContext.hashCode()
    }
}
