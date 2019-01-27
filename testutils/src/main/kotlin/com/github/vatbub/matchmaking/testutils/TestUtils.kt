/*-
 * #%L
 * matchmaking.test-utils
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
package com.github.vatbub.matchmaking.testutils

import kotlin.random.Random

object TestUtils {
    val defaultConnectionId = getRandomHexString()
    val defaultPassword = getRandomHexString()

    fun getRandomHexString(): String {
        return Random.nextInt().toString(16)
    }

    fun getRandomHexString(vararg disallowedStrings: String?): String {
        var result: String
        do {
            result = getRandomHexString()
        } while (disallowedStrings.asList().contains(result))

        return result
    }
}
