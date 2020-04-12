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
package com.github.vatbub.matchmaking.common.testing.dummies

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.ResponseImpl

class DummyResponse(connectionId: String?, responseTo: String? = null) : ResponseImpl(connectionId, DummyResponse::class.qualifiedName!!, responseTo) {
    /**
     * Do not remove! Used by KryoNet.
     */
    @Suppress("unused")
    constructor() : this(null, null)

    override fun copy() = DummyResponse(connectionId, responseTo)

    override fun toString(): String {
        return InteractionConverter.serialize(this)
    }
}
