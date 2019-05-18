package com.github.vatbub.matchmaking.common.data

import com.github.vatbub.matchmaking.common.defaultInet4Address
import com.github.vatbub.matchmaking.common.defaultInet6Address
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserTest : KotlinTestSuperclass<User>() {
    override fun newObjectUnderTest() =
            User(TestUtils.defaultConnectionId, "vatbub")

    override fun getCloneOf(instance: User) =
            User(instance.connectionId, instance.userName, instance.ipv4Address, instance.ipv6Address)

    @Test
    fun defaultParamTest() {
        val userName = "vatbub"
        val user = User(TestUtils.defaultConnectionId, userName)
        Assertions.assertEquals(TestUtils.defaultConnectionId, user.connectionId)
        Assertions.assertEquals(userName, user.userName)
        Assertions.assertNull(user.ipv4Address)
        Assertions.assertNull(user.ipv6Address)
    }

    @Test
    fun ipv4GetTest() {
        val user = User(TestUtils.defaultConnectionId, "vatbub", ipv4Address = defaultInet4Address)
        Assertions.assertEquals(defaultInet4Address, user.ipv4Address)
    }

    @Test
    fun ipv6GetTest() {
        val user = User(TestUtils.defaultConnectionId, "vatbub", ipv6Address = defaultInet6Address)
        Assertions.assertEquals(defaultInet6Address, user.ipv6Address)
    }

    @Test
    override fun notEqualsTest() {
        val user1 = newObjectUnderTest()
        val user2 = User(user1.connectionId, TestUtils.getRandomHexString(user1.userName), user1.ipv4Address, user1.ipv6Address)
        Assertions.assertNotEquals(user1, user2)
    }
}