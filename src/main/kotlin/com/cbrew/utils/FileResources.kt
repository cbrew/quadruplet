package com.cbrew.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors


class FileResourcesUtils {
    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    private fun getFileFromResourceAsStream(fileName: String): InputStream {

        // The class loader that loaded the class
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(fileName)

        // the stream holding the file content
        return inputStream ?: throw IllegalArgumentException("file not found! $fileName")
    }

    // Get all paths from a folder that inside the JAR file
    @Throws(URISyntaxException::class, IOException::class)
    private fun getPathsFromResourceJAR(folder: String): List<Path> {
        var result: List<Path>

        // get path of the current running JAR
        val jarPath = javaClass.protectionDomain
            .codeSource
            .location
            .toURI()
            .path
        println("JAR Path :$jarPath")

        // file walks JAR
        val uri = URI.create("jar:file:$jarPath")
        FileSystems.newFileSystem(uri, emptyMap<String, Any>()).use { fs ->
            result = Files.walk(fs.getPath(folder))
                .filter { path: Path? -> Files.isRegularFile(path) }
                .collect(Collectors.toList())
        }
        return result
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val app = FileResourcesUtils()

            // Sample 3 - read all files from a resources folder (JAR version)
            try {

                // get paths from src/main/resources/json
                val result = app.getPathsFromResourceJAR("grammars")
                for (path in result) {
                    println("Path : $path")
                    var filePathInJAR = path.toString()
                    // Windows will returns /json/file1.json, cut the first /
                    // the correct path should be json/file1.json
                    if (filePathInJAR.startsWith("/")) {
                        filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length)
                    }
                    println("filePathInJAR : $filePathInJAR")

                    // read a file from resource folder
                    val `is` = app.getFileFromResourceAsStream(filePathInJAR)
                    printInputStream(`is`)
                }
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // print input stream
        private fun printInputStream(`is`: InputStream) {
            try {
                InputStreamReader(`is`, StandardCharsets.UTF_8).use { streamReader ->
                    BufferedReader(streamReader).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            println(line)
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

