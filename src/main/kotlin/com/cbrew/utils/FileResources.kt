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
    // This demo code derived from https://mkyong.com/java/java-read-a-file-from-resources-folder/
    // contrary to claim, does not work for me from IDEA, but does work from built jar.


    private fun getFileFromResourceAsStream(fileName: String): InputStream {
        // Validate resource path to prevent directory traversal
        if (fileName.contains("..") || fileName.startsWith("/")) {
            throw IllegalArgumentException("Invalid resource path: $fileName")
        }

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

                // get paths from src/main/resources/grammars
                val result = app.getPathsFromResourceJAR("grammars")
                for (path in result) {
                    println("Path : $path")
                    var filePathInJAR = path.toString()
                    // Windows will return /json/file1.json, cut the first /
                    // the correct path should be json/file1.json
                    if (filePathInJAR.startsWith("/")) {
                        filePathInJAR = filePathInJAR.substring(1, filePathInJAR.length)
                    }
                    println("filePathInJAR : $filePathInJAR")

                    // read a file from resource folder
                    val inStream = app.getFileFromResourceAsStream(filePathInJAR)
                    printInputStream(inStream)
                }
            } catch (e: URISyntaxException) {
                println("Error processing URI: ${e.message}")
                throw e
            } catch (e: IOException) {
                println("I/O error: ${e.message}")
                throw e
            }
        }

        // print input stream
        private fun printInputStream(inStream: InputStream) {
            try {
                InputStreamReader(inStream, StandardCharsets.UTF_8).use { streamReader ->
                    BufferedReader(streamReader).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            println(line)
                        }
                    }
                }
            } catch (e: IOException) {
                println("Error reading stream: ${e.message}")
                throw e
            }
        }
    }
}

