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

import com.beust.jcommander.DynamicParameter
import com.beust.jcommander.Parameter
import com.github.vatbub.matchmaking.common.KryoCommon
import java.io.File
import java.util.*


class CommandLineParams {
    @Parameter(names = ["--configurationFile"])
    var configurationFile: File? = null

    @Parameter(names = ["--launchKryo"], description = "If set to true, a kryo net server will be launched to listen on tcp or udp.")
    var launchKryo = false

    @Parameter(names = ["--kryoTcpPort"])
    var kryoTcpPort = KryoCommon.defaultTcpPort

    @Parameter(names = ["--kryoUdpPort"])
    var kryoUdpPort: Int? = null

    @Parameter(
            names = ["--session-timeout"],
            description = "The number of minutes of inactivity before a user's session is timed out."
    )
    var sessionTimeout: Int? = null

    @Parameter(names = ["--port"], description = "The port that the server will accept http requests on.")
    var port: Int? = 8080

    @Parameter(names = ["--context-xml"], description = "The path to the context xml to use.")
    var contextXml: String? = null

    @Parameter(names = ["--path"], description = "The context path")
    var contextPath = ""

    @Parameter(
            names = ["--shutdown-override"],
            description = "Overrides the default behavior and causes Tomcat to ignore lifecycle failure events rather than shutting down when they occur."
    )
    var shutdownOverride = false

    @Parameter(names = ["--help"], help = true)
    var help: Boolean = false

    @Parameter(names = ["--enable-compression"], description = "Enable GZIP compression on responses")
    var enableCompression: Boolean = false

    @Parameter(
            names = ["--compressable-mime-types"],
            description = "Comma delimited list of mime types that will be compressed when using GZIP compression."
    )
    var compressableMimeTypes =
            "text/html,text/xml,text/plain,text/css,application/json,application/xml,text/javascript,application/javascript"

    @Parameter(
            names = ["--enable-ssl"],
            description = "Specify -Djavax.net.ssl.keyStore, -Djavax.net.ssl.keystoreStorePassword, -Djavax.net.ssl.trustStore and -Djavax.net.ssl.trustStorePassword in JAVA_OPTS. Note: should not be used if a reverse proxy is terminating SSL for you (such as on Heroku)"
    )
    var enableSSL: Boolean = false

    @Parameter(
            names = ["--enable-client-auth"],
            description = "Specify -Djavax.net.ssl.keyStore and -Djavax.net.ssl.keyStorePassword in JAVA_OPTS"
    )
    var enableClientAuth: Boolean = false

    @Parameter(
            names = ["--enable-basic-auth"],
            description = "Secure the app with basic auth. Use with --basic-auth-user and --basic-auth-pw or --tomcat-users-location"
    )
    var enableBasicAuth = false

    @Parameter(
            names = ["--basic-auth-user"],
            description = "Username to be used with basic auth. Defaults to BASIC_AUTH_USER env variable."
    )
    var basicAuthUser: String? = null

    @Parameter(
            names = ["--basic-auth-pw"],
            description = "Password to be used with basic auth. Defaults to BASIC_AUTH_PW env variable."
    )
    var basicAuthPw: String? = null

    @Parameter(
            names = ["--tomcat-users-location"],
            description = "Location of the tomcat-users.xml file. (relative to the location of the webapp-runner jar file)"
    )
    var tomcatUsersLocation: String? = null

    @Parameter(names = ["--uri-encoding"], description = "Set the URI encoding to be used for the Connector.")
    var uriEncoding: String? = null

    @Parameter(
            names = ["--use-body-encoding-for-uri"],
            description = "Set if the entity body encoding should be used for the URI."
    )
    var useBodyEncodingForURI = false

    @Parameter(names = ["--scanBootstrapClassPath"], description = "Set jar scanner scan bootstrap classpath.")
    var scanBootstrapClassPath = false

    @Parameter(
            names = ["--temp-directory"],
            description = "Define the temp directory, default value: ./target/tomcat.PORT"
    )
    var tempDirectory: String? = null

    @Parameter(
            names = ["--bind-on-init"],
            description = "Controls when the socket used by the connector is bound. By default it is bound when the connector is initiated and unbound when the connector is destroyed., default value: true",
            arity = 1
    )
    var bindOnInit = true

    @Parameter(names = ["--proxy-base-url"], description = "Set proxy URL if tomcat is running behind reverse proxy")
    var proxyBaseUrl = ""

    @Parameter(names = ["--max-threads"], description = "Set the maximum number of worker threads")
    var maxThreads: Int? = 0

    @DynamicParameter(
            names = ["-A"],
            description = "Allows setting HTTP connector attributes. For example: -Acompression=on"
    )
    var attributes: Map<String, String> = HashMap()

    @Parameter(names = ["--enable-naming"], description = "Enables JNDI naming")
    var enableNaming = false

    @Parameter(names = ["--access-log"], description = "Enables AccessLogValue to STDOUT")
    var accessLog = false

    @Parameter(names = ["--access-log-pattern"], description = "If --access-log is enabled, sets the logging pattern")
    var accessLogPattern = "common"
}
