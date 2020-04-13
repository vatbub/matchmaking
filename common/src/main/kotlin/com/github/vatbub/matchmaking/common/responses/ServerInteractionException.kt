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

import com.github.vatbub.matchmaking.common.ResponseImpl

/**
 * Superclass for exceptions that can be sent over the network
 * @param message The error/exception message
 *
 * # JSON example
 * ```json
 * {jsonSample}
 * ```
 */
abstract class ServerInteractionException(
        val message: String?,
        httpStatusCode: Int,
        connectionId: String?,
        className: String,
        responseTo: String?
) : ResponseImpl(connectionId, className, responseTo) {
    init {
        super.httpStatusCode = httpStatusCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ServerInteractionException

        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }

}
