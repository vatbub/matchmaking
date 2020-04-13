package com.github.vatbub.matchmaking.common

enum class WebsocketCloseCode(val code: Int, val meaning: String) {
    NORMAL_CLOSURE(1000, "Normal closure"),
    GOING_AWAY(1001, "Going away"),
    PROTOCOL_ERROR(1002, "Protocol error"),
    CANNOT_ACCEPT(1003, "Cannot accept"),
    RESERVED(1004, "Reserved"),
    NO_STATUS_CODE(1005, "No status code specified"),
    CLOSED_ABNORMALLY(1006, "Closed abnormally"),
    NOT_CONSISTENT(1007, "Not consistent"),
    VIOLATED_POLICY(1008, "Violated policy"),
    TOO_BIG(1009, "Too big"),
    NO_EXTENSION(1010, "No extension"),
    UNEXPECTED_CONDITION(1011, "Internal error"),
    SERVICE_RESTART(1012, "Service restart"),
    TRY_AGAIN_LATER(1013, "Try again later"),
    TLS_HANDSHAKE_FAILURE(1015, "Tls handshake failed");

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

    companion object {
        fun fromCode(code: Int): WebsocketCloseCode =
                values().firstOrNull { it.code == code }
                        ?: throw NoSuchElementException("Close code $code is undefined")
    }
}
