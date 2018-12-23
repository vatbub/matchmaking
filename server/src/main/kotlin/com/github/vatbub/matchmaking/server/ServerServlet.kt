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

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.server.handlers.GetConnectionIdHandler
import com.google.gson.Gson
import java.lang.Class.forName
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ServerServlet : HttpServlet() {
    private val gson = Gson()

    init {
        ServerContext.currentMessageDispatcher.registerHandler(GetConnectionIdHandler())
    }

    override fun doPost(request: HttpServletRequest?, resp: HttpServletResponse?) {
        if (request == null)
            return

        val requestBodyBuilder = StringBuilder()
        request.reader.lines().forEachOrdered { line -> requestBodyBuilder.append(line) }
        val requestBody = requestBodyBuilder.toString()

        val abstractRequest: Request = gson.fromJson<Request>(requestBody, Request::class.java)

        val requestClass = forName(abstractRequest.className)
        val concreteRequest = gson.fromJson(requestBody, requestClass) as Request

        ServerContext.currentMessageDispatcher.dispatch(concreteRequest)
    }
}
