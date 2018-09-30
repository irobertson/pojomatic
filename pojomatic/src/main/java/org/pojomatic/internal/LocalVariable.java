package org.pojomatic.internal;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class LocalVariable {
  private final String name;
  private final String signature;
  private Label scopeStart;
  private Label scopeEnd;
  private final int position;
  private final Type type;

  public LocalVariable(String name, Class<?> type, String signature, int position) {
    super();
    this.name = name;
    this.type = Type.getType(Type.getDescriptor(type));
    this.signature = signature;
    this.position = position;
  }

  public LocalVariable(String name, String typeDescriptor, String signature, int position) {
    super();
    this.name = name;
    this.type = Type.getType(typeDescriptor);
    this.signature = signature;
    this.position = position;
  }

  public LocalVariable withScope(Label start, Label end) {
    if (this.scopeStart != null) {
      throw new IllegalStateException("scopeStart already set");
    }
    if (this.scopeEnd != null) {
      throw new IllegalStateException("scopeEnd already set");
    }
    this.scopeStart = start;
    this.scopeEnd = end;
    return this;
  }

  public void acceptLocalVariable(MethodVisitor mv) {
    if (scopeStart == null) {
      throw new IllegalStateException("scopeStart not set");
    }
    if (scopeEnd == null) {
      throw new IllegalStateException("scopeEnd not set");
    }
    mv.visitLocalVariable(name, type.getDescriptor(), signature, scopeStart, scopeEnd, position);
  }

  public void acceptStore(MethodVisitor mv) {
    mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), position);
  }

  public void acceptLoad(MethodVisitor mv) {
    mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), position);
  }

  public int getPosition() {
    return position;
  }
}
