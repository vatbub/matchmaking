/*-
 * #%L
 * matchmaking.server-logic
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
package com.github.vatbub.matchmaking.server.logic

import com.github.vatbub.matchmaking.server.logic.configuration.JndiHelper
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import javax.naming.InitialContext

object JndiTestUtils {
    fun <T> mockContext(environment: Map<String, T>, prefixKeys: Boolean = true) {
        val context = mock(InitialContext::class.java)
        environment.forEach { (key, value) -> `when`(context.lookup(generatePrefixedKeyIfApplicable(key, prefixKeys))).thenReturn(value) }
        JndiHelper.context = context
    }

    private fun generatePrefixedKeyIfApplicable(key: String, prefix: Boolean) =
            if (prefix)
                "java:comp/env/$key"
            else
                key

    fun resetContext() {
        JndiHelper.context = InitialContext()
    }
}
