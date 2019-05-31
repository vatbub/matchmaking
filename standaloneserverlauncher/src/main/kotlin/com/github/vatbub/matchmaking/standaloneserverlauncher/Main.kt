/*-
 * #%L
 * matchmaking.standalone-server-launcher
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
package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.beust.jcommander.JCommander
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import org.apache.catalina.Context
import org.apache.catalina.LifecycleException
import org.apache.catalina.LifecycleState
import org.apache.catalina.connector.Connector
import org.apache.catalina.core.ApplicationContext
import org.apache.catalina.core.StandardContext
import org.apache.catalina.core.StandardServer
import org.apache.catalina.startup.Tomcat
import org.apache.catalina.users.MemoryUserDatabase
import org.apache.catalina.users.MemoryUserDatabaseFactory
import org.apache.commons.io.IOUtils
import org.apache.coyote.AbstractProtocol
import org.apache.tomcat.util.descriptor.web.LoginConfig
import org.apache.tomcat.util.descriptor.web.SecurityCollection
import org.apache.tomcat.util.descriptor.web.SecurityConstraint
import org.apache.tomcat.util.scan.StandardJarScanner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import javax.naming.CompositeName
import javax.naming.StringRefAddr
import javax.servlet.annotation.ServletSecurity


class Main {
    companion object {
        private const val authRole = "user"
        private const val warName = "/serverWar.war"

        @JvmStatic
        fun main(args: Array<String>) {
            val commandLineParams = CommandLineParams()
            @Suppress("DEPRECATION")
            val jCommander = JCommander(commandLineParams, *args)

            if (commandLineParams.help) {
                jCommander.usage()
                System.exit(1)
            }

            val tomcat = Tomcat()
            val baseDir = resolveTomcatBaseDir(commandLineParams.port, commandLineParams.tempDirectory)
            logger.debug("Tomcat base dir: $baseDir")
            tomcat.setBaseDir(baseDir)

            val port = commandLineParams.port

            if (port == null) {
                logger.error("Port not specified")
                jCommander.usage()
                System.exit(1)
                return
            }

            val configurationFileCopy = commandLineParams.configurationFile
            if (configurationFileCopy != null) {
                ConfigurationManager.readConfigurationFile(configurationFileCopy)
            }

            var kryoServer: KryoServer? = null
            if (commandLineParams.launchKryo) {
                kryoServer = KryoServer(commandLineParams.kryoTcpPort, commandLineParams.kryoUdpPort)
                addShutdownHook(kryoServer)
            }

            val nioConnector = Connector("org.apache.coyote.http11.Http11NioProtocol")
            nioConnector.port = port

            if (commandLineParams.attributes.isNotEmpty()) {
                logger.debug("Connector attributes")
                commandLineParams.attributes.forEach { key, value ->
                    logger.debug("property: $key - $value")
                    nioConnector.setProperty(key, value)
                }
            }

            if (commandLineParams.enableSSL) {
                nioConnector.secure = true
                nioConnector.setProperty("SSLEnabled", "true")
                nioConnector.setProperty("allowUnsafeLegacyRenegotiation", "false")
                val pathToTrustStore = System.getProperty("javax.net.ssl.trustStore")
                if (pathToTrustStore != null) {
                    nioConnector.setProperty("sslProtocol", "tls")
                    val truststoreFile = File(pathToTrustStore)
                    nioConnector.setAttribute("truststoreFile", truststoreFile.absolutePath)
                    logger.debug(truststoreFile.absolutePath)
                    nioConnector.setAttribute(
                            "trustStorePassword",
                            System.getProperty("javax.net.ssl.trustStorePassword")
                    )
                }
                val pathToKeystore = System.getProperty("javax.net.ssl.keyStore")
                if (pathToKeystore != null) {
                    val keystoreFile = File(pathToKeystore)
                    nioConnector.setAttribute("keystoreFile", keystoreFile.absolutePath)
                    logger.debug(keystoreFile.absolutePath)
                    nioConnector.setAttribute("keystorePass", System.getProperty("javax.net.ssl.keyStorePassword"))
                }
                if (commandLineParams.enableClientAuth) {
                    nioConnector.setAttribute("clientAuth", true)
                }
            }

            if (commandLineParams.proxyBaseUrl.isNotEmpty()) {
                val uri = URI(commandLineParams.proxyBaseUrl)
                val scheme = uri.scheme
                nioConnector.proxyName = uri.host
                nioConnector.scheme = scheme
                if (scheme == "https" && !nioConnector.secure) {
                    nioConnector.secure = true
                }
                when {
                    uri.port > 0 -> nioConnector.proxyPort = uri.port
                    scheme == "http" -> nioConnector.proxyPort = 80
                    scheme == "https" -> nioConnector.proxyPort = 443
                }
            }

            if (null != commandLineParams.uriEncoding) {
                nioConnector.uriEncoding = commandLineParams.uriEncoding
            }
            nioConnector.useBodyEncodingForURI = commandLineParams.useBodyEncodingForURI

            if (commandLineParams.enableCompression) {
                nioConnector.setProperty("compression", "on")
                nioConnector.setProperty("compressableMimeType", commandLineParams.compressableMimeTypes)
            }

            if (!commandLineParams.bindOnInit) {
                nioConnector.setProperty("bindOnInit", "false")
            }

            val maxThreads = commandLineParams.maxThreads
            if (maxThreads != null && maxThreads > 0) {
                val handler = nioConnector.protocolHandler
                if (handler is AbstractProtocol<*>) {
                    handler.setMaxThreads(maxThreads)
                } else {
                    logger.warn("Could not set maxThreads!")
                }
            }

            tomcat.connector = nioConnector

            tomcat.setPort(port)


            val context: Context

            if (commandLineParams.contextPath.isNotEmpty() && !commandLineParams.contextPath.startsWith("/")) {
                logger.warn("You entered a path: [${commandLineParams.contextPath}]. Your path should start with a '/'. Tomcat will update this for you, but you may still experience issues.")
            }

            val contextPath = commandLineParams.contextPath

            val war = exportResource(warName)
            war.deleteOnExit()

            logger.info("Adding Context $contextPath for ${war.path}")
            context = tomcat.addWebapp(contextPath, war.absolutePath)

            context as StandardContext
            context.unpackWAR = false
            val applicationContext = ApplicationContext(context)

            if (!commandLineParams.shutdownOverride) {
                // allow Tomcat to shutdown if a context failure is detected
                context.addLifecycleListener { event ->
                    if (event.lifecycle.state === LifecycleState.FAILED) {
                        val server = tomcat.server
                        if (server is StandardServer) {
                            logger.error("Context [$contextPath] failed in [${event.lifecycle::class.java.name}] lifecycle. Allowing Tomcat to shutdown.")
                            server.stopAwait()
                        }
                    }
                }
            }

            if (commandLineParams.scanBootstrapClassPath) {
                val scanner = StandardJarScanner()
                scanner.isScanBootstrapClassPath = true
                context.setJarScanner(scanner)
            }

            if (commandLineParams.contextXml != null) {
                logger.info("Using context config: ${commandLineParams.contextXml}")
                context.setConfigFile(File(commandLineParams.contextXml).toURI().toURL())
            }

            //set the session timeout
            val sessionTimeout = commandLineParams.sessionTimeout
            if (sessionTimeout != null)
                context.setSessionTimeout(sessionTimeout)

            addShutdownHook(tomcat)

            if (commandLineParams.enableNaming ||
                    commandLineParams.enableBasicAuth ||
                    commandLineParams.tomcatUsersLocation != null
            ) {
                tomcat.enableNaming()
            }

            if (commandLineParams.enableBasicAuth)
                enableBasicAuth(context, commandLineParams.enableSSL)


            if (commandLineParams.accessLog) {
                val host = tomcat.host
                val valve = StdoutAccessLogValve()
                valve.enabled = true
                valve.pattern = commandLineParams.accessLogPattern
                host.pipeline.addValve(valve)
            }

            tomcat.start()

            /*val serverContainer =
                applicationContext.getAttribute("javax.websocket.server.ServerContainer") as javax.websocket.server.ServerContainer
            serverContainer.addEndpoint(WebsocketEndpoint::class.java)*/

            /*
             * NamingContextListener.lifecycleEvent(LifecycleEvent event)
             * cannot initialize GlobalNamingContext for Tomcat until
             * the Lifecycle.CONFIGURE_START_EVENT occurs, so this block
             * must sit after the call to tomcat.start() and it requires
             * tomcat.enableNaming() to be called much earlier in the code.
            */
            if (commandLineParams.enableBasicAuth || commandLineParams.tomcatUsersLocation != null) {
                configureUserStore(tomcat, commandLineParams)
            }

            tomcat.server.await()
            kryoServer?.server?.stop()
        }

        private fun resolveTomcatBaseDir(port: Int?, tempDirectory: String?): String? {
            val baseDir = if (tempDirectory != null)
                File(tempDirectory)
            else
                File(System.getProperty("user.dir") + "/target/tomcat." + port)

            if (!baseDir.isDirectory)
                Files.createDirectories(baseDir.toPath())

            return try {
                baseDir.canonicalPath
            } catch (e: IOException) {
                baseDir.absolutePath
            }
        }

        private fun enableBasicAuth(ctx: Context, enableSSL: Boolean) {
            val loginConfig = LoginConfig()
            loginConfig.authMethod = "BASIC"
            ctx.loginConfig = loginConfig
            ctx.addSecurityRole(authRole)

            val securityConstraint = SecurityConstraint()
            securityConstraint.addAuthRole(authRole)
            if (enableSSL) {
                securityConstraint.userConstraint = ServletSecurity.TransportGuarantee.CONFIDENTIAL.toString()
            }
            val securityCollection = SecurityCollection()
            securityCollection.addPattern("/*")
            securityConstraint.addCollection(securityCollection)
            ctx.addConstraint(securityConstraint)
        }

        @Throws(Exception::class)
        private fun configureUserStore(tomcat: Tomcat, commandLineParams: CommandLineParams) {
            var tomcatUsersLocation = commandLineParams.tomcatUsersLocation
            if (tomcatUsersLocation ==
                    null
            ) {
                tomcatUsersLocation = "../../tomcat-users.xml"
            }

            val ref = javax.naming.Reference("org.apache.catalina.UserDatabase")
            ref.add(StringRefAddr("pathname", tomcatUsersLocation))
            val memoryUserDatabase = MemoryUserDatabaseFactory().getObjectInstance(
                    ref,
                    CompositeName("UserDatabase"), null, null
            ) as MemoryUserDatabase

            // Add basic auth user
            if (commandLineParams.basicAuthUser != null && commandLineParams.basicAuthPw != null) {

                memoryUserDatabase.readonly = false
                val user = memoryUserDatabase.createRole(authRole, authRole)
                memoryUserDatabase.createUser(
                        commandLineParams.basicAuthUser,
                        commandLineParams.basicAuthPw,
                        commandLineParams.basicAuthUser
                ).addRole(user)
                memoryUserDatabase.save()

            } else if (System.getenv("BASIC_AUTH_USER") != null && System.getenv("BASIC_AUTH_PW") != null) {

                memoryUserDatabase.readonly = false
                val user = memoryUserDatabase.createRole(authRole, authRole)
                memoryUserDatabase.createUser(
                        System.getenv("BASIC_AUTH_USER"),
                        System.getenv("BASIC_AUTH_PW"),
                        System.getenv("BASIC_AUTH_USER")
                ).addRole(user)
                memoryUserDatabase.save()
            }

            // Register memoryUserDatabase with GlobalNamingContext
            logger.debug("MemoryUserDatabase: $memoryUserDatabase")
            tomcat.server.globalNamingContext.addToEnvironment("UserDatabase", memoryUserDatabase)

            val ctxRes = org.apache.tomcat.util.descriptor.web.ContextResource()
            ctxRes.name = "UserDatabase"
            ctxRes.auth = "Container"
            ctxRes.type = "org.apache.catalina.UserDatabase"
            ctxRes.description = "User database that can be updated and saved"
            ctxRes.setProperty("factory", "org.apache.catalina.users.MemoryUserDatabaseFactory")
            ctxRes.setProperty("pathname", tomcatUsersLocation)
            tomcat.server.globalNamingResources.addResource(ctxRes)
            tomcat.engine.realm = org.apache.catalina.realm.UserDatabaseRealm()
        }

        private fun addShutdownHook(tomcat: Tomcat?) {
            // add shutdown hook to stop server
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    tomcat?.server?.stop()
                } catch (exception: LifecycleException) {
                    throw RuntimeException("WARNING: Cannot Stop Tomcat " + exception.message, exception)
                }
            })
        }

        private fun addShutdownHook(kryoServer: KryoServer?) {
            // add shutdown hook to stop server
            Runtime.getRuntime().addShutdownHook(Thread {
                try {
                    kryoServer?.server?.stop()
                } catch (exception: LifecycleException) {
                    throw RuntimeException("WARNING: Cannot Stop Tomcat " + exception.message, exception)
                }
            })
        }

        /**
         * Export a resource embedded into a Jar file to the local file path.
         *
         * @param resourceName ie.: "/SmartLibrary.dll"
         * @return The path to the exported resource
         * @throws Exception
         */
        private fun exportResource(resourceName: String): File {
            val jarFolder: String = File(Main::class.java.protectionDomain.codeSource.location.toURI().path).parentFile
                    .path.replace('\\', '/')


            val stream = Main::class.java.getResourceAsStream(resourceName)
                    ?: throw Exception("Cannot get resource \"$resourceName\" from Jar file.")

            stream.use { resourceInputStream ->
                val finalName = jarFolder + resourceName
                logger.debug("Extracting resource '$resourceName' to '$finalName'")
                FileOutputStream(finalName).use { resourceOutputStream ->
                    IOUtils.copy(resourceInputStream, resourceOutputStream)
                }
            }
            return File(jarFolder + resourceName)
        }
    }
}
