/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.responses

/**
 * Returned by the server when an internal error occurs.
 *
 * # JSON example
 * ```json
 * {jsonSample}
 * ```
 *
 * @param connectionId The connection id of the requesting client
 * @param message The error/exception message
 */
class InternalServerErrorException(message: String? = null, connectionId: String? = null, responseTo: String? = null) :
        ServerInteractionException(message, 500, connectionId, InternalServerErrorException::class.qualifiedName!!, responseTo) {
    override fun copy() = InternalServerErrorException(message, connectionId, responseTo)
}
