package com.jetbrains.plugin.structure.fleet.mock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jetbrains.plugin.structure.base.utils.contentBuilder.buildZipFile
import com.jetbrains.plugin.structure.fleet.FleetPluginManager
import com.jetbrains.plugin.structure.fleet.bean.DependencyVersion
import com.jetbrains.plugin.structure.fleet.bean.PluginPart
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

class FleetPluginGenerator(private val path: String = "/tmp") {
  fun generateWithRandomFiles(
    id: String,
    name: String,
    icon: String? = null,
    vendor: String? = "JetBrains",
    version: String? = "1.0.0",
    description: String? = null,
    depends: Map<String, String> = mapOf(),
    generateFrontend: Boolean = false,
    generateWorkspace: Boolean = false,
  ): File {
    val filesProvider = FilesGenerator(generateFrontend, generateWorkspace)
    filesProvider.generateFiles()

    return generate(
      id, name, icon, vendor, version, description, depends,
      PluginPart(filesProvider.frontendModuleFiles, filesProvider.frontendClasspathFiles, listOf()),
      PluginPart(filesProvider.workspaceModuleFiles, filesProvider.workspaceClasspathFiles, listOf()),
    )
  }

  fun generate(
    id: String,
    name: String,
    icon: String? = null,
    vendor: String? = "JetBrains",
    version: String? = "1.0.0",
    description: String? = null,
    depends: Map<String, String> = mapOf(),
    frontend: PluginPart? = null,
    workspace: PluginPart? = null,
  ): File {
    val fileName = "$id-$version"
    val descriptor = FleetTestDescriptor(
      id = id,
      name = name,
      vendor = vendor,
      version = version,
      description = description,
      depends = depends.mapValues { DependencyVersion.CompatibleWith(it.value) },
      frontend = frontend?.copy(modules = substitute(frontend.modules), classpath = substitute(frontend.classpath)),
      workspace = workspace?.copy(modules = substitute(workspace.modules), classpath = substitute(workspace.classpath)),
    )
    return build(
      jacksonObjectMapper().writeValueAsString(descriptor),
      fileName,
      icon,
      listOfNotNull(frontend?.modules, frontend?.classpath, workspace?.modules, workspace?.classpath).flatten()
    ).toFile()
  }

  private fun substitute(modules: List<String>): List<String> {
    val digest = MessageDigest.getInstance("SHA-1")
    return modules.map {
      val file = Paths.get(it)
      return@map file.fileName.toString() + "#" + digest.sha(file.toFile())
    }
  }

  private fun build(
    text: String,
    fileName: String,
    icon: String?,
    files: List<String>
  ): Path {
    val existing = mutableMapOf<String, Path>()
    val path = Paths.get(path, "$fileName.zip")
    Files.createDirectories(path.parent)
    return buildZipFile(path) {
      files.forEach {
        val p = Paths.get(it)
        val name = p.fileName.toString()
        assert(existing[name] in listOf(null, p)) { "Duplicated filename: $name, existing path: ${existing[name]}, new path: $p" }
        file(name, Files.readAllBytes(p))
      }
      file(FleetPluginManager.DESCRIPTOR_NAME, text)
      if (icon != null) {
        file("pluginIcon.svg", icon)
      }
    }
  }

  private fun MessageDigest.sha(file: File): String {
    val buffer = ByteArray(1 * 1024 * 1024)
    reset()
    file.inputStream().buffered().use {
      while (true) {
        val read = it.read(buffer)
        if (read <= 0) break
        update(buffer, 0, read)
      }
    }

    val shaBytes = digest()
    return buildString {
      shaBytes.forEach { byte -> append(java.lang.Byte.toUnsignedInt(byte).toString(16)) }
    }
  }
}

private class FilesGenerator(private val generateFrontend: Boolean, private val generateWorkspace: Boolean) {
  val workspaceClasspathFiles = mutableListOf<String>()
  val workspaceModuleFiles = mutableListOf<String>()
  val frontendClasspathFiles = mutableListOf<String>()
  val frontendModuleFiles = mutableListOf<String>()

  fun generateFiles() {
    val filesNum = 5
    if (generateFrontend) {
      frontendModuleFiles.addAll(createFiles(filesNum))
      frontendClasspathFiles.addAll(createFiles(filesNum))
    }
    if (generateWorkspace) {
      workspaceModuleFiles.addAll(createFiles(filesNum))
      workspaceClasspathFiles.addAll(createFiles(filesNum))
    }
  }

  private fun createFiles(number: Int): List<String> {
    return (0 until number).map {
      val file = Files.createTempFile("file$it-", ".txt")
      Files.writeString(file, "File #$it")
      return@map file.toString()
    }.toList()
  }
}

fun main() {
  //FleetPluginGenerator("~/test").generate("test.plugin.first", "First Test", frontend = PluginPart(listOf("~/module.jar"), listOf("~/classpath.jar"), listOf()))
  FleetPluginGenerator("~/test").generateWithRandomFiles("test.plugin.second", "Second Test", generateFrontend = true)
}

