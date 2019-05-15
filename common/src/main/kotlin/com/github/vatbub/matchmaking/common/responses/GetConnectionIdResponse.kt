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
import com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest

/**
 * Response to [GetConnectionIdRequest] which contains the connection id assigned by the server.
 * @param connectionId The assigned id. This id shall be used in all further requests.
 * @param password The assigned password. This password shall be used in all further requests to authenticate the client. Failure to do so will result in an [AuthorizationException]
 */
class GetConnectionIdResponse(connectionId: String, val password: String, responseTo: String? = null) :
        ResponseImpl(connectionId, GetConnectionIdResponse::class.qualifiedName!!, responseTo) {
    override fun copy() = GetConnectionIdResponse(connectionId!!, password, responseTo)

    private constructor() : this("", "")
}
