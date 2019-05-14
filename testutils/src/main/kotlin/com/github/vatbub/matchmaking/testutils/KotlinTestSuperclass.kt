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
package com.github.vatbub.matchmaking.testutils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KotlinTestSuperclass<T> {
    abstract fun newObjectUnderTest(): T
    @Test
    fun equalsSameInstanceTest() {
        val instance = newObjectUnderTest()
        Assertions.assertEquals(instance, instance)
    }

    @Test
    fun notEqualsOtherClassTest() {
        val instance = newObjectUnderTest()
        Assertions.assertNotEquals(DummyClass(), instance)
    }

    @Test
    fun notEqualsToNullTest() {
        val instance = newObjectUnderTest()
        Assertions.assertNotEquals(null, instance)
    }
}

class DummyClass
