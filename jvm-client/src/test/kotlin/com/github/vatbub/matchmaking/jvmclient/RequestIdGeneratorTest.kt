/*-
 * #%L
 * matchmaking.jvm-client
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
package com.github.vatbub.matchmaking.jvmclient

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

class RequestIdGeneratorTest {
    @Test
    fun noDuplicateTest() {
        val usedIds = mutableListOf<String>()
        repeat(10000) {
            val newId = RequestIdGenerator.getNewId()
            Assertions.assertFalse(usedIds.contains(newId))
            usedIds.add(newId)
        }
    }

    @Test
    fun noDuplicateWithMockTest() {
        val random = CustomRandom()
        val usedIds = mutableListOf<String>()
        val repetitionCount = 10000
        repeat(repetitionCount) {
            val newId = RequestIdGenerator.getNewId(random)
            Assertions.assertFalse(usedIds.contains(newId))
            usedIds.add(newId)
        }
        Assertions.assertEquals(repetitionCount + random.numberOfCallsBeforeTrueRandomIsReturned, random.nextIntCallCount)
    }

    private class CustomRandom(val intReturnedOnFirstCalls: Int = 1, val numberOfCallsBeforeTrueRandomIsReturned: Int = 2) : Random() {
        var nextIntCallCount = 0
            private set

        override fun nextBits(bitCount: Int) = Random.nextBits(bitCount)
        override fun nextInt(): Int {
            val result =
                    if (nextIntCallCount <= numberOfCallsBeforeTrueRandomIsReturned)
                        intReturnedOnFirstCalls
                    else
                        super.nextInt()
            nextIntCallCount++
            return result
        }
    }
}
