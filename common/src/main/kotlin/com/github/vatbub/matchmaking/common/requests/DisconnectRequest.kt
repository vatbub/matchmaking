/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse

/**
 * This request shall be sent when a user wishes to disconnect from any games he is currently connected to.
 * If this user is the host of a room, that room will be destroyed.
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param password The requesting client's password as assigned by [GetConnectionIdResponse]
 * @see DisconnectResponse
 */
class DisconnectRequest(connectionId: String, password: String, requestId:String?=null) :
    Request(connectionId, password, DisconnectRequest::class.qualifiedName!!, requestId){
    private constructor():this("", "")
}
