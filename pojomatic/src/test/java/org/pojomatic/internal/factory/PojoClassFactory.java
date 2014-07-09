package org.pojomatic.internal.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.asm5.ClassWriter;
import org.kohsuke.asm5.FieldVisitor;
import org.kohsuke.asm5.Label;
import org.kohsuke.asm5.MethodVisitor;
import org.kohsuke.asm5.Type;
import org.pojomatic.annotations.Property;

import static org.kohsuke.asm5.Opcodes.*;

public class PojoClassFactory {

  private static final class DynamicClassLoader extends ClassLoader {
    private final static Map<String, byte[]> classes= new HashMap<>();
    private DynamicClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> loadClass(String name, byte[] classBytes) {
      classes.put(name.replace('.', '/') + ".class", classBytes.clone());
      return defineClass(name, classBytes, 0, classBytes.length);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
      if (classes.containsKey(name)) {
        return new ByteArrayInputStream(classes.get(name));
      }
      return super.getResourceAsStream(name);
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
