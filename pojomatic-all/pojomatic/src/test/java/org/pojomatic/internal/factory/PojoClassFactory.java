package org.pojomatic.internal.factory;

import java.lang.annotation.Annotation;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pojomatic.annotations.Property;

import static org.objectweb.asm.Opcodes.*;

public class PojoClassFactory {

  private static final class DynamicClassLoader extends ClassLoader {
    private DynamicClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> loadClass(String name, byte[] classBytes) {
      return defineClass(name, classBytes, 0, classBytes.length);
    }
  }

  private DynamicClassLoader classLoader = new DynamicClassLoader(
    PojoClassFactory.class.getClassLoader());

  public Class<?> generateClass(PojoDescriptor pojoDescriptor) {
    return classLoader.loadClass(pojoDescriptor.qualifiedName(), generateClassBytes(pojoDescriptor));
  }

  private byte[] generateClassBytes(PojoDescriptor pojoDescriptor) {
    ClassWriter cw = new ClassWriter(0);

    cw.visit(
      V1_7,
      pojoDescriptor.access.getCode() | ACC_SUPER,
      pojoDescriptor.internalName(),
      null,
      pojoDescriptor.parentInternalName(),
      null);

    generateConstructor(cw, pojoDescriptor);
    for (PropertyDescriptor property : pojoDescriptor.properties) {
      generatePropertyField(cw, property);
    }
    return cw.toByteArray();
  }

  private void generateConstructor(ClassWriter cw, PojoDescriptor pojoDescriptor) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, pojoDescriptor.parentInternalName(), "<init>", "()V");
    mv.visitInsn(RETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable(
      "this",
      "L" + pojoDescriptor.internalName() + ";",
      null,
      l0,
      l1,
      0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }

  private void generatePropertyField(ClassWriter cw, PropertyDescriptor property) {
    FieldVisitor fv = cw.visitField(
      property.access.getCode(),
      property.name,
      Type.getDescriptor(property.type),
      null,
      null);
    fv.visitAnnotation(Type.getDescriptor(Property.class), true).visitEnd();
    for (Class<? extends Annotation> annotationClass: property.annotations) {
      fv.visitAnnotation(Type.getDescriptor(annotationClass), true).visitEnd();
    }
    fv.visitEnd();
  }
}
