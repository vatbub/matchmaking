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
package com.github.vatbub.matchmaking.common.serializationtests.requests

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultPassword
import com.github.vatbub.matchmaking.testutils.TestUtils.getRandomHexString

class UpdateGameStateRequestSerializationTest :
    ServerInteractionSerializationTestSuperclass<UpdateGameStateRequest>(UpdateGameStateRequest::class.java) {
    override fun newObjectUnderTest(): UpdateGameStateRequest {
        return UpdateGameStateRequest(defaultConnectionId, defaultPassword, getRandomHexString(), GameData())
    }
}
