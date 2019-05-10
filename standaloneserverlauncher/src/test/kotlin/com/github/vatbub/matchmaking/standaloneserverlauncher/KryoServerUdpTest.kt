package com.github.vatbub.matchmaking.standaloneserverlauncher

import org.junit.jupiter.api.Disabled

@Disabled
class KryoServerUdpTest : KryoServerTest() {
    override fun useUdp() = true
}