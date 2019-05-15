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
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.responses.AuthorizationException
import com.github.vatbub.matchmaking.common.responses.UnknownConnectionIdException
import com.github.vatbub.matchmaking.server.logic.MessageDispatcher
import java.net.Inet4Address
import java.net.Inet6Address

/**
 * Interface for a class which can handle requests sent to the server
 */
interface RequestHandler<TRequest : Request> {
    /**
     * Specifies whether this handler is able to handle the specified request.
     *
     * **IMPORTANT:** The [MessageDispatcher] iterates through all registered handlers and calls this method on them.
     * The first handler which returns `true` will then get to handle the request. This means in turn that, if
     * multiple handlers can handle the same type of request, only one of them will get to handle it.
     *
     * @return `true` if the handler is able to handle this request, `false` otherwise.
     */
    fun canHandle(request: Request): Boolean

    /**
     * Specifies whether the handler expects this particular request to be authenticated.
     * If [needsAuthentication] returns `true` and the request fails to authenticate, an [UnknownConnectionIdException] or
     * [AuthorizationException] will be returned to the client accordingly.
     *
     * **IMPORTANT:** This method shall only determine whether the request must be authenticated, it does **NOT**
     * need to determine whether the request is actually authenticated, this is done by the [MessageDispatcher]
     *
     * This method is only called if [canHandle] returned `true` for this particular request.
     *
     * @param request The request to decide on whether it needs authentication or not
     * @return `true` if the request shall be authenticated, `false` if no authentication is required
     */
    fun needsAuthentication(request: TRequest): Boolean

    /**
     * Handles the specified request, acts upon it and generates a response for it.
     * @param request The request to be handled
     * @return The response to that request
     */
    fun handle(request: TRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response
}
