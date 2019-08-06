package com.github.vatbub.matchmaking.jvmclient

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.github.vatbub.matchmaking.common.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private class DummyKryoServer : DummyServer<EndpointConfiguration.KryoEndpointConfiguration> {
    val kryoServer = Server()
    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration
        get() = EndpointConfiguration.KryoEndpointConfiguration("localhost")

    override fun start() {
        kryoServer.kryo.registerClasses()
        val configuration = endpointConfiguration
        val udpPort = configuration.udpPort
        kryoServer.start()
        if (udpPort == null)
            kryoServer.bind(configuration.tcpPort)
        else
            kryoServer.bind(configuration.tcpPort, udpPort)


        kryoServer.addListener(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                logger.info("Dummy server: Received: $receivedObject")
                if (receivedObject is FrameworkMessage.KeepAlive) return
                receivedObject as Request
                val response = dummyMessageGenerator(receivedObject)
                try {
                    connection!!.sendUDP(response)
                } catch (e: IllegalStateException) {
                    connection!!.sendTCP(response)
                }
            }
        })
    }

    override fun stop() {
        kryoServer.stop()
    }
}

class KryoEndpointTest : ClientEndpointTest<ClientEndpoint.KryoEndpoint, EndpointConfiguration.KryoEndpointConfiguration>(DummyKryoServer()) {
    override fun newObjectUnderTest(endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration) =
            ClientEndpoint.KryoEndpoint(endpointConfiguration)

    override fun newObjectUnderTest() =
            newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost"))

    override fun getCloneOf(instance: ClientEndpoint.KryoEndpoint): ClientEndpoint.KryoEndpoint {
        return newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration(instance.configuration.host, instance.configuration.tcpPort, instance.configuration.udpPort))
    }

    @Test
    override fun notEqualsTest() {
        Assertions.assertNotEquals(newObjectUnderTest(), newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort + 1, KryoCommon.defaultTcpPort + 2, 5003)))
    }
}