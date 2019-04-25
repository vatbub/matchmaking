/*-
 * #%L
 * matchmaking.server
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

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

object IpAddressHelper {
    fun convertToIpv4(ipAddress: String?): Inet4Address? {
        if (ipAddress == null) return null
        return castToIpv4OrNull(InetAddress.getByName(ipAddress))
    }

    fun convertToIpv6(ipAddress: String?): Inet6Address? {
        if (ipAddress == null) return null
        return castToIpv6OrNull(InetAddress.getByName(ipAddress))
    }

    fun castToIpv4OrNull(inetAddress: InetAddress): Inet4Address? =
            if (inetAddress is Inet4Address)
                inetAddress
            else
                null

    fun castToIpv6OrNull(inetAddress: InetAddress): Inet6Address? =
            if (inetAddress is Inet6Address)
                inetAddress
            else
                null
}
