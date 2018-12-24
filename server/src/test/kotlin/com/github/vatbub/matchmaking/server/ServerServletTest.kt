package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.dummies.DummyResponse
import com.github.vatbub.matchmaking.server.dummies.DynamicRequestHandler
import com.google.gson.GsonBuilder
import com.jsunsoft.http.HttpRequestBuilder
import com.jsunsoft.http.NoSuchContentException
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

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

class ServerServletTest : KotlinTestSuperclass() {

    private val tomcatPort: Int = 9999
    private val apiSuffix: String = "matchmaking"
    private val api: ServerServlet = ServerServlet()


    init {
        TomcatTestUtils(tomcatPort, "", "ServerServlet", api, "/$apiSuffix")
    }

    val gson = GsonBuilder().setPrettyPrinting().create()
    val connectionId = (4567876543).toString(16)

    @BeforeEach
    fun resetServer() {
        api.resetHandlers()
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
        val responseJson = httpRequest.executeWithBody(json).get()
        println("Received the following json:\n$responseJson")
        return responseJson
    }

    private fun <T : Response> doRequest(request: ServerInteraction): T {
        val json = doRequest(gson.toJson(request))
        val response = gson.fromJson<Response>(json, ResponseImpl::class.java)
        val clazz = Class.forName(response.className)
        return gson.fromJson<T>(json, clazz)
    }

    @Test
    fun noResponseTest() {
        try {
            doRequest<DummyResponse>(DummyRequest(connectionId))
            Assert.fail("NoSuchContentException expected")
        } catch (e: NoSuchContentException) {
            println("Expected NoSuchContentException was thrown")
            e.printStackTrace(System.out)
        }
    }

    @Test
    fun requestCastTest() {
        val request = DummyRequest(connectionId)
        val handler = DynamicRequestHandler({ true }, { requestToHandle ->
            Assert.assertTrue(requestToHandle is DummyRequest)
            DummyResponse(requestToHandle.connectionId)
        })
        ServerContext.currentMessageDispatcher.registerHandler(handler)
        val response = doRequest<DummyResponse>(request)

        Assert.assertEquals(connectionId, response.connectionId)
    }
}
