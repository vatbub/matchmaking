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
package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.common.responses.ServerInteractionException

/**
 * A possible superclass for all normal server responses. For non-normal responses, use [ServerInteractionException]
 */
open class ResponseImpl(override val connectionId: String?, override val className: String) : Response {
    override val protocolVersion: String
        get() = ServerInteraction.defaultProtocolVersion
    override var httpStatusCode: Int = 200
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResponseImpl

        if (connectionId != other.connectionId) return false
        if (className != other.className) return false
        if (httpStatusCode != other.httpStatusCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = connectionId?.hashCode() ?: 0
        result = 31 * result + className.hashCode()
        result = 31 * result + httpStatusCode
        return result
    }
}
