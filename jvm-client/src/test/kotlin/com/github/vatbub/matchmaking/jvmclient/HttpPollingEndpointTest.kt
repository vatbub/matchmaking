package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.jvmclient.PollInterval.*
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

class HttpPollingEndpointTest : ClientEndpointTest<ClientEndpoint.HttpPollingEndpoint, EndpointConfiguration.HttpPollingEndpointConfig>() {
    override fun newObjectUnderTest(endpointConfiguration: EndpointConfiguration.HttpPollingEndpointConfig): ClientEndpoint.HttpPollingEndpoint =
            ClientEndpoint.HttpPollingEndpoint(endpointConfiguration)

    override fun newObjectUnderTest(): ClientEndpoint.HttpPollingEndpoint =
            ClientEndpoint.HttpPollingEndpoint(EndpointConfiguration.HttpPollingEndpointConfig(URL("http://localhost:8080/")))

    override fun newDummyServer(): DummyServer<EndpointConfiguration.HttpPollingEndpointConfig> = DummyHttpServer()

    override fun getCloneOf(instance: ClientEndpoint.HttpPollingEndpoint): ClientEndpoint.HttpPollingEndpoint =
            newObjectUnderTest(EndpointConfiguration.HttpPollingEndpointConfig(instance.configuration.hostUrl, instance.configuration.pollInterval))

    override fun notEqualsTest() {
        val firstInstance = newObjectUnderTest()
        val secondPollInterval = when (firstInstance.configuration.pollInterval){
            Fast -> ExtremelySlow
            Medium -> Fast
            Slow -> Medium
            VerySlow -> Slow
            ExtremelySlow -> VerySlow
        }
        val secondInstance = newObjectUnderTest(EndpointConfiguration.HttpPollingEndpointConfig(URL(firstInstance.configuration.hostUrl, "testPath"), secondPollInterval))
        Assertions.assertNotEquals(firstInstance, secondInstance)
    }
}