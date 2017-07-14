package com.jetbrains.pluginverifier.problems

import com.jetbrains.pluginverifier.descriptions.DescriptionsBundle
import com.jetbrains.pluginverifier.descriptions.FullDescription
import com.jetbrains.pluginverifier.descriptions.ShortDescription
import com.jetbrains.pluginverifier.location.*
import com.jetbrains.pluginverifier.reference.ClassReference
import com.jetbrains.pluginverifier.reference.FieldReference
import com.jetbrains.pluginverifier.reference.MethodReference
import org.jetbrains.annotations.PropertyKey

/**
 * @author Sergey Patrikeev
 */
sealed class Problem(@PropertyKey(resourceBundle = "long.descriptions") private val messageKey: String) {

  fun getShortDescription(): ShortDescription {
    val shortTemplate = DescriptionsBundle.getShortDescription(messageKey)
    return ShortDescription(shortTemplate, shortDescriptionParams().map { it.toString() })
  }

  fun getFullDescription(): FullDescription {
    val descriptionParams = fullDescriptionParams()
    val fullTemplate = DescriptionsBundle.getFullDescription(messageKey)
    val effect = DescriptionsBundle.getEffect(messageKey)
    return FullDescription(fullTemplate, effect, descriptionParams.map { it.toString() })
  }

  protected abstract fun fullDescriptionParams(): List<Any>

  protected abstract fun shortDescriptionParams(): List<Any>

  final override fun toString(): String = getFullDescription().toString()

  final override fun equals(other: Any?): Boolean = other is Problem && getFullDescription() == other.getFullDescription()

  final override fun hashCode(): Int = getFullDescription().hashCode()

}

data class MultipleDefaultImplementationsProblem(val caller: MethodLocation,
                                                 val methodReference: MethodReference,
                                                 val instruction: Instruction,
                                                 val implementation1: MethodLocation,
                                                 val implementation2: MethodLocation) : Problem("multiple.default.implementations") {

  override fun shortDescriptionParams(): List<Any> = listOf(methodReference)

  override fun fullDescriptionParams() = listOf(caller, instruction, methodReference, implementation1, implementation2)

}

data class IllegalClassAccessProblem(val unavailableClass: ClassLocation,
                                     val access: AccessType,
                                     val usage: Location) : Problem("illegal.class.access") {

  override fun shortDescriptionParams(): List<Any> = listOf(access, unavailableClass)

  override fun fullDescriptionParams(): List<Any> {
    val type = if (unavailableClass.accessFlags.contains(AccessFlags.Flag.INTERFACE)) "interface" else "class"
    return listOf(access.toString().capitalize(), type, unavailableClass, usage)
  }
}

data class AbstractClassInstantiationProblem(val abstractClass: ClassLocation,
                                             val creator: MethodLocation) : Problem("abstract.class.instantiation") {


  override fun shortDescriptionParams(): List<Any> = listOf(abstractClass)

  override fun fullDescriptionParams() = listOf(creator, abstractClass)

}

data class ClassNotFoundProblem(val unresolved: ClassReference,
                                val usage: Location) : Problem("class.not.found") {

  override fun shortDescriptionParams(): List<Any> = listOf(unresolved)

  override fun fullDescriptionParams(): List<Any> {
    val type: String = when (usage) {
      is ClassLocation -> "Class"
      is MethodLocation -> "Method"
      is FieldLocation -> "Field"
      else -> throw IllegalArgumentException()
    }
    return listOf(type, usage, unresolved)
  }
}

data class SuperInterfaceBecameClassProblem(val child: ClassLocation,
                                            val clazz: ClassLocation) : Problem("super.interface.became.class") {

  override fun shortDescriptionParams(): List<Any> = listOf(clazz)

  override fun fullDescriptionParams(): List<Any> {
    val type = if (child.accessFlags.contains(AccessFlags.Flag.INTERFACE)) "Interface" else "Class"
    return listOf(type, child, clazz)
  }

}

data class InheritFromFinalClassProblem(val child: ClassLocation,
                                        val finalClass: ClassLocation) : Problem("inherit.from.final.class") {

  override fun shortDescriptionParams(): List<Any> = listOf(finalClass)

  override fun fullDescriptionParams(): List<Any> {
    val type = if (child.accessFlags.contains(AccessFlags.Flag.INTERFACE)) "Interface" else "Class"
    return listOf(type, child, finalClass)
  }
}

data class SuperClassBecameInterfaceProblem(val child: ClassLocation,
                                            val interfaze: ClassLocation) : Problem("super.class.became.interface") {

  override fun shortDescriptionParams(): List<Any> = listOf(interfaze)

  override fun fullDescriptionParams() = listOf(child, interfaze)

}

data class InvokeClassMethodOnInterfaceProblem(val methodReference: MethodReference,
                                               val caller: MethodLocation,
                                               val instruction: Instruction) : Problem("invoke.class.method.on.interface") {

  override fun shortDescriptionParams(): List<Any> = listOf(methodReference.hostClass)

  override fun fullDescriptionParams() = listOf(caller, instruction, methodReference, methodReference.hostClass)

}

data class InvokeInterfaceMethodOnClassProblem(val methodReference: MethodReference,
                                               val caller: MethodLocation,
                                               val instruction: Instruction) : Problem("invoke.interface.method.on.class") {

  override fun shortDescriptionParams(): List<Any> = listOf(methodReference.hostClass)

  override fun fullDescriptionParams() = listOf(caller, instruction, methodReference, methodReference.hostClass)

}

data class InterfaceInstantiationProblem(val interfaze: ClassLocation,
                                         val creator: MethodLocation) : Problem("interface.instantiation") {

  override fun shortDescriptionParams(): List<Any> = listOf(interfaze)

  override fun fullDescriptionParams() = listOf(creator, interfaze)

}

data class ChangeFinalFieldProblem(val field: FieldLocation,
                                   val accessor: MethodLocation,
                                   val instruction: Instruction) : Problem("change.final.field") {

  override fun shortDescriptionParams(): List<Any> = listOf(field)

  override fun fullDescriptionParams() = listOf(accessor, instruction, field)

}

data class FieldNotFoundProblem(val field: FieldReference,
                                val accessor: MethodLocation,
                                val instruction: Instruction) : Problem("field.not.found") {

  override fun shortDescriptionParams(): List<Any> = listOf(field)

  override fun fullDescriptionParams() = listOf(accessor, instruction, field)
}

data class IllegalFieldAccessProblem(val field: FieldLocation,
                                     val accessor: MethodLocation,
                                     val instruction: Instruction,
                                     val fieldAccess: AccessType) : Problem("illegal.field.access") {

  override fun shortDescriptionParams(): List<Any> = listOf(fieldAccess, field)

  override fun fullDescriptionParams() = listOf(accessor, instruction, fieldAccess, field, accessor.hostClass)

}

data class IllegalMethodAccessProblem(val method: MethodLocation,
                                      val caller: MethodLocation,
                                      val instruction: Instruction,
                                      val methodAccess: AccessType) : Problem("illegal.method.access") {

  override fun shortDescriptionParams(): List<Any> = listOf(methodAccess, method)

  override fun fullDescriptionParams() = listOf(caller, instruction, methodAccess, method, caller.hostClass)
}

data class InvokeInterfaceOnPrivateMethodProblem(val resolvedMethod: MethodLocation,
                                                 val caller: MethodLocation) : Problem("invoke.interface.on.private.method") {

  override fun shortDescriptionParams(): List<Any> = listOf(resolvedMethod)

  override fun fullDescriptionParams() = listOf(caller, resolvedMethod)
}

data class MethodNotFoundProblem(val method: MethodReference,
                                 val caller: MethodLocation,
                                 val instruction: Instruction) : Problem("method.not.found") {

  override fun shortDescriptionParams(): List<Any> = listOf(method)

  override fun fullDescriptionParams() = listOf(caller, instruction, method)

}

data class MethodNotImplementedProblem(val method: MethodLocation,
                                       val incompleteClass: ClassLocation) : Problem("method.not.implemented") {

  override fun shortDescriptionParams(): List<Any> = listOf(method)

  override fun fullDescriptionParams() = listOf(incompleteClass, method.hostClass, method.methodNameAndParameters())
}

data class AbstractMethodInvocationProblem(val method: MethodLocation,
                                           val caller: MethodLocation,
                                           val instruction: Instruction) : Problem("abstract.method.invocation") {

  override fun shortDescriptionParams(): List<Any> = listOf(method)

  override fun fullDescriptionParams() = listOf(caller, instruction, method)

}

data class OverridingFinalMethodProblem(val method: MethodLocation,
                                        val invalidClass: ClassLocation) : Problem("overriding.final.method") {

  override fun shortDescriptionParams(): List<Any> = listOf(method)

  override fun fullDescriptionParams() = listOf(invalidClass, method)
}

data class NonStaticAccessOfStaticFieldProblem(val field: FieldLocation,
                                               val accessor: MethodLocation,
                                               val instruction: Instruction) : Problem("non.static.access.of.static.field") {

  override fun shortDescriptionParams(): List<Any> = listOf(instruction, field)

  override fun fullDescriptionParams() = listOf(accessor, instruction, field)

}

data class InvokeStaticOnNonStaticMethodProblem(val resolvedMethod: MethodLocation,
                                                val caller: MethodLocation) : Problem("invoke.static.on.non.static.method") {

  override fun shortDescriptionParams(): List<Any> = listOf(resolvedMethod)

  override fun fullDescriptionParams() = listOf(caller, resolvedMethod)
}

data class InvokeNonStaticInstructionOnStaticMethodProblem(val resolvedMethod: MethodLocation,
                                                           val caller: MethodLocation,
                                                           val instruction: Instruction) : Problem("invoke.non.static.instruction.on.static.method") {

  override fun shortDescriptionParams(): List<Any> = listOf(instruction, resolvedMethod)

  override fun fullDescriptionParams() = listOf(caller, instruction, resolvedMethod)
}

data class StaticAccessOfNonStaticFieldProblem(val field: FieldLocation,
                                               val accessor: MethodLocation,
                                               val instruction: Instruction) : Problem("static.access.of.non.static.field") {

  override fun shortDescriptionParams(): List<Any> = listOf(instruction, field)

  override fun fullDescriptionParams() = listOf(accessor, instruction, field)
}

data class InvalidClassFileProblem(val brokenClass: ClassReference,
                                   val usage: Location,
                                   val reason: String) : Problem("invalid.class.file") {
  override fun shortDescriptionParams(): List<Any> = listOf(brokenClass)

  override fun fullDescriptionParams(): List<Any> = listOf(brokenClass, usage, reason)

}