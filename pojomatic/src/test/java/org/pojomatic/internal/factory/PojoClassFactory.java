package org.pojomatic.internal.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kohsuke.asm6.AnnotationVisitor;
import org.kohsuke.asm6.ClassWriter;
import org.kohsuke.asm6.FieldVisitor;
import org.kohsuke.asm6.Label;
import org.kohsuke.asm6.MethodVisitor;
import org.kohsuke.asm6.Type;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.Property;

import static org.kohsuke.asm6.Opcodes.*;

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

    void definePackage(String packageName) {
      definePackage(packageName, null, null, null, null, null, null, null);
    }
  }

  private final DynamicClassLoader classLoader = new DynamicClassLoader(
    PojoClassFactory.class.getClassLoader());

  private final Set<String> definedPackageNames = new HashSet<>();

  public Class<?> generateClass(PojoDescriptor pojoDescriptor) {
    if (definedPackageNames.add(pojoDescriptor.packageName)) {
      classLoader.definePackage(pojoDescriptor.packageName);
    }
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

    if (pojoDescriptor.autoDetectPolicy != null) {
      AnnotationVisitor annotationVisitor = cw.visitAnnotation(Type.getDescriptor(AutoProperty.class), true);
      annotationVisitor.visitEnum(
        "autoDetect", Type.getDescriptor(AutoDetectPolicy.class), pojoDescriptor.autoDetectPolicy.name());
    }
    generateConstructor(cw, pojoDescriptor);
    for (PropertyDescriptor property : pojoDescriptor.properties) {
      generateProperty(cw, property);
    }
    return cw.toByteArray();
  }

  private void generateConstructor(ClassWriter cw, PojoDescriptor pojoDescriptor) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, pojoDescriptor.parentInternalName(), "<init>", "()V", false);
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

  private void generateProperty(ClassWriter cw, PropertyDescriptor property) {
     if (property.isMethod) {
       generatePropertyMethod(cw, property);
     }
     else {
       generatePropertyField(cw, property);
     }
  }

  private void generatePropertyField(ClassWriter cw, PropertyDescriptor property) {
    FieldVisitor fv = cw.visitField(
      property.getFlags(),
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

  public Object getX() {
    return null;
  }

  private void generatePropertyMethod(ClassWriter cw, PropertyDescriptor property) {
    if (property.type.isPrimitive()) {
      throw new UnsupportedOperationException("Cannot generate methods returning primitives");
    }
    MethodVisitor mv = cw.visitMethod(
      property.getFlags(),
      property.name,
      Type.getMethodDescriptor(Type.getType(property.type)),
      null,
      null);
    mv.visitAnnotation(Type.getDescriptor(Property.class), true).visitEnd();
    for (Class<? extends Annotation> annotationClass: property.annotations) {
      mv.visitAnnotation(Type.getDescriptor(annotationClass), true).visitEnd();
    }
    mv.visitCode();
    Label l0 = new Label();
    mv.visitLabel(l0);
    mv.visitInsn(ACONST_NULL);
    mv.visitInsn(ARETURN);
    Label l1 = new Label();
    mv.visitLabel(l1);
    mv.visitLocalVariable("this", "Lorg/pojomatic/internal/factory/PojoClassFactory;", null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
}
