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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

class IpAddressHelperTest {
    private val ipv4Sample ="98.139.180.149"
    private val ipv6Sample = "2002:4559:1FE2::4559:1FE2"

    @Test
    fun convertToIpv4Test(){
        val parsedAddress=IpAddressHelper.convertToIpv4(ipv4Sample)
        Assertions.assertEquals(
                InetAddress.getByName(ipv4Sample),
                parsedAddress
        )
        Assertions.assertTrue(parsedAddress is Inet4Address)
    }

    @Test
    fun convertToIpv6Test(){
        val parsedAddress=IpAddressHelper.convertToIpv6(ipv6Sample)
        Assertions.assertEquals(
                InetAddress.getByName(ipv6Sample),
                parsedAddress
        )
        Assertions.assertTrue(parsedAddress is Inet6Address)
    }

    @Test
    fun convertToIpv4NullInputTest() {
        Assertions.assertNull(IpAddressHelper.convertToIpv4(null))
    }

    @Test
    fun convertToIpv6NullInputTest() {
        Assertions.assertNull(IpAddressHelper.convertToIpv6(null))
    }

    @Test
    fun castToIpv4OrNullTest(){
        val inetAddress = InetAddress.getByName(ipv4Sample)
        val castedInetAddress = IpAddressHelper.castToIpv4OrNull(inetAddress)
        Assertions.assertEquals(inetAddress,castedInetAddress)
        Assertions.assertTrue(castedInetAddress is Inet4Address)
    }

    @Test
    fun castToIpv6OrNull(){
        val inetAddress = InetAddress.getByName(ipv6Sample)
        val castedInetAddress = IpAddressHelper.castToIpv6OrNull(inetAddress)
        Assertions.assertEquals(inetAddress,castedInetAddress)
        Assertions.assertTrue(castedInetAddress is Inet6Address)
    }

    @Test
    fun negativeConvertToIpv4Test(){
        val parsedAddress=IpAddressHelper.convertToIpv4(ipv6Sample)
        Assertions.assertNull(parsedAddress)
    }

    @Test
    fun negativeConvertToIpv6Test(){
        val parsedAddress=IpAddressHelper.convertToIpv6(ipv4Sample)
        Assertions.assertNull(parsedAddress)
    }

    @Test
    fun negativeCastToIpv4OrNullTest(){
        val inetAddress = InetAddress.getByName(ipv6Sample)
        val castedInetAddress = IpAddressHelper.castToIpv4OrNull(inetAddress)
        Assertions.assertNull(castedInetAddress)
    }

    @Test
    fun negativeCastToIpv6OrNull(){
        val inetAddress = InetAddress.getByName(ipv4Sample)
        val castedInetAddress = IpAddressHelper.castToIpv6OrNull(inetAddress)
        Assertions.assertNull(castedInetAddress)
    }
}
