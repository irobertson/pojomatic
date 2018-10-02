package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;

public abstract class BasePojomator<T> implements Pojomator<T> {
  protected final Class<?> pojoClass;
  private final ClassProperties classProperties;

  protected BasePojomator(Class<?> pojoClass, ClassProperties classProperties) {
    this.pojoClass = pojoClass;
    this.classProperties = classProperties;
  }

  @Override
  public boolean isCompatibleForEquality(Class<?> otherClass) {
    return classProperties.isCompatibleForEquals(otherClass);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Pojomator for ").append(pojoClass.getName()).append(" with equals properties ");
    propertiesList(builder, classProperties.getEqualsProperties());
    builder.append(", hashCodeProperties ");
    propertiesList(builder, classProperties.getHashCodeProperties());
    builder.append(", and toStringProperties ");
    propertiesList(builder, classProperties.getToStringProperties());
    return builder.toString();
  }

  private void propertiesList(StringBuilder builder, final Iterable<PropertyElement> properties) {
    builder.append("{");
    boolean firstElement = true;
    for (PropertyElement prop: properties) {
      if (!firstElement) {
        builder.append(",");
      }
      else {
        firstElement = false;
      }
      builder.append(prop.getName());
    }
    builder.append("}");
  }

  /**
   * Construct a call site for a property accessor. Because {@code pojoClass} might not be a public class, the
   * parameter in {@code methodType} cannot be {@code pojoClass}, but instead must be just {@code Object.class}. The
   * {@code pojoClass} parameter will be stored as static field in the Pojomator class, and passed in from it's
   * bootstrap method.
   * @param caller A Lookup from the original call site.
   * @param name the name of the dynamic method. This should either be "field_&lt;fieldName&gt;" or "method_&lt;methodName&gt;".
   * @param methodType the type of the dynamic method; the return type should be the type of the aforementioned field
   *   or method
   * @param pojomatorClass the type of the pojomator class
   * @return a CallSite which invokes the method or gets the field value.
   * @throws Throwable if there are reflection issues
   */
  protected static CallSite bootstrap(
      MethodHandles.Lookup caller, String name, MethodType methodType, Class<?> pojomatorClass)
      throws Throwable {
    return new ConstantCallSite(
      MethodHandles.explicitCastArguments(
        getTypedMethod(caller, name, pojomatorClass),
        MethodType.methodType(methodType.returnType(), Object.class)));
  }

  /**
   * Compare two values of static type Object for equality. If both values are arrays, then they will be considered
   * equal iff they have the same class, and (recursively) an equal set of elements.
   * @param instanceValue the first value to compare
   * @param otherValue the second value to compare
   * @return true if {@code instanceValue} and {@code otherValue} are equal to each other.
   */
  protected static boolean areObjectValuesEqual(Object instanceValue, Object otherValue) {
    if (instanceValue == otherValue) {
      return true;
    }
    if (instanceValue == null || otherValue == null) {
      return false;
    }
    else {
      if (!instanceValue.getClass().isArray()) {
        if (!instanceValue.equals(otherValue)) {
          return false;
        }
      }
      else {
        return compareArrays(instanceValue, otherValue);
      }
    }
    return true;
  }

  /**
   * Compare two values of array type for equality. They will be considered
   * equal iff they have the same class, and (recursively) an equal set of elements.
   * @param instanceValue the first value to compare
   * @param otherValue the second value to compare
   * @return true if {@code instanceValue} and {@code otherValue} are equal to each other.
   */
  protected static boolean compareArrays(Object instanceValue, Object otherValue) {
    if (instanceValue == otherValue) {
      return true;
    }
    if (instanceValue == null || otherValue == null) {
      return false;
    }
    if (!instanceValue.getClass().equals(otherValue.getClass())) {
      return false;
    }
    final Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();

    if (!instanceComponentClass.isPrimitive()) {
      Object[] instanceArray = (Object[]) instanceValue;
      Object[] otherArray = (Object[]) otherValue;
      if (instanceArray.length != otherArray.length) {
        return false;
      }
      for (int i = 0; i < instanceArray.length; i++) {
        if (!areObjectValuesEqual(instanceArray[i], otherArray[i])) {
          return false;
        }
      }
      return true;
    }
    else { // instanceComponentClass is primitive
      if (Boolean.TYPE == instanceComponentClass) {
        return Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue);
      }
      else if (Byte.TYPE == instanceComponentClass) {
        return Arrays.equals((byte[]) instanceValue, (byte[]) otherValue);
      }
      else if (Character.TYPE == instanceComponentClass) {
        return Arrays.equals((char[]) instanceValue, (char[]) otherValue);
      }
      else if (Short.TYPE == instanceComponentClass) {
        return Arrays.equals((short[]) instanceValue, (short[]) otherValue);
      }
      else if (Integer.TYPE == instanceComponentClass) {
        return Arrays.equals((int[]) instanceValue, (int[]) otherValue);
      }
      else if (Long.TYPE == instanceComponentClass) {
        return Arrays.equals((long[]) instanceValue, (long[]) otherValue);
      }
      else if (Float.TYPE == instanceComponentClass) {
        return Arrays.equals((float[]) instanceValue, (float[]) otherValue);
      }
      else if (Double.TYPE == instanceComponentClass) {
        return Arrays.equals((double[]) instanceValue, (double[]) otherValue);
      }
      else {
        // should NEVER happen
        throw new IllegalStateException(
          "unknown primitive type " + instanceComponentClass.getName());
      }
    }
  }


  /**
   * Given an object which is of array type, compute it's hashCode by calling the appropriate signature of
   * {@link Arrays}{@code .hashCode()}
   * @param array the array
   * @param deepArray whether to do a deep hashCode for Object arrays.
   * @return the hashCode
   */
  protected static int arrayHashCode(Object array, boolean deepArray) {
    Class<?> componentType = array.getClass().getComponentType();
    if (! componentType.isPrimitive()) {
      return deepArray ? Arrays.deepHashCode((Object[]) array) : Arrays.hashCode((Object[]) array);
    }
    if (componentType == boolean.class) {
      return Arrays.hashCode((boolean[]) array);
    }
    if (componentType == byte.class) {
      return Arrays.hashCode((byte[]) array);
    }
    if (componentType == char.class) {
      return Arrays.hashCode((char[]) array);
    }
    if (componentType == short.class) {
      return Arrays.hashCode((short[]) array);
    }
    if (componentType == int.class) {
      return Arrays.hashCode((int[]) array);
    }
    if (componentType == long.class) {
      return Arrays.hashCode((long[]) array);
    }
    if (componentType == float.class) {
      return Arrays.hashCode((float[]) array);
    }
    if (componentType == double.class) {
      return Arrays.hashCode((double[]) array);
    }
    throw new IllegalStateException("unknown primitive type " + componentType.getName());
  }

  protected static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  protected static <T> T checkNotNull(T reference, String message) {
    if (reference == null) {
      throw new NullPointerException(message);
    }
    return reference;
  }

  protected static void checkNotNullPop(Object reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
  }

  protected void checkCompatibleForEquality(T instance, String label) {
    if (!isCompatibleForEquality(instance.getClass())) {
      throw new IllegalArgumentException(
        label + " has type " + instance.getClass().getName()
        + " which is not compatible for equality with " + pojoClass.getName());
    }
  }

  /**
   * Get a method handle to access a field or invoke a no-arg method.
   * @param caller A Lookup from the original call site.
   * @param name the name of the dynamic method. This should be of the form "get_xxx", where "element_xxx" will be a
   * static field containing a {@link PropertyElement} instance referring to the property to be accessed.
   * @param pojomatorClass the type of the pojomator class
   * @return the MethodHandle
   * @throws Throwable
   */
  private static MethodHandle getTypedMethod(
    final MethodHandles.Lookup caller, final String name, final Class<?> pojomatorClass)
    throws Throwable {
    try {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<MethodHandle>() {
        @Override
        public MethodHandle run() throws Exception {
          return getTypedMethodPrivileged(caller, name, pojomatorClass);
        }
      });
    } catch (PrivilegedActionException e) {
      throw e.getCause();
    }
  }

  /**
   * Do the work for {@link #getTypedMethod(java.lang.invoke.MethodHandles.Lookup, String, Class)}. This method will be
   * run inside of a {@link AccessController#doPrivileged(PrivilegedExceptionAction)} block, hence should make sure to
   * not run untrusted code.
   * @param pojomatorClass the type of the pojomator class
   * @return the MethodHandle
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  private static MethodHandle getTypedMethodPrivileged(
    MethodHandles.Lookup caller, String name, Class<?> pojomatorClass)
    throws NoSuchFieldException, IllegalAccessException {
    String elementName = "element_" + name.substring(4);
    Field elementField = pojomatorClass.getDeclaredField(elementName);
    elementField.setAccessible(true);
    PropertyElement property = (PropertyElement) elementField.get(null);
    AnnotatedElement element = property.getElement();
    // Note that while element is a reference to untrusted code, we do not actually invoke this code inside a
    // doPrivileged block - we merely make it accessible to be invoked later, outside of a doPriviliged block
    if (element instanceof Field) {
      Field field = (Field) element;
      field.setAccessible(true);
      return caller.unreflectGetter(field);
    }
    else if (element instanceof Method) {
      Method method = (Method) element;
      method.setAccessible(true);
      return caller.unreflect(method);
    }
    else {
      throw new IllegalArgumentException("Cannot handle element of type " + element.getClass().getName());
    }
  }

}
