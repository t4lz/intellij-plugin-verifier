package com.jetbrains.pluginverifier.verifiers.instruction;

import com.intellij.structure.resolvers.Resolver;
import com.jetbrains.pluginverifier.VerificationContext;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.org.objectweb.asm.tree.MethodNode;

/**
 * @author Dennis.Ushakov
 */
public interface InstructionVerifier extends Opcodes {
  void verify(ClassNode clazz, MethodNode method, AbstractInsnNode instr, Resolver resolver, VerificationContext ctx);
}
