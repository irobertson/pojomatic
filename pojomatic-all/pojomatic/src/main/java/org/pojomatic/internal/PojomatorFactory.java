package org.pojomatic.internal;

import java.beans.PropertyDescriptor;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;

import static org.objectweb.asm.Opcodes.*;

/*
 * 1) Break up byte code generation into a separate class that takes a class visitor
 */

public class PojomatorFactory {
  private static final Object[] NO_STACK = new Object[] {};
  private static final String CLASS_PROPERTIES_DESCRIPTOR = classDesc(ClassProperties.class);
  private static final String BOOTSTRAP_METHOD_DESCRIPTOR =
    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class)
    .toMethodDescriptorString();
  private static final String EXTENDED_BOOTSTRAP_METHOD_DESCRIPTOR =
    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, Class.class)
    .toMethodDescriptorString();
  private static final String CONSTRUCTOR_DESCRIPTOR = "(" + CLASS_PROPERTIES_DESCRIPTOR + ")V";
  private static final String DO_HASH_CODE_DESCRIPTOR = MethodType.methodType(int.class, Object.class)
    .toMethodDescriptorString();
  private static final String DO_EQUALS_DESCRIPTOR = MethodType.methodType(boolean.class, Object.class, Object.class)
    .toMethodDescriptorString();

  private static final String POJO_CLASS_FIELD_NAME = "pojoClass";
  private static final String POJOMATOR_SIG = internalName(Pojomator.class);
  private static final String OBJECT_INTERNAL_NAME = internalName(Object.class);
  private static final String BASE_POJOMATOR_INTERNAL_NAME = internalName(BasePojomator.class);

  private static final String BOOTSTRAP_METHOD_NAME = "bootstrap";
  final static int HASH_CODE_SEED = 1;
  final static int HASH_CODE_MULTIPLIER = 31;

  private static final class DynamicClassLoader extends ClassLoader {
    private DynamicClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> loadClass(String name, byte[] classBytes) {
      return defineClass(name, classBytes, 0, classBytes.length);
    }
  }

  private static DynamicClassLoader CLASS_LOADER = new DynamicClassLoader(PojomatorFactory.class.getClassLoader());

  public static <T> Pojomator<T> makePojomator(Class<T> pojoClass) {
    try {
      return makePojomatorChecked(pojoClass);
    } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException
        | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> Pojomator<T> makePojomatorChecked(Class<T> pojoClass)
      throws IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException,
      InvocationTargetException, NoSuchMethodException {
    ClassProperties classProperties = ClassProperties.forClass(pojoClass);
    PojomatorFactory pojomatorFactory = new PojomatorFactory(pojoClass, classProperties);
    Class<?> pojomatorClass = CLASS_LOADER.loadClass(
      pojomatorFactory.pojomatorClassName, pojomatorFactory.makeClassBytes());
    Field field = pojomatorClass.getDeclaredField(POJO_CLASS_FIELD_NAME);
    field.setAccessible(true);
    field.set(null, pojoClass);
    @SuppressWarnings("unchecked")
    Pojomator<T> pojomator = (Pojomator<T>) pojomatorClass.getConstructor(ClassProperties.class).newInstance(classProperties);
    return pojomator;
  }

  private final String pojomatorClassName;
  private final String pojomatorInternalClassName;
  private final Class<?> pojoClass;
  private final ClassProperties classProperties;
  private final Handle bootstrapMethod;

  public PojomatorFactory(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojomatorClassName = pojoClass.getName() + "$$Pojomator";
    this.pojomatorInternalClassName = internalName(pojomatorClassName);
    this.pojoClass = pojoClass;
    this.classProperties = classProperties;
    this.bootstrapMethod = new Handle(
      H_INVOKESTATIC,
      pojomatorInternalClassName,
      "bootstrap",
      BOOTSTRAP_METHOD_DESCRIPTOR);
  }

  private byte[] makeClassBytes() {
    ClassWriter classWriter = new ClassWriter(0);
    acceptClassVisitor(new CheckClassAdapter(classWriter));
    return classWriter.toByteArray();
  }

  private void acceptClassVisitor(ClassVisitor classWriter) {
    classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER, internalName(pojomatorClassName), null,
        BASE_POJOMATOR_INTERNAL_NAME, new String[] { POJOMATOR_SIG });
    classWriter.visitSource("FIXME", null);
    makePojoClassField(classWriter);

    makeConstructor(classWriter);
    makeBootstrapMethod(classWriter);
    for (PropertyElement propertyElement: classProperties.getAllProperties()) {
      makeAccessor(classWriter, propertyElement);
    }
    makeDoEquals(classWriter);
    makeDoHashCode(classWriter);
    classWriter.visitEnd();
  }

  private void makePojoClassField(ClassVisitor classWriter) {
    FieldVisitor fieldVisitor =
      classWriter.visitField(ACC_STATIC, POJO_CLASS_FIELD_NAME, Type.getDescriptor(Class.class), null, null);
    fieldVisitor.visitEnd();
  }

  private void makeBootstrapMethod(ClassVisitor classWriter) {
    MethodVisitor mv = classWriter.visitMethod(
       ACC_STATIC, BOOTSTRAP_METHOD_NAME, BOOTSTRAP_METHOD_DESCRIPTOR,
       null,
       null);
    mv.visitCode();
    Label l0 = visitNewLabel(mv);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitVarInsn(ALOAD, 2);
    visitLineNumber(mv, 200);
    mv.visitFieldInsn(GETSTATIC, pojomatorInternalClassName, POJO_CLASS_FIELD_NAME, Type.getDescriptor(Class.class));
    visitLineNumber(mv, 201);
    mv.visitMethodInsn(
      INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME,
      "bootstrap",
      EXTENDED_BOOTSTRAP_METHOD_DESCRIPTOR);
    mv.visitInsn(ARETURN);
    Label l1 = visitNewLabel(mv);
    mv.visitLocalVariable("caller", "Ljava/lang/invoke/MethodHandles$Lookup;", null, l0, l1, 0);
    mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l1, 1);
    mv.visitLocalVariable("type", "Ljava/lang/invoke/MethodType;", null, l0, l1, 2);
    mv.visitMaxs(4, 3);
    mv.visitEnd();
  }

  private void makeAccessor(ClassVisitor classWriter, PropertyElement propertyElement) {
    int maxStackSize = 1;
    String accessorName = accessorName(propertyElement);
    MethodVisitor mv = classWriter.visitMethod(
      ACC_PRIVATE | ACC_STATIC, accessorName, accessorMethodType(propertyElement), null, null);
    mv.visitCode();
    Label l0 = visitNewLabel(mv);
    visitLineNumber(mv, 100);
    mv.visitVarInsn(ALOAD, 0);
    visitLineNumber(mv, 101);
    mv.visitInvokeDynamicInsn(accessorName, accessorMethodType(propertyElement), bootstrapMethod);
    visitLineNumber(mv, 102);
    Class<?> propertyType = propertyElement.getPropertyType();
    if (propertyType.isPrimitive()) {
      if (propertyType == float.class) {
        mv.visitInsn(FRETURN);
      }
      else if (propertyType == long.class) {
        maxStackSize++;
        mv.visitInsn(LRETURN);
      }
      else if (propertyType == double.class) {
        maxStackSize++;
        mv.visitInsn(DRETURN);
      }
      else {
        mv.visitInsn(IRETURN);
      }
    }
    else {
      mv.visitInsn(ARETURN);
    }
    Label l1 = visitNewLabel(mv);
    mv.visitLocalVariable("bean", Type.getDescriptor(Object.class), null, l0, l1, 1);
    mv.visitMaxs(maxStackSize, 2);
    mv.visitEnd();
  }

  private String accessorMethodType(PropertyElement propertyElement) {
    return MethodType.methodType(propertyElement.getPropertyType(), Object.class).toMethodDescriptorString();
  }

  private void makeConstructor(ClassVisitor cw) {
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", CONSTRUCTOR_DESCRIPTOR, null, null);
    mv.visitCode();
    Label l0 = visitNewLabel(mv);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKESPECIAL, BASE_POJOMATOR_INTERNAL_NAME, "<init>", CONSTRUCTOR_DESCRIPTOR);
    mv.visitInsn(RETURN);
    Label l1 = visitNewLabel(mv);
    mv.visitLocalVariable("this", classDesc(pojomatorClassName), null, l0, l1, 0);
    mv.visitLocalVariable("classProperties", CLASS_PROPERTIES_DESCRIPTOR, null, l0, l1, 0);
    mv.visitMaxs(2, 2);
    mv.visitEnd();
  }

  private void makeDoEquals(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 0;

    // FIXME - make class instance
    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME};

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doEquals", DO_EQUALS_DESCRIPTOR, null, null);
    Label returnFalse = new Label();
    Label compatibleTypes = new Label();
    mv.visitCode();
    Label start = visitNewLabel(mv);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    Label notSameInstance = new Label();
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    // same instance; return true
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    // if other is null, return false.
    mv.visitLabel(notSameInstance);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    mv.visitVarInsn(ALOAD, 2);
    mv.visitJumpInsn(IFNULL, returnFalse);
    // if both types are equal, they are compatible
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "getClass", "()Ljava/lang/Class;");
    mv.visitVarInsn(ALOAD, 1);
    mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "getClass", "()Ljava/lang/Class;");
    mv.visitJumpInsn(IF_ACMPEQ, compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    // types are not equals; check for compatibility
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "getClass", "()Ljava/lang/Class;");
    mv.visitMethodInsn(INVOKEVIRTUAL, BASE_POJOMATOR_INTERNAL_NAME, "isCompatibleForEquality", "(Ljava/lang/Class;)Z");
    mv.visitJumpInsn(IFEQ, returnFalse);
    mv.visitLabel(compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    // compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      Class<?> propertyType = propertyElement.getPropertyType();

      visitAccessor(mv, 1, propertyElement);
      visitAccessor(mv, 2, propertyElement);
      if (propertyType.isPrimitive()) {
        if (propertyType.equals(long.class) || propertyElement.equals(double.class)) {
          longOrDoubleStackAdjustment = 1; // why doesn't this need to be 2? There are two of them...
          mv.visitInsn(LCMP);
          mv.visitJumpInsn(IFNE, returnFalse);
        }
        else {
          mv.visitJumpInsn(IF_ICMPNE, returnFalse);
        }
      }
      else if(propertyType.isArray()) {
        Class<? extends Object> arrayPropertyType =
          propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class;
        mv.visitMethodInsn(
          INVOKESTATIC,
          Type.getInternalName(Arrays.class),
          "equals",
          MethodType.methodType(boolean.class, arrayPropertyType, arrayPropertyType).toMethodDescriptorString());
        mv.visitJumpInsn(IFNE, returnFalse);
      }
      else if (propertyType.equals(Object.class)) {

      }
      else {
        mv.visitJumpInsn(IF_ACMPNE, returnFalse);
      }
    }
    // everything checks out; return true.
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    mv.visitLabel(returnFalse);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);

    Label end = visitNewLabel(mv);
    mv.visitLocalVariable("this", classDesc(pojomatorInternalClassName), null, start, end, 0);
    mv.visitLocalVariable("p", Type.getDescriptor(pojoClass) , null, start, end, 1);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 3);
    mv.visitEnd();

/*
 *{
mv = cw.visitMethod(ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(50, l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitVarInsn(ALOAD, 1);
Label l1 = new Label();
mv.visitJumpInsn(IF_ACMPNE, l1);
Label l2 = new Label();
mv.visitLabel(l2);
mv.visitLineNumber(51, l2);
mv.visitInsn(ICONST_1);
mv.visitInsn(IRETURN);
mv.visitLabel(l1);
mv.visitLineNumber(52, l1);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 1);
Label l3 = new Label();
mv.visitJumpInsn(IFNONNULL, l3);
Label l4 = new Label();
mv.visitLabel(l4);
mv.visitLineNumber(53, l4);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l3);
mv.visitLineNumber(54, l3);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
Label l5 = new Label();
mv.visitJumpInsn(IF_ACMPEQ, l5);
Label l6 = new Label();
mv.visitLabel(l6);
mv.visitLineNumber(55, l6);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l5);
mv.visitLineNumber(56, l5);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 1);
mv.visitTypeInsn(CHECKCAST, "beans/Person");
mv.visitVarInsn(ASTORE, 2);
Label l7 = new Label();
mv.visitLabel(l7);
mv.visitLineNumber(57, l7);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "age", "I");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "age", "I");
Label l8 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l8);
Label l9 = new Label();
mv.visitLabel(l9);
mv.visitLineNumber(58, l9);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l8);
mv.visitLineNumber(59, l8);
mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"beans/Person"}, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "boolVal", "Z");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "boolVal", "Z");
Label l10 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l10);
Label l11 = new Label();
mv.visitLabel(l11);
mv.visitLineNumber(60, l11);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l10);
mv.visitLineNumber(61, l10);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "byteVal", "B");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "byteVal", "B");
Label l12 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l12);
Label l13 = new Label();
mv.visitLabel(l13);
mv.visitLineNumber(62, l13);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l12);
mv.visitLineNumber(63, l12);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "charVal", "C");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "charVal", "C");
Label l14 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l14);
Label l15 = new Label();
mv.visitLabel(l15);
mv.visitLineNumber(64, l15);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l14);
mv.visitLineNumber(65, l14);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "doubleVal", "D");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
Label l16 = new Label();
mv.visitLabel(l16);
mv.visitLineNumber(66, l16);
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "doubleVal", "D");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
Label l17 = new Label();
mv.visitLabel(l17);
mv.visitLineNumber(65, l17);
mv.visitInsn(LCMP);
Label l18 = new Label();
mv.visitJumpInsn(IFEQ, l18);
Label l19 = new Label();
mv.visitLabel(l19);
mv.visitLineNumber(67, l19);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l18);
mv.visitLineNumber(68, l18);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "floatVal", "F");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "floatVal", "F");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
Label l20 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l20);
Label l21 = new Label();
mv.visitLabel(l21);
mv.visitLineNumber(69, l21);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l20);
mv.visitLineNumber(70, l20);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "longVal", "J");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "longVal", "J");
mv.visitInsn(LCMP);
Label l22 = new Label();
mv.visitJumpInsn(IFEQ, l22);
Label l23 = new Label();
mv.visitLabel(l23);
mv.visitLineNumber(71, l23);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l22);
mv.visitLineNumber(72, l22);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
Label l24 = new Label();
mv.visitJumpInsn(IFNONNULL, l24);
Label l25 = new Label();
mv.visitLabel(l25);
mv.visitLineNumber(73, l25);
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
Label l26 = new Label();
mv.visitJumpInsn(IFNULL, l26);
Label l27 = new Label();
mv.visitLabel(l27);
mv.visitLineNumber(74, l27);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l24);
mv.visitLineNumber(75, l24);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
mv.visitJumpInsn(IFNE, l26);
Label l28 = new Label();
mv.visitLabel(l28);
mv.visitLineNumber(76, l28);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l26);
mv.visitLineNumber(77, l26);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
Label l29 = new Label();
mv.visitJumpInsn(IFNONNULL, l29);
Label l30 = new Label();
mv.visitLabel(l30);
mv.visitLineNumber(78, l30);
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
Label l31 = new Label();
mv.visitJumpInsn(IFNULL, l31);
Label l32 = new Label();
mv.visitLabel(l32);
mv.visitLineNumber(79, l32);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l29);
mv.visitLineNumber(80, l29);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
mv.visitJumpInsn(IFNE, l31);
Label l33 = new Label();
mv.visitLabel(l33);
mv.visitLineNumber(81, l33);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l31);
mv.visitLineNumber(82, l31);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "shortVal", "S");
mv.visitVarInsn(ALOAD, 2);
mv.visitFieldInsn(GETFIELD, "beans/Person", "shortVal", "S");
Label l34 = new Label();
mv.visitJumpInsn(IF_ICMPEQ, l34);
Label l35 = new Label();
mv.visitLabel(l35);
mv.visitLineNumber(83, l35);
mv.visitInsn(ICONST_0);
mv.visitInsn(IRETURN);
mv.visitLabel(l34);
mv.visitLineNumber(84, l34);
mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
mv.visitInsn(ICONST_1);
mv.visitInsn(IRETURN);
Label l36 = new Label();
mv.visitLabel(l36);
mv.visitLocalVariable("this", "Lbeans/Person;", null, l0, l36, 0);
mv.visitLocalVariable("obj", "Ljava/lang/Object;", null, l0, l36, 1);
mv.visitLocalVariable("other", "Lbeans/Person;", null, l7, l36, 2);
mv.visitMaxs(4, 3);
mv.visitEnd();
}

 */
  }

  private void makeDoHashCode(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 0;

    //FIXME - make class instance
    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, INTEGER, INTEGER};

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doHashCode", DO_HASH_CODE_DESCRIPTOR, null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    visitLineNumber(mv, 1);
    mv.visitInsn(ICONST_1);

    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      visitLineNumber(mv, 2);
      mv.visitIntInsn(BIPUSH, 31);
      visitLineNumber(mv, 3);
      mv.visitInsn(IMUL);
      visitLineNumber(mv, 4);

      visitAccessor(mv, 1, propertyElement);
      Class<?> propertyType = propertyElement.getPropertyType();
      if (propertyType.isPrimitive()) {
        // need to compute the hash code for this primitive value, based on its type
        switch (propertyType.getName()) {
          case "boolean":
            Label ifeq = new Label();
            mv.visitJumpInsn(IFEQ, ifeq);
            mv.visitIntInsn(SIPUSH, 1231);
            Label hashCodeDetermined = new Label();
            mv.visitJumpInsn(GOTO, hashCodeDetermined);
            mv.visitLabel(ifeq);
            mv.visitFrame(F_FULL, 3, localVars, 1, new Object[] {INTEGER});
            mv.visitIntInsn(SIPUSH, 1237);
            mv.visitLabel(hashCodeDetermined);
            mv.visitFrame(F_FULL, 3, localVars, 2, new Object[] {INTEGER, INTEGER});
            break;
          case "byte":
          case "char":
          case "int":
          case "short":
          case "float":
            break; // already an int
          case "double":
          case "long":
            longOrDoubleStackAdjustment = 3;
            mv.visitInsn(DUP2);
            mv.visitIntInsn(BIPUSH, 32);
            mv.visitInsn(LUSHR);
            mv.visitInsn(LXOR);
            mv.visitInsn(L2I);
            break;
          default:
            throw new IllegalStateException("unknown primitive type " + propertyType.getName());
        }
      }
      else {
        Label ifNonNull = new Label();
        Label hashCodeDetermined = new Label();

        mv.visitInsn(DUP); // if it's non-null, let's not have to get it a second time.
        mv.visitJumpInsn(IFNONNULL, ifNonNull);
        // it's null
        mv.visitInsn(POP); // won't need that duped copy after all
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, hashCodeDetermined);
        // it's not null
        mv.visitLabel(ifNonNull);
        mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, Type.getInternalName(propertyType)});

        if(propertyType.isArray()) {
          mv.visitMethodInsn(
            INVOKESTATIC,
            Type.getInternalName(Arrays.class),
            "hashCode",
            MethodType.methodType(
              int.class, propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class)
              .toMethodDescriptorString());
        }
        else if (propertyType == Object.class) {
          mv.visitInsn(DUP);
          mv.visitMethodInsn(
            INVOKEVIRTUAL,
            OBJECT_INTERNAL_NAME,
            "getClass",
            MethodType.methodType(Class.class).toMethodDescriptorString());
          mv.visitMethodInsn(
            INVOKEVIRTUAL,
            Type.getInternalName(Class.class),
            "isArray",
            MethodType.methodType(boolean.class).toMethodDescriptorString());
          Label isArray = new Label();
          mv.visitJumpInsn(IFNE, isArray); // if true
          mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");
          mv.visitJumpInsn(GOTO, hashCodeDetermined);

          mv.visitLabel(isArray);
          mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] { INTEGER, Type.getInternalName(propertyType) });
          mv.visitMethodInsn(
            INVOKESTATIC,
            BASE_POJOMATOR_INTERNAL_NAME,
            "arrayHashCode",
            MethodType.methodType(int.class, Object.class).toMethodDescriptorString());
        }
        else {
          mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");
        }

        mv.visitLabel(hashCodeDetermined);
        mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, INTEGER});
      }
      mv.visitInsn(IADD);
    }
    mv.visitInsn(IRETURN);
    Label end = visitNewLabel(mv);
    mv.visitLocalVariable("this", classDesc(pojomatorInternalClassName), null, start, end, 0);
    mv.visitLocalVariable("p", Type.getDescriptor(pojoClass) , null, start, end, 1);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 2);
    mv.visitEnd();
  }

  /**
   * Visit an accessor, converting floats or doubles to int bits or long bits respectively
   * @param mv
   * @param variableNumber
   * @param propertyElement
   */
  private void visitAccessor(MethodVisitor mv, int variableNumber, PropertyElement propertyElement) {
    mv.visitVarInsn(ALOAD, variableNumber);
    mv.visitMethodInsn(
      INVOKESTATIC, pojomatorInternalClassName, accessorName(propertyElement),
      accessorMethodType(propertyElement));
    if (propertyElement.getPropertyType().equals(float.class)) {
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
    }
    else if (propertyElement.getPropertyType().equals(double.class)) {
      //FIXME - would Double.compare be faster here?
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
    }
  }

  private static Label visitNewLabel(MethodVisitor mv) {
    Label label = new Label();
    mv.visitLabel(label);
    return label;
  }

  private static void visitLineNumber(MethodVisitor mv, int lineNumber) {
    mv.visitLineNumber(lineNumber, visitNewLabel(mv));
  }

  private static String internalName(Class<?> clazz) {
    return internalName(clazz.getName());
  }

  private static String internalName(String className) {
    return className.replace('.', '/');
  }

  private static String classDesc(Class<?> clazz) {
    return classDesc(clazz.getName());
  }

  private static String classDesc(String className) {
    return "L" + internalName(className) + ";";
  }

  private static String accessorName(PropertyElement propertyElement) {
    return propertyElement.getType() + "_" + propertyElement.getElementName();
  }
}
