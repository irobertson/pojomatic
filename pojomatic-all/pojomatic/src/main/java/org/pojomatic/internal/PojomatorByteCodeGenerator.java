package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;

import static org.objectweb.asm.Opcodes.*;

class PojomatorByteCodeGenerator {
  @Deprecated
  private static final String ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME =
    internalName(org.pojomatic.internal.EnhancedPojoFormatterWrapper.class);
  private static final Object[] NO_STACK = new Object[] {};
  private static final String OBJECT_INTERNAL_NAME = internalName(Object.class);
  private static final String BASE_POJOMATOR_INTERNAL_NAME = internalName(BasePojomator.class);
  @Deprecated
  private static final String ENHANCED_POJO_FORMATTER_WRAPPER_CONSTRUCTOR_DESCRIPTOR =
    methodDesc(void.class, org.pojomatic.formatter.PojoFormatter.class);

  static final String POJO_CLASS_FIELD_NAME = "pojoClass";
  private static final String BOOTSTRAP_METHOD_NAME = "bootstrap";

  private static final AtomicLong counter = new AtomicLong();

  final String pojomatorClassName;
  private final String pojomatorInternalClassName;
  private final Class<?> pojoClass;
  private final String pojoDescriptor;
  private final ClassProperties classProperties;
  private final Handle bootstrapMethod;

  PojomatorByteCodeGenerator(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojomatorClassName = getClass().getName() + "$Pojomator$" + counter.incrementAndGet();
    this.pojomatorInternalClassName = internalName(pojomatorClassName);
    this.pojoClass = pojoClass;
    this.pojoDescriptor = classDesc(pojoClass);
    this.classProperties = classProperties;
    this.bootstrapMethod = new Handle(
      H_INVOKESTATIC,
      pojomatorInternalClassName,
      BOOTSTRAP_METHOD_NAME,
      methodDesc(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class));
  }

  byte[] makeClassBytes() {
    ClassWriter classWriter = new ClassWriter(0);
    acceptClassVisitor(new CheckClassAdapter(classWriter));
    return classWriter.toByteArray();
  }

  private void acceptClassVisitor(ClassVisitor classWriter) {
    classWriter.visit(V1_7, ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC, pojomatorInternalClassName, null,
        BASE_POJOMATOR_INTERNAL_NAME, new String[] { internalName(Pojomator.class) });

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
    makeDoDiff(classWriter);

    classWriter.visitEnd();
  }

  private void makeFields(ClassVisitor classVisitor) {
    visitField(classVisitor, ACC_STATIC, POJO_CLASS_FIELD_NAME, classDesc(Class.class));
    for (PropertyElement property: classProperties.getToStringProperties()) {
      visitField(
        classVisitor, ACC_STATIC, propertyFormatterName(property), classDesc(EnhancedPropertyFormatter.class));
    }
    for (PropertyElement property: classProperties.getAllProperties()) {
      visitField(
        classVisitor, ACC_STATIC, propertyElementName(property), classDesc(PropertyElement.class));
    }
  }

  private static void visitField(ClassVisitor classVisitor, int flags, String name, String classDescriptor) {
    classVisitor.visitField(flags, name, classDescriptor, null, null).visitEnd();
  }

  /**
   * Generate a bootstrap method that allows us to access properties without reflection, even though they may not be
   * public. While the bulk of the work will be done by {@link BasePojomator}, we need to implement a shim which can
   * provide our classname to
   * {@link BasePojomator#bootstrap(java.lang.invoke.MethodHandles.Lookup, String, MethodType, Class)},
   * so that it can in turn access the {@link PropertyElement} field on the class we are generating.
   * @param classWriter
   */
  private void makeBootstrapMethod(ClassVisitor classWriter) {
    LocalVariable caller = new LocalVariable("caller", MethodHandles.Lookup.class, null, 0);
    LocalVariable name = new LocalVariable("name", String.class, null, 1);
    LocalVariable type = new LocalVariable("type", MethodType.class, null, 2);
    MethodVisitor mv = classWriter.visitMethod(
       ACC_STATIC,
       BOOTSTRAP_METHOD_NAME,
       methodDesc(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class),
       null,
       null);
    mv.visitCode();
    Label start = visitNewLabel(mv);

    caller.acceptLoad(mv);
    name.acceptLoad(mv);
    type.acceptLoad(mv);
    visitLineNumber(mv, 200);
    mv.visitLdcInsn(Type.getObjectType(pojomatorInternalClassName));
    visitLineNumber(mv, 201);
    mv.visitMethodInsn(
      INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME,
      "bootstrap",
      methodDesc(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, Class.class));
    mv.visitInsn(ARETURN);
    Label end = visitNewLabel(mv);
    caller.withScope(start, end).acceptLocalVariable(mv);
    name.withScope(start, end).acceptLocalVariable(mv);
    type.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(4, 3);
    mv.visitEnd();
  }

  /**
   * Generate an accessor method for a property. The generated method uses InvokeDynamic, calling the method generated
   * by {@link #makeBootstrapMethod(ClassVisitor)}
   * @param classWriter
   * @param propertyElement the property to generate the accessor for
   */
  private void makeAccessor(ClassVisitor classWriter, PropertyElement propertyElement) {
    LocalVariable pojo = new LocalVariable("pojo", Object.class, null, 0);
    int maxStackSize = 1;
    String accessorName = propertyAccessorName(propertyElement);
    MethodVisitor mv = classWriter.visitMethod(
      ACC_PRIVATE | ACC_STATIC, accessorName, accessorMethodType(propertyElement), null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    pojo.acceptLoad(mv);
    visitLineNumber(mv, 101);
    mv.visitInvokeDynamicInsn(accessorName, accessorMethodType(propertyElement), bootstrapMethod);
    visitLineNumber(mv, 102);

    // return using the appropriate return byte code, based on type
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

    Label end = visitNewLabel(mv);
    pojo.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(maxStackSize, 2);
    mv.visitEnd();
  }

  private void makeConstructor(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", classDesc(pojomatorInternalClassName), null, 0);
    LocalVariable varPojoClass = new LocalVariable("pojoClass", Class.class, null, 1);
    LocalVariable varClassProperties = new LocalVariable("classProperties", ClassProperties.class, null, 2);
    MethodVisitor mv = cw.visitMethod(
      ACC_PUBLIC, "<init>", methodDesc(void.class, Class.class, ClassProperties.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    varThis.acceptLoad(mv);
    varPojoClass.acceptLoad(mv);
    varClassProperties.acceptLoad(mv);
    mv.visitMethodInsn(
      INVOKESPECIAL, BASE_POJOMATOR_INTERNAL_NAME, "<init>", methodDesc(void.class, Class.class, ClassProperties.class));
    mv.visitInsn(RETURN);
    Label end = visitNewLabel(mv);
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojoClass.withScope(start, end).acceptLocalVariable(mv);
    varClassProperties.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(3, 3);
    mv.visitEnd();
  }

  /**
   * Generate the {@link Pojomator#doEquals(Object, Object)} method.
   * @param cw
   */
  private void makeDoEquals(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", classDesc(pojomatorInternalClassName), null, 0);
    LocalVariable varPojo1 = new LocalVariable("pojo1", pojoClass, pojoDescriptor, 1);
    LocalVariable varPojo2 = new LocalVariable("pojo2", pojoClass, pojoDescriptor, 2);

    int longOrDoubleStackAdjustment = 0;

    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME};

    MethodVisitor mv = cw.visitMethod(
      ACC_PUBLIC, "doEquals", methodDesc(boolean.class, Object.class, Object.class), null, null);

    // where to jump if we should return false
    Label returnFalse = new Label();
    // where to jump if we determine that pojo1 and pojo2 have types which are compatible for equality
    Label compatibleTypes = new Label();

    mv.visitCode();
    Label start = visitNewLabel(mv);
    varPojo1.acceptLoad(mv);
    checkNotNull(mv);
    varPojo2.acceptLoad(mv);
    visitLineNumber(mv, 1);
    Label notSameInstance = new Label();
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);

    // same instance; return true
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    mv.visitLabel(notSameInstance);

    // if other is null, return false.
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    visitLineNumber(mv, 2);
    varPojo2.acceptLoad(mv);
    mv.visitJumpInsn(IFNULL, returnFalse);

    // common case: if both types are the same, they are compatible for equality
    varThis.acceptLoad(mv);
    invokeGetClass(mv);
    varPojo1.acceptLoad(mv);
    invokeGetClass(mv);
    mv.visitJumpInsn(IF_ACMPEQ, compatibleTypes);

    // types are not the same; check for compatibility
    varThis.acceptLoad(mv);
    varPojo2.acceptLoad(mv);
    invokeGetClass(mv);
    mv.visitMethodInsn(
      INVOKEVIRTUAL, BASE_POJOMATOR_INTERNAL_NAME, "isCompatibleForEquality", methodDesc(boolean.class, Class.class));
    mv.visitJumpInsn(IFEQ, returnFalse);

    // types are compatible, so start comparing properties
    mv.visitLabel(compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    // Compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {

      visitAccessorAndConvert(mv, varPojo1, propertyElement);
      visitAccessorAndConvert(mv, varPojo2, propertyElement);
      if(compareProperties(mv, returnFalse, propertyElement)) {
        longOrDoubleStackAdjustment = 2;
      }
    }
    // If we have gotten this far, all properties are equal, so return true.
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    mv.visitLabel(returnFalse);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);

    Label end = visitNewLabel(mv);
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo1.withScope(start, end).acceptLocalVariable(mv);
    varPojo2.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(2 + longOrDoubleStackAdjustment, 3);
    mv.visitEnd();
  }

  /**
   * Compare a property from each pojo. It is assumed when this method is called that both property values have been
   * loaded onto the stack. In the event the property value is a float or double, it is further assumed that it has
   * been converted to an int or long.
   * @param mv
   * @param notEqualLabel where to jump if the property values are not equal
   * @param propertyElement the property being compared
   * @return {@code true} if the value of the property will take two positions on the stack (i.e. is a long or double)
   */
  private boolean compareProperties(MethodVisitor mv, Label notEqualLabel, PropertyElement propertyElement) {
    boolean propertyRequiresTwoStackFrames = false;
    Class<?> propertyType = propertyElement.getPropertyType();
    if (propertyType.isPrimitive()) {
      if (isWide(propertyElement)) {
        propertyRequiresTwoStackFrames = true;
        mv.visitInsn(LCMP);
        mv.visitJumpInsn(IFNE, notEqualLabel);
      }
      else {
        mv.visitJumpInsn(IF_ICMPNE, notEqualLabel);
      }
    }
    else {
      if(propertyType.isArray()) {
        // Comopare array values element by element
        Class<? extends Object> arrayPropertyType =
          propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class;
        mv.visitMethodInsn(
          INVOKESTATIC,
          internalName(Arrays.class),
          "equals",
          methodDesc(boolean.class, arrayPropertyType, arrayPropertyType));
      }
      else {
        mv.visitMethodInsn(
          INVOKESTATIC,
          BASE_POJOMATOR_INTERNAL_NAME,
          propertyType.equals(Object.class) ? "areObjectValuesEqual" : "areNonArrayValuesEqual",
          methodDesc(boolean.class, Object.class, Object.class));
      }
      mv.visitJumpInsn(IFEQ, notEqualLabel);
    }
    return propertyRequiresTwoStackFrames;
  }

  /**
   * Generate the {@link Pojomator#doHashCode(Object)} method.
   * @param cw
   */
  private void makeDoHashCode(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", classDesc(pojomatorInternalClassName), null, 0);
    LocalVariable varPojo = new LocalVariable("pojo", pojoClass, pojoDescriptor, 1);

    int longOrDoubleStackAdjustment = 0;
    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME};

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doHashCode", methodDesc(int.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    visitLineNumber(mv, 1);
    varPojo.acceptLoad(mv);
    checkNotNullPop(mv);

    //algorithm:
    // hashCode(prop_n) + 31 * (hashCode(prop_n-1) + 31 * ( ... (hashCode(prop_1) + 31 * 1) ... ))

    mv.visitInsn(ICONST_1); // this will just be multiplied by 31; let the optimizer take care of it

    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      // multiply what we have so far by 31.
      visitLineNumber(mv, 2);
      mv.visitIntInsn(BIPUSH, 31);
      visitLineNumber(mv, 3);
      mv.visitInsn(IMUL);
      visitLineNumber(mv, 4);

      visitAccessorAndConvert(mv, varPojo, propertyElement); // grab the property value, converting a float or double
      Class<?> propertyType = propertyElement.getPropertyType();
      if (propertyType.isPrimitive()) {
        // need to compute the hash code for this primitive value, based on its type
        switch (propertyType.getName()) {
          case "boolean":
            Label ifeq = new Label();
            mv.visitJumpInsn(IFEQ, ifeq);
            mv.visitIntInsn(SIPUSH, Boolean.TRUE.hashCode());
            Label hashCodeDetermined = new Label();
            mv.visitJumpInsn(GOTO, hashCodeDetermined);
            mv.visitLabel(ifeq);
            mv.visitFrame(F_FULL, 2, localVars, 1, new Object[] {INTEGER});
            mv.visitIntInsn(SIPUSH, Boolean.FALSE.hashCode());
            mv.visitLabel(hashCodeDetermined);
            mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, INTEGER});
            break;
          case "byte":
          case "char":
          case "int":
          case "short":
          case "float":
            break; // already an int (from the JVM's point of view)
          case "double":
          case "long":
            longOrDoubleStackAdjustment = 3; // one extra for the field, two extra for the dup to do an xor

            // compute bits ^ (bits >> 32)

            // we'll need a second copy to do the xor:
            mv.visitInsn(DUP2);
            // bitshift 32 right:
            mv.visitIntInsn(BIPUSH, 32);
            mv.visitInsn(LUSHR);
            // xor with the original
            mv.visitInsn(LXOR);
            // chop of the high 32 bits
            mv.visitInsn(L2I);
            break;
          default:
            throw new IllegalStateException("unknown primitive type " + propertyType.getName());
        }
      }
      else {
        Label ifNonNull = new Label();
        Label hashCodeDetermined = new Label();

        mv.visitInsn(DUP); // if it is non-null, let's not have to get it a second time.
        mv.visitJumpInsn(IFNONNULL, ifNonNull);
        // it's null
        mv.visitInsn(POP); // won't need that duped copy after all
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, hashCodeDetermined);

        // it's not null
        mv.visitLabel(ifNonNull);
        mv.visitFrame(
          F_FULL, 2, localVars, 2, new Object[] {INTEGER, Type.getInternalName(effectiveType(propertyType))});

        if(propertyType.isArray()) {
          //FIXME - for object arrays, should we do Arrays.deepHashCode? Perhaps an annotation to control this?
          mv.visitMethodInsn(
            INVOKESTATIC,
            internalName(Arrays.class),
            "hashCode",
            methodDesc(
              int.class, propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class)
              );
        }
        else if (propertyType == Object.class) {
          // it *could* be an array; if so, we want to do an array hashCode. (or do we?)

          mv.visitInsn(DUP); // we'll still want the property value handy after calling getClass().isArray()
          invokeGetClass(mv);
          mv.visitMethodInsn(INVOKEVIRTUAL, internalName(Class.class), "isArray", "()Z");
          Label isArray = new Label();
          mv.visitJumpInsn(IFNE, isArray); // if true

          // regular old hashCode
          mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");
          mv.visitJumpInsn(GOTO, hashCodeDetermined);

          mv.visitLabel(isArray);
          mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] { INTEGER, Type.getInternalName(propertyType) });
          mv.visitMethodInsn(INVOKESTATIC, BASE_POJOMATOR_INTERNAL_NAME, "arrayHashCode", methodDesc(int.class, Object.class));
        }
        else {
          mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");
        }

        mv.visitLabel(hashCodeDetermined);
        mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, INTEGER});
      }
      // add result to what we have so far
      mv.visitInsn(IADD);
    }
    mv.visitInsn(IRETURN);
    Label end = visitNewLabel(mv);
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 2);
    mv.visitEnd();
  }

  /**
   * Generate {@link Pojomator#doToString(Object)}
   * @param cw
   */
  private void makeDoToString(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 1;
    LocalVariable varThis = new LocalVariable("this", classDesc(pojomatorInternalClassName), null, 0);
    LocalVariable varPojo = new LocalVariable("pojo", pojoClass, null, 1);
    LocalVariable varPojoFormatter=
      new LocalVariable("pojoFormattor", classDesc(EnhancedPojoFormatter.class), null, 2);
    LocalVariable varBuilder= new LocalVariable("builder", classDesc(String.class), null, 3);

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doToString", methodDesc(String.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    varPojo.acceptLoad(mv);
    checkNotNullPop(mv);

    constructEnhancedPojoFormatter(mv);
    varPojoFormatter.acceptStore(mv);

    mv.visitTypeInsn(NEW, internalName(StringBuilder.class));
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, internalName(StringBuilder.class), "<init>", "()V");
    varBuilder.acceptStore(mv);

    varPojoFormatter.acceptLoad(mv);
    varBuilder.acceptLoad(mv);
    loadPojoClass(mv);

    visitLineNumber(mv, 200);

    mv.visitMethodInsn(INVOKEINTERFACE, internalName(EnhancedPojoFormatter.class), "appendToStringPrefix",
      methodDesc(void.class, StringBuilder.class, Class.class));

    for(PropertyElement propertyElement: classProperties.getToStringProperties()) {
      if (isWide(propertyElement)) {
        longOrDoubleStackAdjustment = 1; // having any double-wide values on our stack increases max stack depth by one
      }

      // append the property prefix
      varPojoFormatter.acceptLoad(mv);
      varBuilder.acceptLoad(mv);
      loadPropertyElementField(mv, propertyElement);
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        internalName(EnhancedPojoFormatter.class),
        "appendPropertyPrefix",
        methodDesc(void.class, StringBuilder.class, PropertyElement.class));

      // get the propertyFormatter for this property
      mv.visitFieldInsn(
        GETSTATIC,
        pojomatorInternalClassName,
        propertyFormatterName(propertyElement),
        classDesc(EnhancedPropertyFormatter.class));
      visitLineNumber(mv, 201);

      // The propertyFormatter will format the property value and append the results to our StringBuilder
      varBuilder.acceptLoad(mv);
      visitLineNumber(mv, 202);
      visitAccessor(mv, varPojo, propertyElement);
      visitLineNumber(mv, 203);
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        internalName(EnhancedPropertyFormatter.class),
        "appendFormatted",
        methodDesc(
          void.class,
          StringBuilder.class,
          propertyElement.getPropertyType().isPrimitive() ? propertyElement.getPropertyType() : Object.class)
          );
      visitLineNumber(mv, 204);

      // have any property suffix appended to the StringBuilder
      varPojoFormatter.acceptLoad(mv);
      varBuilder.acceptLoad(mv);
      loadPropertyElementField(mv, propertyElement);
      mv.visitMethodInsn(
        INVOKEINTERFACE,
        internalName(EnhancedPojoFormatter.class),
        "appendPropertySuffix",
        methodDesc(void.class, StringBuilder.class, PropertyElement.class));
    }

    // Have any toString suffix appended
    varPojoFormatter.acceptLoad(mv);
    varBuilder.acceptLoad(mv);
    loadPojoClass(mv);
    mv.visitMethodInsn(INVOKEINTERFACE, internalName(EnhancedPojoFormatter.class), "appendToStringSuffix",
      methodDesc(void.class, StringBuilder.class, Class.class));

    // invoke toString and return the result
    varBuilder.acceptLoad(mv);
    mv.visitMethodInsn(INVOKEVIRTUAL, internalName(StringBuilder.class), "toString", "()Ljava/lang/String;");
    mv.visitInsn(ARETURN);

    Label end = visitNewLabel(mv);
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo.withScope(start, end).acceptLocalVariable(mv);
    varPojoFormatter.withScope(start, end).acceptLocalVariable(mv);
    varBuilder.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 4);
    mv.visitEnd();
  }

  private void loadPropertyElementField(MethodVisitor mv, PropertyElement propertyElement) {
    mv.visitFieldInsn(
      GETSTATIC,
      pojomatorInternalClassName,
      propertyElementName(propertyElement),
      classDesc(PropertyElement.class));
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
      Class<? extends org.pojomatic.formatter.PojoFormatter> pojoFormatterClass = format.value();
      // if it isn't an enhanced formatter, we'll need to wrap it a EnhancedPojoFormatter. If we do this, we'll first
      // invoke new on the wrapper, then construct the underlying formatter, then call the constructor on the wrapper.
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
   * Generate {@link Pojomator#doDiff(Object, Object)
   * @param cw
   */
  private void makeDoDiff(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", classDesc(pojomatorInternalClassName), null, 0);
    LocalVariable varPojo1 = new LocalVariable("instance", pojoClass, pojoDescriptor, 1);
    LocalVariable varPojo2 = new LocalVariable("other", pojoClass, pojoDescriptor, 2);
    LocalVariable varDifferencesList = new LocalVariable(
      "differences", List.class, "Ljava/util/List<Lorg/pojomatic/diff/Difference;>;", 3);

    int longOrDoubleStackAdjustment = 0;
    Object[] localVarTypes = new Object[] {
      pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME, internalName(List.class), null, null };

    MethodVisitor mv = cw.visitMethod(
      ACC_PUBLIC, "doDiff", methodDesc(Differences.class, Object.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel(mv);
    varPojo1.acceptLoad(mv);
    checkNotNull(mv, "instance is null");
    varPojo2.acceptLoad(mv);
    checkNotNull(mv, "other is null");

    // If instance and other are the same object, then return NoDifferences.getInstance();
    Label notSameInstance = new Label();
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);
    mv.visitMethodInsn(INVOKESTATIC, internalName(NoDifferences.class), "getInstance", methodDesc(NoDifferences.class));
    mv.visitInsn(ARETURN);

    // not the same instance, some work to do
    mv.visitLabel(notSameInstance);
    mv.visitFrame(F_FULL, 3, localVarTypes, 0, NO_STACK);
    checkClass(mv, varThis, varPojo1, "instance");
    checkClass(mv, varThis, varPojo2, "other");

    Label makeDiferences = notSameInstance;
    mv.visitTypeInsn(NEW, "java/util/ArrayList");
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
    varDifferencesList.acceptStore(mv);

    List<LocalVariable> propertyVariables = new ArrayList<>(); // these will occur in a block scope
    // compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      int width = isWide(propertyElement) ? 2 : 1;
      if (width > 1) {
        longOrDoubleStackAdjustment = 2; // one extra slot each for the value from instance and from other
      }
      Class<?> propertyType = propertyElement.getPropertyType();
      LocalVariable varProp1 = new LocalVariable(
        "property_" + propertyElement.getName() + "_1", propertyType, null, 4);
      //If the type is long or double, we need to store the next var at 6, not 5.
      LocalVariable varProp2 = new LocalVariable(
        "property_" + propertyElement.getName() + "_2", propertyType, null, 4 + width);
      propertyVariables.add(varProp1);
      propertyVariables.add(varProp2);

      Label blockStart = visitNewLabel(mv);

      visitAccessor(mv, varPojo1, propertyElement);
      varProp1.acceptStore(mv);
      visitAccessor(mv, varPojo2, propertyElement);
      varProp2.acceptStore(mv);

      visitAccessorAndConvert(mv, varPojo1, propertyElement);
      visitAccessorAndConvert(mv, varPojo2, propertyElement);

      Label propertiesNotEqual = new Label();
      Label next = new Label();
      compareProperties(mv, propertiesNotEqual, propertyElement);
      mv.visitJumpInsn(GOTO, next); // there were no differences.

      mv.visitLabel(propertiesNotEqual);

      localVarTypes[5] = localVarTypes[4] = propertyType.isPrimitive()
        ? Primitives.getOpcode(propertyType)
        : internalName(effectiveType(propertyType));
      mv.visitFrame(F_FULL, 6, localVarTypes, 0, NO_STACK);

      // Create a ValueDifference instance, initialized with the property name and the two values, and add it to our list
      varDifferencesList.acceptLoad(mv); // we'll need this to add to the list
      mv.visitTypeInsn(NEW, "org/pojomatic/diff/ValueDifference");
      mv.visitInsn(DUP);
      mv.visitLdcInsn(propertyElement.getName());
      varProp1.acceptLoad(mv);
      convertToObject(mv, propertyType);
      varProp2.acceptLoad(mv);
      convertToObject(mv, propertyType);
      mv.visitMethodInsn(
        INVOKESPECIAL,
        internalName(ValueDifference.class),
        "<init>",
        methodDesc(void.class, String.class, Object.class, Object.class));

      // add the ValueDifference instance to our list
      mv.visitMethodInsn(INVOKEINTERFACE, internalName(List.class), "add", methodDesc(boolean.class, Object.class));
      mv.visitInsn(POP); // ignore the return value of List#add
      mv.visitLabel(next);
      mv.visitFrame(F_FULL, 4, localVarTypes, 0, NO_STACK);

      varProp1.withScope(blockStart, next);
      varProp2.withScope(blockStart, next);
    }

    // if our list is empty, return the NoDifferences instance
    varDifferencesList.acceptLoad(mv);
    mv.visitMethodInsn(INVOKEINTERFACE, internalName(List.class), "isEmpty", "()Z");
    Label hasDifferences = new Label();
    mv.visitJumpInsn(IFEQ, hasDifferences);
    mv.visitMethodInsn(INVOKESTATIC, internalName(NoDifferences.class), "getInstance", methodDesc(NoDifferences.class));
    mv.visitInsn(ARETURN);

    // our list is not empty, so wrap it in a PropertyDiferences instance
    mv.visitLabel(hasDifferences);
    mv.visitFrame(F_FULL, 4, localVarTypes, 0, NO_STACK);

    mv.visitTypeInsn(NEW, internalName(PropertyDifferences.class));
    mv.visitInsn(DUP);
    varDifferencesList.acceptLoad(mv);
    mv.visitMethodInsn(
      INVOKESPECIAL, internalName(PropertyDifferences.class), "<init>", methodDesc(void.class, List.class));
    mv.visitInsn(ARETURN);

    Label end = visitNewLabel(mv);

    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo1.withScope(start, end).acceptLocalVariable(mv);
    varPojo2.withScope(start, end).acceptLocalVariable(mv);
    varDifferencesList.withScope(makeDiferences, end).acceptLocalVariable(mv);
    for (LocalVariable var: propertyVariables) {
      var.acceptLocalVariable(mv);
    }
    mv.visitMaxs(6 + longOrDoubleStackAdjustment, 6 + longOrDoubleStackAdjustment);
    mv.visitEnd();
  }

  /**
   * Invoke {@link BasePojomator#checkClass(Object, String)} on the specified variable
   * @param mv the current methodVisitor
   * @param varNumber the variable number to check
   * @param message the message to include in the {@link IllegalArgumentException} if the variable fails the
   * class test
   */
  private void checkClass(MethodVisitor mv, LocalVariable varThis, LocalVariable var, String message) {
    varThis.acceptLoad(mv);
    var.acceptLoad(mv);
    mv.visitLdcInsn(message);
    mv.visitMethodInsn(
      INVOKEVIRTUAL,
      BASE_POJOMATOR_INTERNAL_NAME,
      "checkClass",
      methodDesc(void.class, Object.class, String.class));
  }

  /**
   * If the parameter on the stack (of type propertyType) is primitive, convert it to the appropriate wrapper object.
   * Otherwise, leave it alone
   * @param mv
   * @param propertyType TODO
   */
  private void convertToObject(MethodVisitor mv, Class<?> propertyType) {
    if (propertyType.isPrimitive()) {
      Class<?> wrapperClass = Primitives.getWrapperClass(propertyType);
      mv.visitMethodInsn(INVOKESTATIC, internalName(wrapperClass), "valueOf", methodDesc(wrapperClass, propertyType));
    }
  }

  /**
   * Load a reference to the pojo class. We cannot refer to this directly, since the class may not be visible to us,
   * so instead, we store a reference in a static field which is populated by {@link PojomatorFactory}
   * @param mv
   */
  private void loadPojoClass(MethodVisitor mv) {
    mv.visitFieldInsn(GETSTATIC, pojomatorInternalClassName, POJO_CLASS_FIELD_NAME, classDesc(Class.class));
  }

  /**
   * Visit an accessor, converting floats or doubles to int bits or long bits respectively.
   * @param mv
   * @param variableNumber the index of the local variable holding a the pojo instance to access
   * @param propertyElement the property to access
   */
  private void visitAccessorAndConvert(MethodVisitor mv, LocalVariable var, PropertyElement propertyElement) {
    visitAccessor(mv, var, propertyElement);
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
   * @param var the index of the local variable holding a the pojo instance to access
   * @param propertyElement the property to access
   */
  private void visitAccessor(MethodVisitor mv, LocalVariable var, PropertyElement propertyElement) {
    var.acceptLoad(mv);
    mv.visitMethodInsn(
      INVOKESTATIC, pojomatorInternalClassName, propertyAccessorName(propertyElement),
      accessorMethodType(propertyElement));
  }

  private String accessorMethodType(PropertyElement propertyElement) {
    return methodDesc(effectiveType(propertyElement.getPropertyType()), Object.class);
  }

  private Class<?> effectiveType(Class<?> propertyClass) {
    if (propertyClass.isArray()) {
      return propertyClass.getComponentType().isPrimitive() ? propertyClass : Object[].class;
    }
    else {
      return propertyClass.isPrimitive() ? propertyClass : Object.class;
    }
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

  private static boolean isWide(PropertyElement propertyElement) {
    Class<?> type = propertyElement.getPropertyType();
    return type == long.class || type == double.class;
  }

  /**
   * Pop the top element off of the stack and invoke {@link BasePojomator#checkNotNull(Object)} on it.
   * @param mv the current methodVisitor
   */
  private void checkNotNullPop(MethodVisitor mv) {
    mv.visitMethodInsn(
      INVOKESTATIC,
      BASE_POJOMATOR_INTERNAL_NAME,
      "checkNotNullPop",
      methodDesc(void.class, Object.class));
  }

  /**
   * Invoke {@link BasePojomator#checkNotNull(Object)} on the top element of the stack, leaving that element there.
   * @param mv the current methodVisitor
   */
  private void checkNotNull(MethodVisitor mv) {
    mv.visitMethodInsn(
      INVOKESTATIC,
      BASE_POJOMATOR_INTERNAL_NAME,
      "checkNotNull",
      methodDesc(Object.class, Object.class));
  }

  /**
   * Invoke {@link BasePojomator#checkNotNull(Object, String)} on the top element of the stack, leaving that element there.
   * @param mv the current methodVisitor
   * @param message the message to include in the {@link NullPointerException} if the top element is null
   */
  private void checkNotNull(MethodVisitor mv, String message) {
    mv.visitLdcInsn(message);
    mv.visitMethodInsn(
      INVOKESTATIC,
      BASE_POJOMATOR_INTERNAL_NAME,
      "checkNotNull",
      methodDesc(Object.class, Object.class, String.class));
  }

  private static String internalName(Class<?> clazz) {
    return internalName(clazz.getName());
  }

  private static String internalName(String className) {
    return className.replace('.', '/');
  }

  private static String classDesc(Class<?> clazz) {
    return Type.getDescriptor(clazz);
  }

  private static String classDesc(String className) {
    return "L" + internalName(className) + ";";
  }

  private static String methodDesc(Class<?> returnType) {
    return MethodType.methodType(returnType).toMethodDescriptorString();
  }

  private static String methodDesc(Class<?> returnType, Class<?> parameterType0) {
    return MethodType.methodType(returnType, parameterType0).toMethodDescriptorString();
  }

  private static String methodDesc(Class<?> returnType, Class<?> parameterType0, Class<?>... parameterTypes) {
    return MethodType.methodType(returnType, parameterType0, parameterTypes).toMethodDescriptorString();
  }

  private static String propertyAccessorName(PropertyElement property) {
    return "get_" + qualifiedPropertyName(property);
  }

  static String propertyElementName(PropertyElement property) {
    return "element_" + qualifiedPropertyName(property);
  }

  static String propertyFormatterName(PropertyElement property) {
    return "formatter_" + qualifiedPropertyName(property);
  }

  private static String qualifiedPropertyName(PropertyElement property) {
    return property.getType()
      + "_" + property.getDeclaringClass().getName().replace('.', '$')
      + "_" + property.getElementName();
  }
}
