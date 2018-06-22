package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.kohsuke.asm6.ClassWriter;
import org.kohsuke.asm6.util.CheckClassAdapter;
import org.kohsuke.asm6.ClassVisitor;
import org.kohsuke.asm6.Handle;
import org.kohsuke.asm6.Label;
import org.kohsuke.asm6.MethodVisitor;
import org.kohsuke.asm6.Type;
import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.SkipArrayCheck;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;

import static org.kohsuke.asm6.Opcodes.*;

class PojomatorByteCodeGenerator {
  @Deprecated
  private static final String ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME =
    internalName(org.pojomatic.internal.EnhancedPojoFormatterWrapper.class);
  private static final Object[] NO_STACK = new Object[] {};
  private static final String OBJECT_INTERNAL_NAME = internalName(Object.class);
  private static final String BASE_POJOMATOR_INTERNAL_NAME = internalName(BasePojomator.class);
  static final String POJO_CLASS_FIELD_NAME = "pojoClass";
  private static final String BOOTSTRAP_METHOD_NAME = "bootstrap";

  private static final AtomicLong counter = new AtomicLong();

  final String pojomatorClassName;
  private final String pojomatorInternalClassName;
  private final String pojomatorInternalClassDesc;
  private final Class<?> pojoClass;
  private final String pojoDescriptor;
  private final ClassProperties classProperties;
  private final Handle bootstrapMethod;
  private final Map<PropertyElement, Integer> propertyNumbers = new HashMap<>();

  private MethodVisitor mv; // the active method visitor

  /**
   * Class for tracking adjustments to be made to the max stack and/or localvariable size
   */
  private static class StackAdjustments {
    /**
     * At least one "wide" property (long or double) has been encountered
     */
    boolean wideProperty;

    int adjustments(int widePropertyWeight, int callAreObjectValuesEqualWeight) {
      return (wideProperty ? widePropertyWeight : 0);
    }
  }

  PojomatorByteCodeGenerator(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojomatorClassName = PojomatorStub.class.getName() + "$" + counter.incrementAndGet();
    this.pojomatorInternalClassName = internalName(pojomatorClassName);
    this.pojomatorInternalClassDesc = "L" + pojomatorInternalClassName + ";";
    this.pojoClass = pojoClass;
    this.pojoDescriptor = classDesc(pojoClass);
    this.classProperties = classProperties;
    this.bootstrapMethod = new Handle(
      H_INVOKESTATIC,
      BASE_POJOMATOR_INTERNAL_NAME,
      BOOTSTRAP_METHOD_NAME,
      methodDesc(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, Class.class));
    int propertyNumber = 1;
    for (PropertyElement property: classProperties.getAllProperties()) {
      propertyNumbers.put(property, propertyNumber++);
    }
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
    //visitField(classVisitor, ACC_STATIC, POJO_CLASS_FIELD_NAME, classDesc(Class.class));
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
   * Generate an accessor method for a property. The generated method uses InvokeDynamic, calling the method generated
   * by {@link #makeBootstrapMethod(ClassVisitor)}
   * @param classWriter
   * @param propertyElement the property to generate the accessor for
   */
  private void makeAccessor(ClassVisitor classWriter, PropertyElement propertyElement) {
    LocalVariable pojo = new LocalVariable("pojo", Object.class, null, 0);
    int maxStackSize = 1;
    String accessorName = propertyAccessorName(propertyElement);
    mv = classWriter.visitMethod(
      ACC_PRIVATE | ACC_STATIC, accessorName, accessorMethodDescription(propertyElement), null, null);
    mv.visitCode();
    Label start = visitNewLabel();
    pojo.acceptLoad(mv);
    visitLineNumber(4, propertyElement);
    mv.visitInvokeDynamicInsn(
      accessorName, accessorMethodDescription(propertyElement), bootstrapMethod, Type.getType(pojomatorInternalClassDesc));
    visitLineNumber(5, propertyElement);

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

    Label end = visitNewLabel();
    pojo.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(maxStackSize, 2);
    mv.visitEnd();
  }

  private void makeConstructor(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", pojomatorInternalClassDesc, null, 0);
    LocalVariable varPojoClass = new LocalVariable(POJO_CLASS_FIELD_NAME, Class.class, null, 1);
    LocalVariable varClassProperties = new LocalVariable("classProperties", ClassProperties.class, null, 2);
    mv = cw.visitMethod(ACC_PUBLIC, "<init>", methodDesc(void.class, Class.class, ClassProperties.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel();
    varThis.acceptLoad(mv);
    varPojoClass.acceptLoad(mv);
    varClassProperties.acceptLoad(mv);
    visitLineNumber(6, null);
    construct(BasePojomator.class, Class.class, ClassProperties.class);
    mv.visitInsn(RETURN);
    Label end = visitNewLabel();
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
    LocalVariable varThis = new LocalVariable("this", pojomatorInternalClassDesc, null, 0);
    LocalVariable varPojo1 = new LocalVariable("pojo1", pojoClass, pojoDescriptor, 1);
    LocalVariable varPojo2 = new LocalVariable("pojo2", pojoClass, pojoDescriptor, 2);

    StackAdjustments stackAdjustments = new StackAdjustments();

    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME};

    mv = cw.visitMethod(ACC_PUBLIC, "doEquals", methodDesc(boolean.class, Object.class, Object.class), null, null);

    // where to jump if we should return false
    Label returnFalse = new Label();
    // where to jump if we determine that pojo1 and pojo2 have types which are compatible for equality
    Label compatibleTypes = new Label();

    mv.visitCode();
    Label start = visitNewLabel();
    varPojo1.acceptLoad(mv);
    visitLineNumber(7, null);
    checkNotNull();
    varPojo2.acceptLoad(mv);
    visitLineNumber(8, null);
    Label notSameInstance = new Label();
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);

    // same instance; return true
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    mv.visitLabel(notSameInstance);

    // if other is null, return false.
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    visitLineNumber(9, null);
    varPojo2.acceptLoad(mv);
    mv.visitJumpInsn(IFNULL, returnFalse);

    // common case: if both types are the same, they are compatible for equality
    varThis.acceptLoad(mv);
    visitLineNumber(10, null);
    invokeVirtual(Object.class, "getClass", Class.class);
    varPojo1.acceptLoad(mv);
    visitLineNumber(11, null);
    invokeVirtual(Object.class, "getClass", Class.class);
    mv.visitJumpInsn(IF_ACMPEQ, compatibleTypes);

    // types are not the same; check for compatibility
    varThis.acceptLoad(mv);
    varPojo2.acceptLoad(mv);
    visitLineNumber(12, null);
    invokeVirtual(Object.class, "getClass", Class.class);
    visitLineNumber(13, null);
    invokeVirtual(BasePojomator.class, "isCompatibleForEquality", boolean.class, Class.class);
    mv.visitJumpInsn(IFEQ, returnFalse);

    // types are compatible, so start comparing properties
    mv.visitLabel(compatibleTypes);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);

    // Compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      visitLineNumber(14, propertyElement);
      visitAccessorAndConvert(varPojo1, propertyElement);
      visitLineNumber(15, propertyElement);
      visitAccessorAndConvert(varPojo2, propertyElement);
      visitLineNumber(16, propertyElement);
      compareProperties(mv, returnFalse, propertyElement, stackAdjustments);
    }
    // If we have gotten this far, all properties are equal, so return true.
    mv.visitInsn(ICONST_1);
    mv.visitInsn(IRETURN);

    mv.visitLabel(returnFalse);
    mv.visitFrame(F_FULL, 3, localVars, 0, NO_STACK);
    mv.visitInsn(ICONST_0);
    mv.visitInsn(IRETURN);

    Label end = visitNewLabel();
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo1.withScope(start, end).acceptLocalVariable(mv);
    varPojo2.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(2 + stackAdjustments.adjustments(2,  1), 3);
    mv.visitEnd();
  }

  /**
   * Compare a property from each pojo. It is assumed when this method is called that both property values have been
   * loaded onto the stack. In the event the property value is a float or double, it is further assumed that it has
   * been converted to an int or long.
   * @param mv
   * @param notEqualLabel where to jump if the property values are not equal
   * @param propertyElement the property being compared
   * @param stackAdjustments adjustments to be made to the max stack size, based on property type
   * @return {@code true} if the value of the property will take two positions on the stack (i.e. is a long or double)
   */
  private void compareProperties(
      MethodVisitor mv, Label notEqualLabel, PropertyElement propertyElement, StackAdjustments stackAdjustments) {
    Class<?> propertyType = propertyElement.getPropertyType();
    if (propertyType.isPrimitive()) {
      if (isWide(propertyElement)) {
        stackAdjustments.wideProperty = true;
        mv.visitInsn(LCMP);
        mv.visitJumpInsn(IFNE, notEqualLabel);
      }
      else {
        mv.visitJumpInsn(IF_ICMPNE, notEqualLabel);
      }
    }
    else {
      if(propertyType.isArray()) {
        Class<?> componentType = propertyType.getComponentType();
        if (componentType.isPrimitive()) {
          visitLineNumber(17, propertyElement);
          invokeStatic(Arrays.class, "equals",boolean.class, propertyType, propertyType);
        }
        else {
          visitLineNumber(18, propertyElement);
          invokeStatic(BasePojomator.class, "compareArrays", boolean.class, Object.class, Object.class);
        }
      }
      else {
        if (isObjectPossiblyHoldingArray(propertyElement)) {
          visitLineNumber(19, propertyElement);
          invokeStatic(BasePojomator.class, "areObjectValuesEqual", boolean.class, Object.class, Object.class);
        }
        else {
          visitLineNumber(20, propertyElement);
          invokeStatic(Objects.class,  "equals", boolean.class, Object.class, Object.class);
        }
      }
      mv.visitJumpInsn(IFEQ, notEqualLabel);
    }
  }

  /**
   * Generate the {@link Pojomator#doHashCode(Object)} method.
   * @param cw
   */
  private void makeDoHashCode(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", pojomatorInternalClassDesc, null, 0);
    LocalVariable varPojo = new LocalVariable("pojo", pojoClass, pojoDescriptor, 1);

    int longOrDoubleStackAdjustment = 0;
    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME};

    mv = cw.visitMethod(ACC_PUBLIC, "doHashCode", methodDesc(int.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel();
    visitLineNumber(21, null);
    varPojo.acceptLoad(mv);
    visitLineNumber(22, null);
    checkNotNullPop();

    //algorithm:
    // hashCode(prop_n) + 31 * (hashCode(prop_n-1) + 31 * ( ... (hashCode(prop_1) + 31 * 1) ... ))

    mv.visitInsn(ICONST_1); // this will just be multiplied by 31; let the optimizer take care of it

    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      // multiply what we have so far by 31.
      visitLineNumber(23, propertyElement);
      mv.visitIntInsn(BIPUSH, 31);
      visitLineNumber(24, propertyElement);
      mv.visitInsn(IMUL);

      visitLineNumber(25, propertyElement);
      visitAccessorAndConvert(varPojo, propertyElement); // grab the property value, converting a float or double
      Class<?> propertyType = propertyElement.getPropertyType();
      if (propertyType.isPrimitive()) {
        // need to compute the hash code for this primitive value, based on its type
        switch (propertyType.getName()) {
          case "boolean":
            visitLineNumber(26, propertyElement);
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

            visitLineNumber(27, propertyElement);
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
          visitLineNumber(28, propertyElement);

         invokeStatic(
           Arrays.class,
           isDeepArray(propertyElement) ? "deepHashCode" : "hashCode",
           int.class,
           propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class);
        }
        else if (isObjectPossiblyHoldingArray(propertyElement)) {
          // it *could* be an array; if so, we want to do an array hashCode.

          mv.visitInsn(DUP); // we'll still want the property value handy after calling getClass().isArray()
          invokeVirtual(Object.class, "getClass", Class.class);
          visitLineNumber(29, propertyElement);
          invokeVirtual(Class.class, "isArray", boolean.class);
          Label isArray = new Label();
          mv.visitJumpInsn(IFNE, isArray); // if true

          // regular old hashCode
          visitLineNumber(30, propertyElement);
          invokeVirtual(Object.class, "hashCode", int.class);
          mv.visitJumpInsn(GOTO, hashCodeDetermined);

          // add a deep parameter to arrayHashCode, like we did for compareProperties
          mv.visitLabel(isArray);
          mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] { INTEGER, Type.getInternalName(propertyType) });

          mv.visitInsn(ICONST_1);
          visitLineNumber(31, propertyElement);
          invokeStatic(BasePojomator.class, "arrayHashCode", int.class, Object.class, boolean.class);
        }
        else {
          visitLineNumber(32, propertyElement);
          invokeVirtual(Object.class, "hashCode", int.class);
        }

        mv.visitLabel(hashCodeDetermined);
        mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, INTEGER});
      }
      // add result to what we have so far
      mv.visitInsn(IADD);
    }
    mv.visitInsn(IRETURN);
    Label end = visitNewLabel();
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
    LocalVariable varThis = new LocalVariable("this", pojomatorInternalClassDesc, null, 0);
    LocalVariable varPojo = new LocalVariable("pojo", pojoClass, null, 1);
    LocalVariable varPojoFormatter=
      new LocalVariable("pojoFormattor", classDesc(EnhancedPojoFormatter.class), null, 2);
    LocalVariable varBuilder= new LocalVariable("builder", classDesc(String.class), null, 3);

    mv = cw.visitMethod(ACC_PUBLIC, "doToString", methodDesc(String.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel();
    varPojo.acceptLoad(mv);
    checkNotNullPop();

    constructEnhancedPojoFormatter();
    varPojoFormatter.acceptStore(mv);

    visitLineNumber(33, null);
    mv.visitTypeInsn(NEW, internalName(StringBuilder.class));
    mv.visitInsn(DUP);
    construct(StringBuilder.class);
    varBuilder.acceptStore(mv);

    varPojoFormatter.acceptLoad(mv);
    varBuilder.acceptLoad(mv);
    loadPojoClass(varThis);

    visitLineNumber(34, null);

    invokeInterface(EnhancedPojoFormatter.class,  "appendToStringPrefix", void.class, StringBuilder.class, Class.class);

    for(PropertyElement propertyElement: classProperties.getToStringProperties()) {
      if (isWide(propertyElement)) {
        longOrDoubleStackAdjustment = 1; // having any double-wide values on our stack increases max stack depth by one
      }

      // append the property prefix
      varPojoFormatter.acceptLoad(mv);
      varBuilder.acceptLoad(mv);
      visitLineNumber(35, propertyElement);
      loadPropertyElementField(propertyElement);
      visitLineNumber(36, propertyElement);
      invokeInterface(
        EnhancedPojoFormatter.class, "appendPropertyPrefix", void.class, StringBuilder.class, PropertyElement.class);

      // get the propertyFormatter for this property
      visitLineNumber(37, propertyElement);
      mv.visitFieldInsn(
        GETSTATIC,
        pojomatorInternalClassName,
        propertyFormatterName(propertyElement),
        classDesc(EnhancedPropertyFormatter.class));

      // The propertyFormatter will format the property value and append the results to our StringBuilder
      varBuilder.acceptLoad(mv);
      visitLineNumber(38, propertyElement);
      visitAccessor(varPojo, propertyElement);
      Class<?> appendType = appendFormattedType(propertyElement.getPropertyType());
      if (isObjectPossiblyHoldingArray(propertyElement)) {
        visitLineNumber(39, propertyElement);
        invokeInterface(
          EnhancedPropertyFormatter.class, "appendFormattedPossibleArray", void.class, StringBuilder.class, appendType);
      }
      else {
        visitLineNumber(40, propertyElement);
        invokeInterface(
          EnhancedPropertyFormatter.class,  "appendFormatted", void.class, StringBuilder.class, appendType);
      }

      // have any property suffix appended to the StringBuilder
      varPojoFormatter.acceptLoad(mv);
      varBuilder.acceptLoad(mv);
      visitLineNumber(41, propertyElement);
      loadPropertyElementField(propertyElement);
      visitLineNumber(42, propertyElement);
      invokeInterface(
        EnhancedPojoFormatter.class,  "appendPropertySuffix", void.class, StringBuilder.class, PropertyElement.class);
    }

    // Have any toString suffix appended
    varPojoFormatter.acceptLoad(mv);
    varBuilder.acceptLoad(mv);
    loadPojoClass(varThis);
    visitLineNumber(43, null);
    invokeInterface(EnhancedPojoFormatter.class,  "appendToStringSuffix", void.class, StringBuilder.class, Class.class);

    // invoke toString and return the result
    varBuilder.acceptLoad(mv);
    visitLineNumber(44, null);
    invokeVirtual(StringBuilder.class, "toString", String.class);
    mv.visitInsn(ARETURN);

    Label end = visitNewLabel();
    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo.withScope(start, end).acceptLocalVariable(mv);
    varPojoFormatter.withScope(start, end).acceptLocalVariable(mv);
    varBuilder.withScope(start, end).acceptLocalVariable(mv);
    mv.visitMaxs(3 + longOrDoubleStackAdjustment, 4);
    mv.visitEnd();
  }

  private static Class<?> appendFormattedType(Class<?> propertyType) {
    if (propertyType.isPrimitive()) {
      return propertyType;
    }
    else if (propertyType.isArray()) {
      return propertyType.getComponentType().isPrimitive() ? propertyType : Object[].class;
    }
    else {
      return Object.class;
    }
  }

  private void loadPropertyElementField(PropertyElement propertyElement) {
    mv.visitFieldInsn(
      GETSTATIC,
      pojomatorInternalClassName,
      propertyElementName(propertyElement),
      classDesc(PropertyElement.class));
  }

  /**
   * Construct the pojoFormatter to use. This method will contribute 2 or 4 to the max stack depth,
   * depending on whether the pojoFormatter implements {@link EnhancedPojoFormatter} or not.
   */
  @SuppressWarnings("deprecation")
  private void constructEnhancedPojoFormatter() {
    PojoFormat format = pojoClass.getAnnotation(PojoFormat.class);
    if (format == null) {
      mv.visitTypeInsn(NEW, internalName(DefaultEnhancedPojoFormatter.class));
      mv.visitInsn(DUP);
      visitLineNumber(45, null);
      construct(DefaultEnhancedPojoFormatter.class);
    }
    else {
      Class<? extends org.pojomatic.formatter.PojoFormatter> pojoFormatterClass = format.value();
      // if it isn't an enhanced formatter, we'll need to wrap it a EnhancedPojoFormatter. If we do this, we'll first
      // invoke new on the wrapper, then construct the underlying formatter, then call the constructor on the wrapper.
      boolean isEnhancedFormatter = EnhancedPojoFormatter.class.isAssignableFrom(pojoFormatterClass);
      if (! isEnhancedFormatter) {
        visitLineNumber(46, null);
        mv.visitTypeInsn(NEW, ENHANCED_POJO_FORMATTER_WRAPPER_INTERNAL_NAME);
        mv.visitInsn(DUP);
      }
      mv.visitTypeInsn(NEW, internalName(pojoFormatterClass));
      mv.visitInsn(DUP);
      visitLineNumber(47, null);
      construct(pojoFormatterClass);
      if (! isEnhancedFormatter) {
        visitLineNumber(48, null);
        construct(
          org.pojomatic.internal.EnhancedPojoFormatterWrapper.class, org.pojomatic.formatter.PojoFormatter.class);
      }
    }
  }

  /**
   * Load a reference to the pojo class. We cannot refer to this directly, since the class may not be visible to us,
   * so instead, we refer to the instance variable in {@link BasePojomator}.
   * @param varThis the "this" local variable.
   */
  private void loadPojoClass(LocalVariable varThis) {
    visitLineNumber(49, null);
    varThis.acceptLoad(mv);
    mv.visitFieldInsn(GETFIELD, BASE_POJOMATOR_INTERNAL_NAME, POJO_CLASS_FIELD_NAME, classDesc(Class.class));
    //mv.visitFieldInsn(GETSTATIC, pojomatorInternalClassName, POJO_CLASS_FIELD_NAME, classDesc(Class.class));
  }

  /**
   * Generate {@link Pojomator#doDiff(Object, Object)
   * @param cw
   */
  private void makeDoDiff(ClassVisitor cw) {
    LocalVariable varThis = new LocalVariable("this", pojomatorInternalClassDesc, null, 0);
    LocalVariable varPojo1 = new LocalVariable("instance", pojoClass, pojoDescriptor, 1);
    LocalVariable varPojo2 = new LocalVariable("other", pojoClass, pojoDescriptor, 2);
    LocalVariable varDifferencesList = new LocalVariable(
      "differences", List.class, "Ljava/util/List<Lorg/pojomatic/diff/Difference;>;", 3);

    StackAdjustments stackAdjustments = new StackAdjustments();
    Object[] localVarTypes = new Object[] {
      pojomatorInternalClassName, OBJECT_INTERNAL_NAME, OBJECT_INTERNAL_NAME, internalName(List.class), null, null };

    mv = cw.visitMethod(ACC_PUBLIC, "doDiff", methodDesc(Differences.class, Object.class, Object.class), null, null);
    mv.visitCode();
    Label start = visitNewLabel();
    varPojo1.acceptLoad(mv);
    visitLineNumber(50, null);
    checkNotNull("instance is null");
    varPojo2.acceptLoad(mv);
    checkNotNull("other is null");

    // If instance and other are the same object, then return NoDifferences.getInstance();
    Label notSameInstance = new Label();
    mv.visitJumpInsn(IF_ACMPNE, notSameInstance);
    invokeStatic(NoDifferences.class, "getInstance", NoDifferences.class);
    mv.visitInsn(ARETURN);

    // not the same instance, some work to do
    mv.visitLabel(notSameInstance);
    mv.visitFrame(F_FULL, 3, localVarTypes, 0, NO_STACK);
    visitLineNumber(51, null);
    checkCompatibleForEquality(varThis, varPojo1, "instance");
    visitLineNumber(52, null);
    checkCompatibleForEquality(varThis, varPojo2, "other");

    Label makeDiferences = notSameInstance;
    mv.visitTypeInsn(NEW, "java/util/ArrayList");
    mv.visitInsn(DUP);
    construct(ArrayList.class);
    varDifferencesList.acceptStore(mv);

    List<LocalVariable> propertyVariables = new ArrayList<>(); // these will occur in a block scope
    // compare properties
    for(PropertyElement propertyElement: classProperties.getHashCodeProperties()) {
      int width = isWide(propertyElement) ? 2 : 1;
      Class<?> propertyType = propertyElement.getPropertyType();
      LocalVariable varProp1 = new LocalVariable(
        "property_" + propertyElement.getName() + "_1", propertyType, null, 4);
      //If the type is long or double, we need to store the next var at 6, not 5.
      LocalVariable varProp2 = new LocalVariable(
        "property_" + propertyElement.getName() + "_2", propertyType, null, 4 + width);
      propertyVariables.add(varProp1);
      propertyVariables.add(varProp2);

      Label blockStart = visitNewLabel();

      visitLineNumber(53, propertyElement);
      visitAccessor(varPojo1, propertyElement);
      varProp1.acceptStore(mv);
      visitLineNumber(54, propertyElement);
      visitAccessor(varPojo2, propertyElement);
      varProp2.acceptStore(mv);

      visitLineNumber(55, propertyElement);
      visitAccessorAndConvert(varPojo1, propertyElement);
      visitLineNumber(56, propertyElement);
      visitAccessorAndConvert(varPojo2, propertyElement);

      Label propertiesNotEqual = new Label();
      Label next = new Label();
      visitLineNumber(57, propertyElement);
      compareProperties(mv, propertiesNotEqual, propertyElement, stackAdjustments);
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
      visitLineNumber(58, propertyElement);
      convertToObject(propertyType);
      varProp2.acceptLoad(mv);
      visitLineNumber(59, propertyElement);
      convertToObject(propertyType);
      visitLineNumber(60, propertyElement);
      construct(ValueDifference.class, String.class, Object.class, Object.class);

      // add the ValueDifference instance to our list
      visitLineNumber(61, propertyElement);
      invokeInterface(List.class, "add", boolean.class, Object.class);
      mv.visitInsn(POP); // ignore the return value of List#add
      mv.visitLabel(next);
      mv.visitFrame(F_FULL, 4, localVarTypes, 0, NO_STACK);

      varProp1.withScope(blockStart, next);
      varProp2.withScope(blockStart, next);
    }

    // if our list is empty, return the NoDifferences instance
    varDifferencesList.acceptLoad(mv);
    visitLineNumber(62, null);
    invokeInterface(List.class, "isEmpty", boolean.class);
    Label hasDifferences = new Label();
    mv.visitJumpInsn(IFEQ, hasDifferences);
    visitLineNumber(63, null);
    invokeStatic(NoDifferences.class, "getInstance", NoDifferences.class);
    mv.visitInsn(ARETURN);

    // our list is not empty, so wrap it in a PropertyDiferences instance
    mv.visitLabel(hasDifferences);
    mv.visitFrame(F_FULL, 4, localVarTypes, 0, NO_STACK);

    visitLineNumber(64, null);
    mv.visitTypeInsn(NEW, internalName(PropertyDifferences.class));
    mv.visitInsn(DUP);
    varDifferencesList.acceptLoad(mv);
    visitLineNumber(65, null);
    construct(PropertyDifferences.class, List.class);
    mv.visitInsn(ARETURN);

    Label end = visitNewLabel();

    varThis.withScope(start, end).acceptLocalVariable(mv);
    varPojo1.withScope(start, end).acceptLocalVariable(mv);
    varPojo2.withScope(start, end).acceptLocalVariable(mv);
    varDifferencesList.withScope(makeDiferences, end).acceptLocalVariable(mv);
    for (LocalVariable var: propertyVariables) {
      var.acceptLocalVariable(mv);
    }
    mv.visitMaxs(6 + stackAdjustments.adjustments(2, 0), 6 + stackAdjustments.adjustments(2, 0));
    mv.visitEnd();
  }

  /**
   * Invoke {@link BasePojomator#checkCompatibleForEquality(Object, String)} on the specified variable
   * @param message the message to include in the {@link IllegalArgumentException} if the variable fails the
   * class test
   * @param varNumber the variable number to check
   */
  private void checkCompatibleForEquality(LocalVariable varThis, LocalVariable var, String message) {
    varThis.acceptLoad(mv);
    var.acceptLoad(mv);
    mv.visitLdcInsn(message);
    visitLineNumber(66, null);
    invokeVirtual(BasePojomator.class, "checkCompatibleForEquality", void.class, Object.class, String.class);
  }

  /**
   * Determine if the given propertyElement should be treated as possibly containing a multi-level array.
   * This will be the case if it is:
   * <ul>
   *   <li>of type Object and is not annotated with @{@link SkipArrayCheck}</li>
   *   <li>of type Object[]</li>
   *   <li>of array type with a component type of array type</li>
   * </ul>
   * @param propertyElement
   * @return {@code true} if the given propertyElement should be treated as possibly containing a multi-level array,
   * or {@code false} otherwise.
   */
  private boolean isDeepArray(PropertyElement propertyElement) {
    Class<?> propertyType = propertyElement.getPropertyType();
    return
      propertyType.equals(Object[].class)
      || (propertyType.isArray() && propertyType.getComponentType().isArray())
      || ((propertyType.equals(Object.class) || propertyType.equals(Object[].class))
          && !propertyElement.getElement().isAnnotationPresent(SkipArrayCheck.class));
  }

  /**
   * Determine if the given propertyElement should be treated as one that could be an array.
   * @param propertyElement
   * @return {@code true} if the given propertyElement is either of array type, or is of type Object and not annotated
   * with @{@link SkipArrayCheck}
   */
  private boolean isObjectPossiblyHoldingArray(PropertyElement propertyElement) {
    return Object.class.equals(propertyElement.getPropertyType())
          && ! propertyElement.getElement().isAnnotationPresent(SkipArrayCheck.class);
  }

  /**
   * If the parameter on the stack (of type propertyType) is primitive, convert it to the appropriate wrapper object.
   * Otherwise, leave it alone
   * @param propertyType the type of the parameter on the stack
   */
  private void convertToObject(Class<?> propertyType) {
    if (propertyType.isPrimitive()) {
      Class<?> wrapperClass = Primitives.getWrapperClass(propertyType);
      invokeStatic(wrapperClass, "valueOf", wrapperClass, propertyType);
    }
  }

  /**
   * Visit an accessor, converting floats or doubles to int bits or long bits respectively.
   * @param propertyElement the property to access
   * @param variableNumber the index of the local variable holding a the pojo instance to access
   */
  private void visitAccessorAndConvert(LocalVariable var, PropertyElement propertyElement) {
    visitAccessor(var, propertyElement);
    if (propertyElement.getPropertyType().equals(float.class)) {
      invokeStatic(Float.class, "floatToIntBits", int.class, float.class);
    }
    else if (propertyElement.getPropertyType().equals(double.class)) {
      invokeStatic(Double.class, "doubleToLongBits", long.class, double.class);
    }
  }

  /**
   * Visit an accessor
   * @param var the index of the local variable holding a the pojo instance to access
   * @param propertyElement the property to access
   */
  private void visitAccessor(LocalVariable var, PropertyElement propertyElement) {
    var.acceptLoad(mv);
    mv.visitMethodInsn(
      INVOKESTATIC, pojomatorInternalClassName, propertyAccessorName(propertyElement),
      accessorMethodDescription(propertyElement),
      false);
  }

  private String accessorMethodDescription(PropertyElement propertyElement) {
    return methodDesc(effectiveType(propertyElement.getPropertyType()), Object.class);
  }

  /**
   * Determine what, for our purposes, is the effective type of a property. Since a property type may be a class which
   * is not visible to us, we'll treat any type which is neither primitive nor an array to be of type Object.
   * A primitive type or array of primitives will be returned as is. All other array types will be returned as Object[].
   * @param propertyClass the class to determine the effective type for.
   * @return the effective type of {@code propertyClass}
   */
  private Class<?> effectiveType(Class<?> propertyClass) {
    if (propertyClass.isArray()) {
      return propertyClass.getComponentType().isPrimitive() ? propertyClass : Object[].class;
    }
    else {
      return propertyClass.isPrimitive() ? propertyClass : Object.class;
    }
  }

  /**
   * Create a new label and visit it.
   * @return the new label
   */
  private Label visitNewLabel() {
    Label label = new Label();
    mv.visitLabel(label);
    return label;
  }

  /**
   * Visit a line number, based on a provided number, and a propertyElement (possibly null). The propertyElement will
   * be used to distinguish line numbers generated from the same place in the code of this class, but for different
   * properties.
   * </p>
   * To ensure unique line numbers, run the following:
   * <code>
   *   perl -pi -e 'if (/visitLineNumber\(/) { $i++; s/visitLineNumber\(mv, \d+/visitLineNumber\(mv, $i/; }' \
   *     src/main/java/org/pojomatic/internal/PojomatorByteCodeGenerator.java
   * </code>
   * @param lineNumberBase
   * @param propertyElement
   */
  private void visitLineNumber(int lineNumberBase, PropertyElement propertyElement) {
    Integer offset = propertyNumbers.get(propertyElement);
    mv.visitLineNumber(lineNumberBase + 100 * (offset == null ? 0 : offset), visitNewLabel());
  }

  /**
   * Determine if the type of a property is "wide" - i.e. is a long or double.
   * @param propertyElement
   * @return
   */
  private static boolean isWide(PropertyElement propertyElement) {
    Class<?> type = propertyElement.getPropertyType();
    return type == long.class || type == double.class;
  }

  /**
   * Pop the top element off of the stack and invoke {@link BasePojomator#checkNotNull(Object)} on it.
   */
  private void checkNotNullPop() {
    invokeStatic(BasePojomator.class, "checkNotNullPop", void.class, Object.class);
  }

  /**
   * Invoke {@link BasePojomator#checkNotNull(Object)} on the top element of the stack, leaving that element there.
   */
  private void checkNotNull() {
    invokeStatic(BasePojomator.class, "checkNotNull", Object.class, Object.class);
  }

  /**
   * Invoke {@link BasePojomator#checkNotNull(Object, String)} on the top element of the stack, leaving that element there.
   * @param message the message to include in the {@link NullPointerException} if the top element is null
   */
  private void checkNotNull(String message) {
    mv.visitLdcInsn(message);
    invokeStatic(BasePojomator.class, "checkNotNull", Object.class, Object.class, String.class);
  }

  private void invokeStatic(Class<?> ownerClass, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
    mv.visitMethodInsn(
      INVOKESTATIC, internalName(ownerClass), methodName, methodDesc(returnType, parameterTypes), false);
  }

  private void invokeInterface(
    Class<?> ownerClass, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
    mv.visitMethodInsn(
      INVOKEINTERFACE, internalName(ownerClass), methodName, methodDesc(returnType, parameterTypes), true);
  }

  private void invokeVirtual(
    Class<?> ownerClass, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
    mv.visitMethodInsn(
      INVOKEVIRTUAL, internalName(ownerClass), methodName, methodDesc(returnType, parameterTypes), false);
  }

  private void construct(
    Class<?> ownerClass, Class<?>... parameterTypes) {
    mv.visitMethodInsn(
      INVOKESPECIAL, internalName(ownerClass), "<init>", methodDesc(void.class, parameterTypes), false);
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

  private static String methodDesc(Class<?> returnType, Class<?>... parameterTypes) {
    return MethodType.methodType(returnType, parameterTypes).toMethodDescriptorString();
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
