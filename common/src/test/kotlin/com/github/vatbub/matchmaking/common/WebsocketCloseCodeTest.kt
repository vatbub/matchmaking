/*-
 * #%L
 * matchmaking.common
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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

import com.github.vatbub.matchmaking.common.WebsocketCloseCode.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebsocketCloseCodeTest {
    @Test
    fun normalClosureCodeTest() {
        assertEquals(1000, NORMAL_CLOSURE.code)
    }

    @Test
    fun goingAwayCodeTest() {
        assertEquals(1001, GOING_AWAY.code)
    }

    @Test
    fun protocolErrorCodeTest() {
        assertEquals(1002, PROTOCOL_ERROR.code)
    }

    @Test
    fun cannotAcceptCodeTest() {
        assertEquals(1003, CANNOT_ACCEPT.code)
    }

    @Test
    fun reservedCodeTest() {
        assertEquals(1004, RESERVED.code)
    }

    @Test
    fun noStatusCodeCodeTest() {
        assertEquals(1005, NO_STATUS_CODE.code)
    }

    @Test
    fun closedAbnormallyCodeTest() {
        assertEquals(1006, CLOSED_ABNORMALLY.code)
    }

    @Test
    fun notConsistentCodeTest() {
        assertEquals(1007, NOT_CONSISTENT.code)
    }

    @Test
    fun violatedPolicyCodeTest() {
        assertEquals(1008, VIOLATED_POLICY.code)
    }

    @Test
    fun tooBigCodeTest() {
        assertEquals(1009, TOO_BIG.code)
    }

    @Test
    fun noExtensionCodeTest() {
        assertEquals(1010, NO_EXTENSION.code)
    }

    @Test
    fun unexpectedConditionCodeTest() {
        assertEquals(1011, UNEXPECTED_CONDITION.code)
    }

    @Test
    fun serviceRestartCodeTest() {
        assertEquals(1012, SERVICE_RESTART.code)
    }

    @Test
    fun tryAgainLaterCodeTest() {
        assertEquals(1013, TRY_AGAIN_LATER.code)
    }

    @Test
    fun tlsHandshakeFailureCodeTest() {
        assertEquals(1015, TLS_HANDSHAKE_FAILURE.code)
    }

    @Test
    fun normalClosureMeaningTest() {
        assertEquals("Normal closure", NORMAL_CLOSURE.meaning)
    }

    @Test
    fun goingAwayMeaningTest() {
        assertEquals("Going away", GOING_AWAY.meaning)
    }

    @Test
    fun protocolErrorMeaningTest() {
        assertEquals("Protocol error", PROTOCOL_ERROR.meaning)
    }

    @Test
    fun cannotAcceptMeaningTest() {
        assertEquals("Cannot accept", CANNOT_ACCEPT.meaning)
    }

    @Test
    fun reservedMeaningTest() {
        assertEquals("Reserved", RESERVED.meaning)
    }

    @Test
    fun noStatusCodeMeaningTest() {
        assertEquals("No status code specified", NO_STATUS_CODE.meaning)
    }

    @Test
    fun closedAbnormallyMeaningTest() {
        assertEquals("Closed abnormally", CLOSED_ABNORMALLY.meaning)
    }

    @Test
    fun notConsistentMeaningTest() {
        assertEquals("Not consistent", NOT_CONSISTENT.meaning)
    }

    @Test
    fun violatedPolicyMeaningTest() {
        assertEquals("Violated policy", VIOLATED_POLICY.meaning)
    }

    @Test
    fun tooBigMeaningTest() {
        assertEquals("Too big", TOO_BIG.meaning)
    }

    @Test
    fun noExtensionMeaningTest() {
        assertEquals("No extension", NO_EXTENSION.meaning)
    }

    @Test
    fun unexpectedConditionMeaningTest() {
        assertEquals("Internal error", UNEXPECTED_CONDITION.meaning)
    }

    @Test
    fun serviceRestartMeaningTest() {
        assertEquals("Service restart", SERVICE_RESTART.meaning)
    }

    @Test
    fun tryAgainLaterMeaningTest() {
        assertEquals("Try again later", TRY_AGAIN_LATER.meaning)
    }

    @Test
    fun tlsHandshakeFailureMeaningTest() {
        assertEquals("Tls handshake failed", TLS_HANDSHAKE_FAILURE.meaning)
    }

    @Test
    fun fromCodeLegalValuesTest() {
        WebsocketCloseCode.values().forEach { closeCode ->
            assertEquals(closeCode, WebsocketCloseCode.fromCode(closeCode.code))
        }
    }

    @Test
    fun fromCodeIllegalValuesTest() {
        val legalCodes = WebsocketCloseCode.values().map { it.code }
        val illegalValues =
                (legalCodes.minOrNull()!! - 10..legalCodes.maxOrNull()!! + 10)
                        .filterNot { legalCodes.contains(it) }
        illegalValues.forEach { code ->
            Assertions.assertThrows(NoSuchElementException::class.java) { WebsocketCloseCode.fromCode(code) }
        }
    }
}
