/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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
package com.github.vatbub.matchmaking.server

import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import javax.servlet.Servlet

open class TomcatTestUtils(
    tomcatPort: Int,
    contextPath: String,
    servletName: String,
    servlet: Servlet,
    servletPattern: String
) {
    @Suppress("MemberVisibilityCanBePrivate")
    val tomcat = Tomcat()
    private var destinationPath: Path
    private val webappsPath: Path
    private val baseDir: Path
    @Suppress("MemberVisibilityCanBePrivate")
    var context: Context

    init {
        val relativeFolders = listOf("src", "main", "webapp")

        tomcat.setBaseDir("target")
        tomcat.setPort(tomcatPort)

        baseDir = tomcat.server.catalinaHome.toPath()
        println("Tomcat-baseDir: $baseDir")
        var sourcePath = baseDir.parent.parent.resolve("server")
        webappsPath = baseDir.resolve("webapps")
        destinationPath = webappsPath

        for (folder in relativeFolders) {
            sourcePath = sourcePath.resolve(folder)
            destinationPath = destinationPath.resolve(folder)
        }

        FileUtils.copyDirectory(sourcePath.toFile(), destinationPath.toFile())

        val relativePath = webappsPath.relativize(destinationPath)
        context = tomcat.addContext(contextPath, relativePath.toString())
        tomcat.addServlet(contextPath, servletName, servlet)
        context.addServletMappingDecoded(servletPattern, servletName)
        tomcat.init()
        tomcat.start()
    }

    fun shutTomcatDown() {
        tomcat.stop()
        FileUtils.deleteDirectory(destinationPath.toFile())
        FileUtils.deleteDirectory(webappsPath.toFile())
    }
}
