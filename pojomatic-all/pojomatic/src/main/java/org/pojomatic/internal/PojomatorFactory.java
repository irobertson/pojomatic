package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;

import static org.objectweb.asm.Opcodes.*;

/*
 * 1) Break up byte code generation into a separate class that takes a class visitor
 */

public class PojomatorFactory {
  private static final String ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME = internalName(EnhancedPojoFormatterWrapper.class);
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
  private static final String DO_TO_STRING_DESCRIPTOR = MethodType.methodType(String.class, Object.class)
    .toMethodDescriptorString();
  private static final String ARRAY_HASHCODE_DESCRIPTOR =
    MethodType.methodType(int.class, Object.class).toMethodDescriptorString();
  private static final String OBJECT_OBJECT_TO_BOOL_DESCRIPTOR =
    MethodType.methodType(boolean.class, Object.class, Object.class).toMethodDescriptorString();
  private static final String POJO_CLASS_FIELD_NAME = "pojoClass";
  private static final String POJOMATOR_SIG = internalName(Pojomator.class);
  private static final String OBJECT_INTERNAL_NAME = internalName(Object.class);
  private static final String OBJECT_DESCRIPTOR = Type.getDescriptor(Object.class);
  private static final String ARRAYS_INTERNAL_NAME = internalName(Arrays.class);
  private static final String CLASS_INTERNAL_NAME = internalName(Class.class);
  private static final String CLASS_DESCRIPTOR = Type.getDescriptor(Class.class);
  private static final String BASE_POJOMATOR_INTERNAL_NAME = internalName(BasePojomator.class);
  private static final String STRING_BUILDER_INTERNAL_NAME = internalName(StringBuilder.class);
  private static final String ENHANCED_POJO_FORMATTER_INTERNAL_NAME = internalName(EnhancedPojoFormatter.class);
  private static final String ENHANCED_PROPERTY_FORMATTER_INTERNAL_NAME = internalName(EnhancedPropertyFormatter.class);
  private static final String ENHANCED_PROPERTY_FORMATTER_DESCRIPTOR =
    Type.getDescriptor(EnhancedPropertyFormatter.class);
  private static final String PROPERTY_ELEMENT_DESCRIPTOR = Type.getDescriptor(PropertyElement.class);

  @SuppressWarnings("deprecation")
  private static final String ENHANCED_POJO_FORMATTER_WRAPPER_CONSTRUCTOR_DESCRIPTOR =
    MethodType.methodType(void.class, PojoFormatter.class).toMethodDescriptorString();

  private static final String BOOTSTRAP_METHOD_NAME = "bootstrap";

  private static final AtomicLong counter = new AtomicLong();

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
    setStaticField(pojomatorClass, POJO_CLASS_FIELD_NAME, pojoClass);
    @SuppressWarnings("unchecked")
    Pojomator<T> pojomator =
      (Pojomator<T>) pojomatorClass.getConstructor(ClassProperties.class).newInstance(classProperties);
    for (PropertyElement propertyElement: classProperties.getToStringProperties()) {
      setStaticField(
        pojomatorClass,
        propertyFormatterName(propertyElement),
        createPropertyFormatter(propertyElement.getElement().getAnnotation(PropertyFormat.class)));

      setStaticField(pojomatorClass, propertyElementName(propertyElement), propertyElement);
    }
    return pojomator;
  }

  private static void setStaticField(Class<?> clazz, String fieldName, Object value)
      throws NoSuchFieldException, SecurityException, IllegalAccessException {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(null, value);
  }
  private static EnhancedPropertyFormatter createPropertyFormatter(PropertyFormat format)
    throws InstantiationException, IllegalAccessException {
    if (format == null) {
      return new DefaultEnhancedPropertyFormatter();
    }
    else {
      if (EnhancedPropertyFormatter.class.isAssignableFrom(format.value())) {
        return (EnhancedPropertyFormatter) format.value().newInstance();
      }
      else {
        @SuppressWarnings("deprecation")
        EnhancedPropertyFormatterWrapper wrapper = new EnhancedPropertyFormatterWrapper(format.value().newInstance());
        return wrapper;
      }
    }

  }

  private final String pojomatorClassName;
  private final String pojomatorInternalClassName;
  private final Class<?> pojoClass;
  private final String pojoDescriptor;
  private final ClassProperties classProperties;
  private final Handle bootstrapMethod;

  public PojomatorFactory(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojomatorClassName = PojomatorFactory.class.getName() + "$Pojomator$" + counter.incrementAndGet();
    this.pojomatorInternalClassName = internalName(pojomatorClassName);
    this.pojoClass = pojoClass;
    this.pojoDescriptor = Type.getDescriptor(pojoClass);
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
    classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER, pojomatorInternalClassName, null,
        BASE_POJOMATOR_INTERNAL_NAME, new String[] { POJOMATOR_SIG });
    classWriter.visitSource("Look for visitLineNumber", null);
    makeFields(classWriter);

    makeConstructor(classWriter);
    makeBootstrapMethod(classWriter);
    for (PropertyElement propertyElement: classProperties.getAllProperties()) {
      makeAccessor(classWriter, propertyElement);
    }
    makeDoEquals(classWriter);
    makeDoHashCode(classWriter);
    makeDoToString(classWriter);
    classWriter.visitEnd();
  }

  private void makeFields(ClassVisitor classVisitor) {
    visitField(classVisitor, ACC_STATIC, POJO_CLASS_FIELD_NAME, CLASS_DESCRIPTOR);
    for (PropertyElement propertyElement: classProperties.getToStringProperties()) {
      visitField(
        classVisitor, ACC_STATIC, propertyFormatterName(propertyElement), ENHANCED_PROPERTY_FORMATTER_DESCRIPTOR);
      visitField(
        classVisitor, ACC_STATIC, propertyElementName(propertyElement), PROPERTY_ELEMENT_DESCRIPTOR);
    }
  }

  private static void visitField(ClassVisitor classVisitor, int flags, String name, String classDescriptor) {
    FieldVisitor fieldVisitor = classVisitor.visitField(flags, name, classDescriptor, null, null);
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
    loadPojoClass(mv);
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
    String accessorName = propertyName(propertyElement);
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
    mv.visitLocalVariable("bean", OBJECT_DESCRIPTOR, null, l0, l1, 1);
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

    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME};

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doEquals", OBJECT_OBJECT_TO_BOOL_DESCRIPTOR, null, null);
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
    invokeGetClass(mv);
    mv.visitVarInsn(ALOAD, 1);
    invokeGetClass(mv);
    mv.visitJumpInsn(IF_ACMPEQ, compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    // types are not equals; check for compatibility
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 2);
    invokeGetClass(mv);
    mv.visitMethodInsn(INVOKEVIRTUAL, BASE_POJOMATOR_INTERNAL_NAME, "isCompatibleForEquality", "(Ljava/lang/Class;)Z");
    mv.visitJumpInsn(IFEQ, returnFalse);
    mv.visitLabel(compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    // compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      Class<?> propertyType = propertyElement.getPropertyType();

      visitAccessorAndCompact(mv, 1, propertyElement);
      visitAccessorAndCompact(mv, 2, propertyElement);
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
      else {
        if(propertyType.isArray()) {
          Class<? extends Object> arrayPropertyType =
            propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class;
          mv.visitMethodInsn(
            INVOKESTATIC,
            ARRAYS_INTERNAL_NAME,
            "equals",
            MethodType.methodType(boolean.class, arrayPropertyType, arrayPropertyType).toMethodDescriptorString());
        }
        else if (propertyType.equals(Object.class)) {
          mv.visitMethodInsn(
            INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME, "areObjectValuesEqual", OBJECT_OBJECT_TO_BOOL_DESCRIPTOR);
        }
        else {
          mv.visitMethodInsn(
            INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME, "areNonArrayValuesEqual", OBJECT_OBJECT_TO_BOOL_DESCRIPTOR);
        }
        mv.visitJumpInsn(IFEQ, returnFalse);
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
    mv.visitLocalVariable("p", pojoDescriptor, null, start, end, 1);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 3);
    mv.visitEnd();
  }

  private void makeDoHashCode(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 0;
    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME};

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

      visitAccessorAndCompact(mv, 1, propertyElement);
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
            ARRAYS_INTERNAL_NAME,
            "hashCode",
            MethodType.methodType(
              int.class, propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class)
              .toMethodDescriptorString());
        }
        else if (propertyType == Object.class) {
          mv.visitInsn(DUP);
          invokeGetClass(mv);
          mv.visitMethodInsn(INVOKEVIRTUAL, CLASS_INTERNAL_NAME, "isArray", "()Z");
          Label isArray = new Label();
          mv.visitJumpInsn(IFNE, isArray); // if true
          mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");
          mv.visitJumpInsn(GOTO, hashCodeDetermined);

          mv.visitLabel(isArray);
          mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] { INTEGER, Type.getInternalName(propertyType) });
          mv.visitMethodInsn(INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME, "arrayHashCode", ARRAY_HASHCODE_DESCRIPTOR);
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
    mv.visitLocalVariable("p", pojoDescriptor, null, start, end, 1);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 2);
    mv.visitEnd();
  }

  private void makeDoToString(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 1;
    /*
     * Local vars in this method:
     * 0: this
     * 1: the pojo instance to invoke toString on
     * 2: the StringBuilder used to build the result
     * 3: The EnhancedPojoFormatter used to control the building
     */

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doToString", DO_TO_STRING_DESCRIPTOR, null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);

    constructEnhancedPojoFormatter(mv);
    mv.visitVarInsn(ASTORE, 2);

    mv.visitTypeInsn(NEW, STRING_BUILDER_INTERNAL_NAME);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER_INTERNAL_NAME, "<init>", "()V");
    mv.visitVarInsn(ASTORE, 3);

    mv.visitVarInsn(ALOAD, 2);
    mv.visitVarInsn(ALOAD, 3);
    loadPojoClass(mv);

    visitLineNumber(mv, 200);

    mv.visitMethodInsn(INVOKEINTERFACE, ENHANCED_POJO_FORMATTER_INTERNAL_NAME, "appendToStringPrefix",
      MethodType.methodType(void.class, StringBuilder.class, Class.class).toMethodDescriptorString());

    for(PropertyElement propertyElement: classProperties.getToStringProperties()) {
      if (long.class == propertyElement.getPropertyType() || double.class == propertyElement.getPropertyType()) {
        longOrDoubleStackAdjustment = 1;
      }
      mv.visitVarInsn(ALOAD, 2);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitFieldInsn(
        GETSTATIC,
        pojomatorInternalClassName,
        propertyElementName(propertyElement),
        Type.getDescriptor(PropertyElement.class));
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        ENHANCED_POJO_FORMATTER_INTERNAL_NAME,
        "appendPropertyPrefix",
        MethodType.methodType(void.class, StringBuilder.class, PropertyElement.class).toMethodDescriptorString());

      mv.visitFieldInsn(
        GETSTATIC,
        pojomatorInternalClassName,
        propertyFormatterName(propertyElement),
        Type.getDescriptor(EnhancedPropertyFormatter.class));
      mv.visitVarInsn(ALOAD, 3);
      visitAccessor(mv, 1, propertyElement);
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        ENHANCED_PROPERTY_FORMATTER_INTERNAL_NAME,
        "appendFormatted",
        MethodType.methodType(
          void.class,
          StringBuilder.class,
          propertyElement.getPropertyType().isPrimitive() ? propertyElement.getPropertyType() : Object.class)
          .toMethodDescriptorString());

      mv.visitVarInsn(ALOAD, 2);
      mv.visitVarInsn(ALOAD, 3);
      mv.visitFieldInsn(
        GETSTATIC,
        pojomatorInternalClassName,
        propertyElementName(propertyElement),
        Type.getDescriptor(PropertyElement.class));
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        ENHANCED_POJO_FORMATTER_INTERNAL_NAME,
        "appendPropertySuffix",
        MethodType.methodType(void.class, StringBuilder.class, PropertyElement.class).toMethodDescriptorString());
    }

    mv.visitVarInsn(ALOAD, 2);
    mv.visitVarInsn(ALOAD, 3);
    loadPojoClass(mv);
    mv.visitMethodInsn(INVOKEINTERFACE, ENHANCED_POJO_FORMATTER_INTERNAL_NAME, "appendToStringSuffix",
      MethodType.methodType(void.class, StringBuilder.class, Class.class).toMethodDescriptorString());

    mv.visitVarInsn(ALOAD, 3);
    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_INTERNAL_NAME, "toString", "()Ljava/lang/String;");

    mv.visitInsn(ARETURN);
    Label end = visitNewLabel(mv);
    mv.visitLocalVariable("this", classDesc(pojomatorInternalClassName), null, start, end, 0);
    mv.visitLocalVariable("p", pojoDescriptor, null, start, end, 1);
    mv.visitLocalVariable("builder", Type.getDescriptor(StringBuilder.class), null, start, end, 1);
    mv.visitLocalVariable("pojoFormatter", Type.getDescriptor(EnhancedPojoFormatter.class), null, start, end, 1);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 4);
    mv.visitEnd();
  }

  private void loadPojoClass(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, pojomatorInternalClassName, POJO_CLASS_FIELD_NAME, CLASS_DESCRIPTOR);
  }

  /**
   * Construct the pojoFormatter to use. This method will contribute 2 or 4 to the max stack depth,
   * depending on whether the pojoFormatter implements {@link EnhancedPojoFormatter} or not.
   * @param mv
   */
  private void constructEnhancedPojoFormatter(MethodVisitor mv) {
    PojoFormat format = pojoClass.getAnnotation(PojoFormat.class);
    if (format == null) {
      mv.visitTypeInsn(NEW, internalName(DefaultEnhancedPojoFormatter.class));
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, internalName(DefaultEnhancedPojoFormatter.class), "<init>", "()V");
    }
    else {
      @SuppressWarnings("deprecation")
      Class<? extends PojoFormatter> pojoFormatterClass = format.value();
      boolean isEnhancedFormatter = EnhancedPojoFormatter.class.isAssignableFrom(pojoFormatterClass);
      if (! isEnhancedFormatter) {
        mv.visitTypeInsn(NEW, ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME);
        mv.visitInsn(DUP);
      }
      mv.visitTypeInsn(NEW, internalName(pojoFormatterClass));
      mv.visitInsn(DUP);
      mv.visitMethodInsn(INVOKESPECIAL, internalName(pojoFormatterClass), "<init>", "()V");
      if (! isEnhancedFormatter) {
        mv.visitMethodInsn(
          INVOKESPECIAL,
          ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME,
          "<init>",
          ENHANCED_POJO_FORMATTER_WRAPPER_CONSTRUCTOR_DESCRIPTOR);
      }
    }
  }

  /**
   * Visit an accessor, converting floats or doubles to int bits or long bits respectively
   * @param mv
   * @param variableNumber the index of the local variable holding a the pojo instance to access
   * @param propertyElement the property to access
   */
  private void visitAccessorAndCompact(MethodVisitor mv, int variableNumber, PropertyElement propertyElement) {
    visitAccessor(mv, variableNumber, propertyElement);
    if (propertyElement.getPropertyType().equals(float.class)) {
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
    }
    else if (propertyElement.getPropertyType().equals(double.class)) {
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
    }
  }

  /**
   * Visit an accessor
   * @param mv
   * @param variableNumber the index of the local variable holding a the pojo instance to access
   * @param propertyElement the property to access
   */
  private void visitAccessor(MethodVisitor mv, int variableNumber, PropertyElement propertyElement) {
    mv.visitVarInsn(ALOAD, variableNumber);
    mv.visitMethodInsn(
      INVOKESTATIC, pojomatorInternalClassName, propertyName(propertyElement),
      accessorMethodType(propertyElement));
  }

  private static void invokeGetClass(MethodVisitor mv) {
    mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "getClass", "()Ljava/lang/Class;");
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

  private static String propertyName(PropertyElement propertyElement) {
    return propertyElement.getType() + "_" + propertyElement.getElementName();
  }

  private static String propertyElementName(PropertyElement propertyElement) {
    return "element_" + propertyElement.getType() + "_" + propertyElement.getElementName();
  }

  private static String propertyFormatterName(PropertyElement propertyElement) {
    return "formatter_" + propertyElement.getType() + "_" + propertyElement.getElementName();
  }
}
