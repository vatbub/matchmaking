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
import com.github.vatbub.matchmaking.common.Response

/**
 * Interface for a class which can handle requests sent to the server
 */
interface RequestHandler {
    /**
     * Specifies whether this candler is able to handle the specified request.
     *
     * **IMPORTANT:** The [MessageDispatcher] iterates through all registered handlers and calls this method on them.
     * The first handler which returns `true` will actually get to handle the request. This means in turn that, if
     * multiple handlers can handle the same type of request, only one of them will get to handle it.
     *
     * @return `true` if the handler is able to handle this request, `false` otherwise.
     */
    fun canHandle(request: Request):Boolean

    /**
     * Handles the specified request, acts upon it and generates a response for it.
     * @param request The request to be handled
     * @return The response to that request
     */
    fun handle(request: Request): Response
}
