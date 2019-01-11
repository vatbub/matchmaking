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
package com.github.vatbub.matchmaking.common.data

import java.net.Inet4Address
import java.net.Inet6Address

/**
 * Saves connection data of a user
 * @param connectionId The user's connection id as assigned by the matchmaking server
 * @param userName The user name as was chosen by the user
 * @param ipv4Address The user's ipv4 address
 * @param ipv6Address The user's ipv6 address if he has one
 */
data class User(
    val connectionId: String,
    val userName: String,
    val ipv4Address: Inet4Address? = null,
    val ipv6Address: Inet6Address? = null
)
