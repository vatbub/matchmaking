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
    val ipv4Address: Inet4Address,
    val ipv6Address: Inet6Address? = null
)