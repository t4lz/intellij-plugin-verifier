package com.jetbrains.plugin.structure.toolbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jetbrains.plugin.structure.base.plugin.PluginProblem
import com.jetbrains.plugin.structure.base.problems.*
import com.vdurmont.semver4j.Semver
import com.vdurmont.semver4j.SemverException

@JsonIgnoreProperties(ignoreUnknown = true)
data class ToolboxPluginDescriptor(
  @JsonProperty("id")
  val id: String? = null,
  @JsonProperty("version")
  val version: String? = null,
  @JsonProperty("compatibleVersionRange")
  val compatibleVersionRange: ToolboxVersionRange? = null,
  @JsonProperty("meta")
  val meta: ToolboxMeta? = null,
) {
  companion object {
    private val NON_ID_SYMBOL_REGEX = "^[A-Za-z\\d_.]+$".toRegex()

    fun parse(serializedDescriptor: String): ToolboxPluginDescriptor {
      return jacksonObjectMapper().readValue(serializedDescriptor, ToolboxPluginDescriptor::class.java)
    }
  }

  fun validate(): List<PluginProblem> {
    val problems = mutableListOf<PluginProblem>()
    when {
      id.isNullOrBlank() -> {
        problems.add(PropertyNotSpecified("id"))
      }

      !NON_ID_SYMBOL_REGEX.matches(id) -> {
        problems.add(InvalidPluginIDProblem(id))
      }
    }

    if (version.isNullOrBlank()) {
      problems.add(PropertyNotSpecified("version"))
    }

    if (meta?.description.isNullOrBlank()) {
      problems.add(PropertyNotSpecified("description"))
    }

    if (meta?.vendor.isNullOrBlank()) {
      problems.add(PropertyNotSpecified("vendor"))
    }

    when {
      compatibleVersionRange == null -> {
        problems.add(PropertyNotSpecified("compatibleVersionRange"))
      }

      compatibleVersionRange.from.isNullOrBlank() -> {
        problems.add(PropertyNotSpecified("compatibleVersionRange.from"))
      }

      compatibleVersionRange.to.isNullOrBlank() -> {
        problems.add(PropertyNotSpecified("compatibleVersionRange.to"))
      }

      else -> {
        val fromSemver = parseVersionOrNull(compatibleVersionRange.from)
        val toSemver = parseVersionOrNull(compatibleVersionRange.to)
        when {
          fromSemver == null -> {
            problems.add(ToolboxInvalidVersion("from", compatibleVersionRange.from))
          }

          toSemver == null -> {
            problems.add(ToolboxInvalidVersion("to", compatibleVersionRange.to))
          }

          fromSemver.isGreaterThan(toSemver) -> {
            problems.add(ToolboxInvalidVersionRange(compatibleVersionRange.from, compatibleVersionRange.to))
          }

          else -> {
            val fromVersionProblems = validateVersion("from", fromSemver)
            problems.addAll(fromVersionProblems)
            if (fromVersionProblems.isEmpty()) {
              problems.addAll(validateVersion("to", toSemver))
            }
          }
        }
      }
    }

    val readableName = meta?.name
    when {
      readableName.isNullOrBlank() -> {
        problems.add(PropertyNotSpecified("name"))
      }

      else -> {
        validatePropertyLength(ToolboxPluginManager.DESCRIPTOR_NAME, "name", readableName, MAX_NAME_LENGTH, problems)
      }
    }
    return problems
  }

  private fun parseVersionOrNull(version: String): Semver? {
    return try {
      Semver(version)
    } catch (e: SemverException) {
      null
    }
  }

  private fun validateVersion(versionName: String, semver: Semver): Collection<PluginProblem> {
    val problems = mutableListOf<PluginProblem>()
    when {
      semver.major > VERSION_MAJOR_PART_MAX_VALUE -> problems.add(ToolboxErroneousVersion(versionName, "major", semver.originalValue, VERSION_MAJOR_PART_MAX_VALUE))
      semver.minor > VERSION_MINOR_PART_MAX_VALUE -> problems.add(ToolboxErroneousVersion(versionName, "minor", semver.originalValue, VERSION_MINOR_PART_MAX_VALUE))
      semver.patch > VERSION_PATCH_PART_MAX_VALUE -> problems.add(ToolboxErroneousVersion(versionName, "patch", semver.originalValue, VERSION_PATCH_PART_MAX_VALUE))
    }
    return problems
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ToolboxMeta(
  @JsonProperty("readableName")
  val name: String? = null,
  @JsonProperty("description")
  val description: String? = null,
  @JsonProperty("vendor")
  val vendor: String? = null
)

data class ToolboxVersionRange(
  @JsonProperty("from")
  val from: String? = null,
  @JsonProperty("to")
  val to: String? = null
) {

  companion object {
    fun fromStringToLong(version: String?): Long {
      return Semver(version).run {
        major.toLong().shl(VERSION_PATCH_LENGTH + VERSION_MINOR_LENGTH) + minor.toLong().shl(VERSION_PATCH_LENGTH) + patch
      }
    }
  }

  @Suppress("unused")
  fun asLongRange(): LongRange {
    val fromLong = fromStringToLong(from)
    val toLong = fromStringToLong(to)
    return fromLong..toLong
  }
}

private const val VERSION_PATCH_LENGTH = 14
private const val VERSION_MINOR_LENGTH = 13

const val VERSION_MAJOR_PART_MAX_VALUE = 7449 // 1110100011001
const val VERSION_MINOR_PART_MAX_VALUE = 1.shl(VERSION_MINOR_LENGTH) - 1 // 8191
const val VERSION_PATCH_PART_MAX_VALUE = 1.shl(VERSION_PATCH_LENGTH) - 1 // 16383

class ToolboxInvalidVersion(private val versionName: String, private val version: String) : InvalidDescriptorProblem(
  ToolboxPluginManager.DESCRIPTOR_NAME
) {
  override val detailedMessage: String
    get() = "$versionName version should be formatted as semver [$version]"

  override val level
    get() = Level.ERROR
}

class ToolboxInvalidVersionRange(private val from: String, private val to: String) : InvalidDescriptorProblem(
  ToolboxPluginManager.DESCRIPTOR_NAME
) {
  override val detailedMessage: String
    get() = "from build $from is greater than to build $to"

  override val level
    get() = Level.ERROR
}

class ToolboxErroneousVersion(
  private val versionName: String,
  private val partName: String,
  private val version: String,
  private val limit: Int,
) : InvalidDescriptorProblem(ToolboxPluginManager.DESCRIPTOR_NAME) {
  override val detailedMessage: String
    get() = "$partName part of $versionName version is too big [$version]. Max value is $limit"

  override val level: Level
    get() = Level.ERROR
}
