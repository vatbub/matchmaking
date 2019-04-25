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
package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.common.responses.InternalServerErrorException
import com.github.vatbub.matchmaking.common.responses.ServerInteractionException
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DynamicRequestHandler
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jsunsoft.http.HttpRequestBuilder
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class ServerServletTest : KotlinTestSuperclass() {

    private val tomcatPort: Int = 9999
    private val apiSuffix: String = "matchmaking"
    private val serverContext = ServerContext()
    private val api: ServerServlet = ServerServlet(serverContext)
    private val tomcatTestUtils = TomcatTestUtils(tomcatPort, "", "ServerServlet", api, "/$apiSuffix")

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val connectionId = (4567876543).toString(16)
    private val password = (5638290234).toString(16)

    @BeforeEach
    fun resetServer() {
        api.serverContext.resetMessageHandlers()
    }

    @AfterAll
    fun shutServerDown() {
        tomcatTestUtils.shutTomcatDown()
    }

    private fun doRequest(json: String): String {
        println("Sending the following json:")
        println(json)
        val httpRequest = HttpRequestBuilder.createPost(
                URL(URL("http", "localhost", tomcatPort, ""), apiSuffix).toURI(),
                String::class.java
        ).addDefaultHeader("Content-Type", "application/json; charset=UTF-8")
                .responseDeserializer { responseContext ->
                    val contentTypeHeader = responseContext.httpResponse.getFirstHeader("charset")
                    var encoding: String? = null
                    if (contentTypeHeader != null) {
                        val contentTypeParts = contentTypeHeader.value.split(";")
                        for (contentTypePart in contentTypeParts)
                            if (contentTypePart.startsWith("charset="))
                                encoding =
                                        contentTypePart.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    }

                    if (encoding == null)
                        encoding = "UTF-8"

                    val responseString = IOUtils.toString(responseContext.content, encoding)
                    responseString
                }.build()
        val response = httpRequest.executeWithBody(json)
        val responseJson = if (response.hasContent())
            response.get()
        else
            response.errorText

        println("Received the following json:\n$responseJson")
        return responseJson
    }

    private fun <T : Response> doRequest(request: ServerInteraction): T {
        val json = doRequest(gson.toJson(request))
        val response = gson.fromJson<Response>(json, ResponseImpl::class.java)
        val clazz = Class.forName(response.className)
        return gson.fromJson<T>(json, clazz)
    }

    private fun doRequestNoTypeParam(request: ServerInteraction): Response {
        val json = doRequest(gson.toJson(request))
        val response = gson.fromJson<Response>(json, ResponseImpl::class.java)
        val clazz = Class.forName(response.className)
        return gson.fromJson<Response>(json, clazz)
    }

    @Test
    fun noResponseTest() {
        val response = doRequestNoTypeParam(
                DummyRequest(
                        connectionId,
                        password
                )
        )
        assertExceptionResponse(
                InternalServerErrorException(),
                IllegalStateException("No response generated by server"),
                500,
                response
        )
    }

    @Test
    fun illegalArgumentExceptionTest() {
        val expectedInnerException = java.lang.IllegalArgumentException("Test exception")
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ ->
            throw expectedInnerException
        })
        serverContext.messageDispatcher.registerHandler(handler)

        val response = doRequestNoTypeParam(
                DummyRequest(
                        connectionId,
                        password
                )
        )
        assertExceptionResponse(BadRequestException(), expectedInnerException, 400, response)
    }

    @Test
    fun internalServerErrorExceptionTest() {
        val expectedInnerException = ArrayIndexOutOfBoundsException("Test exception")
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ ->
            throw expectedInnerException
        })
        serverContext.messageDispatcher.registerHandler(handler)

        val response = doRequestNoTypeParam(
                DummyRequest(
                        connectionId,
                        password
                )
        )
        assertExceptionResponse(InternalServerErrorException(), expectedInnerException, 500, response)
    }

    private fun assertExceptionResponse(
            expectedOuterException: ServerInteractionException,
            expectedInnerException: Throwable,
            expectedHttpStatusCode: Int,
            actualResponse: Response
    ) {
        assertExceptionResponse(
                expectedOuterException,
                expectedHttpStatusCode,
                """${expectedInnerException.javaClass.name}, ${expectedInnerException.message}""",
                actualResponse
        )
    }

    private fun assertExceptionResponse(
            expectedOuterException: ServerInteractionException,
            expectedHttpStatusCode: Int,
            expectedExceptionMessage: String,
            actualResponse: Response
    ) {
        Assertions.assertEquals(expectedHttpStatusCode, actualResponse.httpStatusCode)
        Assertions.assertEquals(expectedOuterException.className, actualResponse.className)
        Assertions.assertEquals(expectedExceptionMessage, (actualResponse as ServerInteractionException).message)
    }
}
