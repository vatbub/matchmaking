/*-
 * #%L
 * matchmaking.jvm-client
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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
package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.jvmclient.PollInterval.*
import com.github.vatbub.matchmaking.jvmclient.endpoints.HttpPollingEndpoint
import fi.iki.elonen.NanoHTTPD
import org.junit.jupiter.api.Assertions
import java.net.URL

private class DummyHttpServer : DummyServer<EndpointConfiguration.HttpPollingEndpointConfig> {
    private val httpServer = object : NanoHTTPD(endpointConfiguration.finalUrl.port) {
        override fun serve(uri: String, method: Method, headers: MutableMap<String, String>, parms: MutableMap<String, String>, files: MutableMap<String, String>): Response {
            logger.info { "Dummy http server received a request. Request uri: $uri; Request method: $method" }
            headers.forEach { (header, value) ->
                logger.info { "Header name: \"$header\"; Header value: \"$value\"" }
            }
            parms.forEach { (parameter, value) ->
                logger.info { "Parameter name: \"$parameter\"; Parameter value: \"$value\"" }
            }
            files.forEach { (file, value) ->
                logger.info { "File name: \"$file\"; File value: \"$value\"" }
            }

            val requestJson = files["postData"]!!
            val response = dummyMessageGenerator(InteractionConverter.deserialize(requestJson))

            return newFixedLengthResponse(Response.Status.OK, "application/json", InteractionConverter.serialize(response))
        }
    }

    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.HttpPollingEndpointConfig
        get() = EndpointConfiguration.HttpPollingEndpointConfig(URL("http://localhost:8080/"))
    override val isRunning: Boolean
        get() = httpServer.isAlive

    override fun start() {
        httpServer.start()
    }

    override fun stop() {
        httpServer.stop()
    }

}

class HttpPollingEndpointTest : ClientEndpointTest<HttpPollingEndpoint, EndpointConfiguration.HttpPollingEndpointConfig>() {
    override fun newObjectUnderTest(
            endpointConfiguration: EndpointConfiguration.HttpPollingEndpointConfig,
            onException: (Throwable) -> Unit
    ): HttpPollingEndpoint =
            HttpPollingEndpoint(endpointConfiguration, onException)

    override fun newObjectUnderTest(onException: (Throwable) -> Unit): HttpPollingEndpoint =
            HttpPollingEndpoint(EndpointConfiguration.HttpPollingEndpointConfig(URL("http://localhost:8080/"))) {
                throw it
            }

    override fun newDummyServer(): DummyServer<EndpointConfiguration.HttpPollingEndpointConfig> = DummyHttpServer()

    override fun getCloneOf(instance: HttpPollingEndpoint): HttpPollingEndpoint =
            newObjectUnderTest(EndpointConfiguration.HttpPollingEndpointConfig(instance.configuration.hostUrl, instance.configuration.pollInterval), instance.onExceptionHappened)

    override fun notEqualsTest() {
        val firstInstance = newObjectUnderTest()
        val secondPollInterval = when (firstInstance.configuration.pollInterval) {
            Fast -> ExtremelySlow
            Medium -> Fast
            Slow -> Medium
            VerySlow -> Slow
            ExtremelySlow -> VerySlow
        }
        val secondInstance = newObjectUnderTest(EndpointConfiguration.HttpPollingEndpointConfig(URL(firstInstance.configuration.hostUrl, "testPath"), secondPollInterval), firstInstance.onExceptionHappened)
        Assertions.assertNotEquals(firstInstance, secondInstance)
    }
}
