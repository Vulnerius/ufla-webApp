package org.hsmw.uflaWebApp.webStorage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Stream


@Service
class FileSystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private val rootLocation: Path

    init {
        rootLocation = Paths.get(properties.location)
    }

    override fun store(file: MultipartFile) {
        try {
            if (file.isEmpty) {
                throw Exception("Failed to store empty file.")
            }
            val destinationFile: Path = rootLocation.resolve(
                Paths.get(file.name)
            )
                .normalize().toAbsolutePath()
            if (!destinationFile.parent.equals(rootLocation.toAbsolutePath())) {
                // This is a security check
                throw Exception(
                    "Cannot store file outside current directory."
                )
            }
            file.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        } catch (e: IOException) {
            throw Exception("Failed to store file.", e)
        }
    }

    override fun loadAll(): Stream<Path> {
        return try {
            Files.walk(rootLocation, 1)
                .filter { path -> !path.equals(rootLocation) }
                .map(rootLocation::relativize)
        } catch (e: IOException) {
            throw Exception("Failed to read stored files")
        }
    }

    override fun load(fileName: String): Path {
        return rootLocation.resolve(fileName)
    }

    override fun loadAsResource(fileName: String): Resource {
        return try {
            val file: Path = load(fileName)
            val resource: Resource = UrlResource(file.toUri())
            if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw FileNotFoundException(
                    "Could not read file: $fileName"
                )
            }
        } catch (e: MalformedURLException) {
            throw FileNotFoundException("Could not read file: $fileName")
        }
    }

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile())
    }

    override fun init() {
        try {
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            throw Exception("Could not initialize storage", e)
        }
    }
}