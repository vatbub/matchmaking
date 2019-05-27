package com.github.vatbub.matchmaking.common.serializationtests

import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.kryoSafeListOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KryoSafeListOfSerializationTest : SerializationTestSuperclass<List<*>>(List::class.java) {
    override fun newObjectUnderTest() =
            kryoSafeListOf("el1", "el2", "el3")

    override fun getCloneOf(instance: List<*>) =
            kryoSafeListOf(*instance.toTypedArray())

    @Test
    override fun notEqualsTest() {
        val list1 = newObjectUnderTest()
        val list2 = kryoSafeListOf("el1", "el2", "el3", "el4")
        Assertions.assertNotEquals(list1, list2)
    }
}

class KryoCommonTest {
    @Test
    fun isDefaultTcpPortNotPrivilegedOnLinux() {
        Assertions.assertTrue(KryoCommon.defaultTcpPort > 1024)
    }

    @Test
    fun defaultStringValueForInstantiationIsRecognizable() {
        Assertions.assertTrue(KryoCommon.defaultStringValueForInstantiation.contains("kryoDefaultValue"))
    }
}