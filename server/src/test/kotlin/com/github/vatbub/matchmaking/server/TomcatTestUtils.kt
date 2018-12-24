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
)  {
    @Suppress("MemberVisibilityCanBePrivate")
    val tomcat = Tomcat()
    private var destinationPath: Path
    private val webappsPath: Path
    private val baseDir: Path
    @Suppress("MemberVisibilityCanBePrivate")
    var context: Context

    init {
        val relativeFolders = listOf("src", "main", "webapp")

        tomcat.setBaseDir(".")
        tomcat.setPort(tomcatPort)

        baseDir = tomcat.server.catalinaHome.toPath()
        var sourcePath = baseDir.parent.resolve("server")
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
        FileUtils.deleteDirectory(baseDir.toFile())
    }
}