package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

import static org.objectweb.asm.Opcodes.*;

/*
 * 1) Break up byte code generation into a separate class that takes a class visitor
 * 2) Have a generating classloader per client classloader, and have the methodname passed to invokedynamic
 *    include the classname. Store these custom classloaders in WeakHashMap<ClassLoader, WeakReference<ClassLoader>>.
 */

public class PojomatorFactory {
  private static final String BOOTSTRAP_METHOD_DESCRIPTOR =
    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class)
    .toMethodDescriptorString();
  private static final String EXTENDED_BOOTSTRAP_METHOD_DESCRIPTOR =
    MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, Class.class)
    .toMethodDescriptorString();
  private static final String POJO_CLASS_FIELD_NAME = "pojoClass";
  private static final String POJOMATOR_SIG = internalName(Pojomator.class);
  private static final String OBJECT_INTERNAL_NAME = internalName(Object.class);
  private static final String FIELD_PREFIX = "field_";
  private static final String METHOD_PREFIX = "method_";
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

  public static <T> Pojomator<T> makePojomator(Class<T> pojoClass)
      throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
    ClassProperties classProperties = ClassProperties.forClass(pojoClass);
    PojomatorFactory pojomatorFactory = new PojomatorFactory(pojoClass, classProperties);
    Class<?> pojomatorClass = CLASS_LOADER.loadClass(
      pojomatorFactory.pojomatorClassName, pojomatorFactory.makeClassBytes());
    Field field = pojomatorClass.getDeclaredField(POJO_CLASS_FIELD_NAME);
    field.setAccessible(true);
    field.set(null, pojoClass);
    @SuppressWarnings("unchecked")
    Pojomator<T> pojomator = (Pojomator<T>) pojomatorClass.newInstance();
    return pojomator;
  }

  /**
   * Construct a call site for a property accessor. Because {@code pojoClass} might not be a public class, the
   * parameter in {@code methodType} cannot be {@code pojoClass}, but instead must be just {@code Object.class}. The
   * {@code pojoClass} parameter will be stored as static field in the pojomator class, and passed in from it's
   * bootstrap method.
   * @param caller A Lookup from the original call site.
   * @param name the name of the dynamic method. This should either be "field_&lt;fieldName&gt;" or "method_&lt;methodName&gt;".
   * @param methodType the type of the dynamic method; the return type should be the type of the aforementioned field
   *   or method
   * @param pojoClass the type of the pojo class
   * @return a CallSite which invokes the method or gets the field value.
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType methodType, Class<?> pojoClass)
      throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
    return new ConstantCallSite(
      MethodHandles.explicitCastArguments(
        getTypedMethod(caller, name, pojoClass),
        MethodType.methodType(methodType.returnType(), Object.class)));
  }

  /**
   * Get a method handle to access a field or invoke a no-arg method.
   * @param caller A Lookup from the original call site.
   * @param name the name of the dynamic method. This should either be "field_&lt;fieldName&gt;" or "method_&lt;methodName&gt;".
   * @param pojoClass the type of the pojo class
   * @return
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   */
  private static MethodHandle getTypedMethod(MethodHandles.Lookup caller, String name, Class<?> pojoClass)
    throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
    if (name.startsWith(FIELD_PREFIX)) {
      String fieldName = name.substring(FIELD_PREFIX.length());
      Field field = pojoClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return caller.unreflectGetter(field);
    }
    else if (name.startsWith(METHOD_PREFIX)) {
      String methodName = name.substring(METHOD_PREFIX.length());
      Method method = pojoClass.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return caller.unreflect(method);
    }
    else {
      throw new IllegalArgumentException("Cannot handle method named " + name);
    }
  }

  private final String pojomatorClassName;
  private final String pojomatorInternalClassName;
  private final Class<?> pojoClass;
  private final String pojoInternalName;
  private final ClassProperties classProperties;
  private final Handle bootstrapMethod;

  public PojomatorFactory(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojomatorClassName = pojoClass.getName() + "$$Pojomator";
    this.pojomatorInternalClassName = internalName(pojomatorClassName);
    this.pojoClass = pojoClass;
    this.pojoInternalName = Type.getInternalName(pojoClass);
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
        OBJECT_INTERNAL_NAME, new String[] { POJOMATOR_SIG });
    classWriter.visitSource("FIXME", null);
    makePojoClassField(classWriter);

    makeConstructor(classWriter);
    makeBootstrapMethod(classWriter);
    for (PropertyElement propertyElement: classProperties.getAllProperties()) {
      makeAccessor(classWriter, propertyElement);
    }
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
      INVOKESTATIC, Type.getInternalName(PojomatorFactory.class),
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
      if (propertyType == long.class || propertyType == double.class) {
        maxStackSize++;
        mv.visitInsn(LRETURN);
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
    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    Label l0 = visitNewLabel(mv);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, OBJECT_INTERNAL_NAME, "<init>", "()V");
    mv.visitInsn(RETURN);
    Label l1 = visitNewLabel(mv);
    mv.visitLocalVariable("this", classDesc(pojomatorClassName), null, l0, l1, 0);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
  }
/*
 *
{
mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(29, l0);
mv.visitInsn(ICONST_1);
mv.visitVarInsn(ISTORE, 1);
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(30, l1);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "age", "I");
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l2 = new Label();
mv.visitLabel(l2);
mv.visitLineNumber(31, l2);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "boolVal", "Z");
Label l3 = new Label();
mv.visitJumpInsn(IFEQ, l3);
mv.visitIntInsn(SIPUSH, 1231);
Label l4 = new Label();
mv.visitJumpInsn(GOTO, l4);
mv.visitLabel(l3);
mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {"beans/Person", Opcodes.INTEGER}, 1, new Object[] {Opcodes.INTEGER});
mv.visitIntInsn(SIPUSH, 1237);
mv.visitLabel(l4);
mv.visitFrame(Opcodes.F_FULL, 2, new Object[] {"beans/Person", Opcodes.INTEGER}, 2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER});
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l5 = new Label();
mv.visitLabel(l5);
mv.visitLineNumber(32, l5);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "byteVal", "B");
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l6 = new Label();
mv.visitLabel(l6);
mv.visitLineNumber(33, l6);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "charVal", "C");
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l7 = new Label();
mv.visitLabel(l7);
mv.visitLineNumber(35, l7);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "doubleVal", "D");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
mv.visitVarInsn(LSTORE, 2);
Label l8 = new Label();
mv.visitLabel(l8);
mv.visitLineNumber(36, l8);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(LLOAD, 2);
mv.visitVarInsn(LLOAD, 2);
mv.visitIntInsn(BIPUSH, 32);
mv.visitInsn(LUSHR);
mv.visitInsn(LXOR);
mv.visitInsn(L2I);
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l9 = new Label();
mv.visitLabel(l9);
mv.visitLineNumber(37, l9);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "floatVal", "F");
mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l10 = new Label();
mv.visitLabel(l10);
mv.visitLineNumber(38, l10);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "longVal", "J");
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "longVal", "J");
mv.visitIntInsn(BIPUSH, 32);
mv.visitInsn(LUSHR);
mv.visitInsn(LXOR);
mv.visitInsn(L2I);
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l11 = new Label();
mv.visitLabel(l11);
mv.visitLineNumber(39, l11);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
Label l12 = new Label();
mv.visitJumpInsn(IFNONNULL, l12);
mv.visitInsn(ICONST_0);
Label l13 = new Label();
mv.visitJumpInsn(GOTO, l13);
mv.visitLabel(l12);
mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"beans/Person", Opcodes.INTEGER, Opcodes.LONG}, 1, new Object[] {Opcodes.INTEGER});
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "name", "Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I");
mv.visitLabel(l13);
mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"beans/Person", Opcodes.INTEGER, Opcodes.LONG}, 2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER});
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l14 = new Label();
mv.visitLabel(l14);
mv.visitLineNumber(40, l14);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
Label l15 = new Label();
mv.visitLabel(l15);
mv.visitLineNumber(41, l15);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
Label l16 = new Label();
mv.visitJumpInsn(IFNONNULL, l16);
mv.visitInsn(ICONST_0);
Label l17 = new Label();
mv.visitJumpInsn(GOTO, l17);
mv.visitLabel(l16);
mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "publicName", "Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I");
mv.visitLabel(l17);
mv.visitLineNumber(40, l17);
mv.visitFrame(Opcodes.F_FULL, 3, new Object[] {"beans/Person", Opcodes.INTEGER, Opcodes.LONG}, 2, new Object[] {Opcodes.INTEGER, Opcodes.INTEGER});
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l18 = new Label();
mv.visitLabel(l18);
mv.visitLineNumber(42, l18);
mv.visitIntInsn(BIPUSH, 31);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IMUL);
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "beans/Person", "shortVal", "S");
mv.visitInsn(IADD);
mv.visitVarInsn(ISTORE, 1);
Label l19 = new Label();
mv.visitLabel(l19);
mv.visitLineNumber(43, l19);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IRETURN);
Label l20 = new Label();
mv.visitLabel(l20);
mv.visitLocalVariable("this", "Lbeans/Person;", null, l0, l20, 0);
mv.visitLocalVariable("result", "I", null, l1, l20, 1);
mv.visitLocalVariable("temp", "J", null, l8, l20, 2);
mv.visitMaxs(6, 4);
mv.visitEnd();
}
 */
  private void makeDoHashCode(ClassVisitor cw) {
    int longOrDoubleStackAdjustment = 0;

    Object[] localVars = new Object[] {pojomatorInternalClassName, OBJECT_INTERNAL_NAME, INTEGER, INTEGER};

    MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "doHashCode", "(" + classDesc(Object.class) + ")I", null, null);
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
      mv.visitVarInsn(ALOAD, 1);
      visitLineNumber(mv, 5);
      mv.visitMethodInsn(
        INVOKESTATIC, pojomatorInternalClassName, accessorName(propertyElement),
        accessorMethodType(propertyElement));
      if (propertyElement.getPropertyType().isPrimitive()) {
        // need to compute the hash code for this primitive value, based on its type
        switch (propertyElement.getPropertyType().getName()) {
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
            break; // already an int
          case "float":
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "floatToIntBits", "(F)I");
            break;
          case "double":
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "doubleToLongBits", "(D)J");
            //$FALL-THROUGH$
          case "long":
            longOrDoubleStackAdjustment = 3;
            mv.visitInsn(DUP2);
            mv.visitIntInsn(BIPUSH, 32);
            mv.visitInsn(LUSHR);
            mv.visitInsn(LXOR);
            mv.visitInsn(L2I);
            break;
          default:
            throw new IllegalStateException("unknown primitive class " + propertyElement.getPropertyType().getName());
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

        mv.visitLabel(ifNonNull);
        mv.visitFrame(F_FULL, 2, localVars, 2, new Object[] {INTEGER, OBJECT_INTERNAL_NAME});
        mv.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INTERNAL_NAME, "hashCode", "()I");

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
