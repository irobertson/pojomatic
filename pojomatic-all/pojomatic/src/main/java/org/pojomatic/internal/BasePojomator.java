package org.pojomatic.internal;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.pojomatic.Pojomator;

public abstract class BasePojomator<T> implements Pojomator<T> {
  private static final String FIELD_PREFIX = "field_";
  private static final String METHOD_PREFIX = "method_";

  private final ClassProperties classProperties;

  protected BasePojomator(ClassProperties classProperties) {
    this.classProperties = classProperties;
  }

  @Override
  public boolean isCompatibleForEquality(Class<?> otherClass) {
    return classProperties.isCompatibleForEquals(otherClass);
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
   * @param pojoClass the type of the pojo class
   * @return a CallSite which invokes the method or gets the field value.
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException
   * @throws IllegalAccessException
   */
  protected static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType methodType, Class<?> pojoClass)
      throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
    return new ConstantCallSite(
      MethodHandles.explicitCastArguments(
        getTypedMethod(caller, name, pojoClass),
        MethodType.methodType(methodType.returnType(), Object.class)));
  }

  /**
   * Compare two values with a static type a proper sub-type of Object for equality. In particular, it is assumed that
   * neither value is an array.
   * @param instanceValue the first value to compare
   * @param otherValue the second value to compare
   * @return true if {@code instanceValue} and {@code otherValue} are equal to each other.
   */
  protected static boolean areNonArrayValuesEqual(Object instanceValue, Object otherValue) {
    if (instanceValue == otherValue) {
      return true;
    }
    if (instanceValue == null || otherValue == null) {
      return false;
    }
    return instanceValue.equals(otherValue);
  }

  /**
   * Compare two values of static type Object for equality. If both values are arrays of the same primitive component
   * type, or if both values are arrays of non-primitive component type, then the appropriate {@code equals} method
   * on {@link Arrays} is used to determine equality.
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
        if (!otherValue.getClass().isArray()) {
          return false;
        }
        final Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();
        if (!instanceComponentClass.isPrimitive()) {
          if (otherValue.getClass().getComponentType().isPrimitive()) {
            return false;
          }
          if (!Arrays.deepEquals((Object[]) instanceValue, (Object[]) otherValue)) {
            return false;
          }
        }
        else { // instanceComponentClass is primitive
          if (otherValue.getClass().getComponentType() != instanceComponentClass) {
            return false;
          }

          if (Boolean.TYPE == instanceComponentClass) {
            if (!Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue)) {
              return false;
            }
          }
          else if (Byte.TYPE == instanceComponentClass) {
            if (!Arrays.equals((byte[]) instanceValue, (byte[]) otherValue)) {
              return false;
            }
          }
          else if (Character.TYPE == instanceComponentClass) {
            if (!Arrays.equals((char[]) instanceValue, (char[]) otherValue)) {
              return false;
            }
          }
          else if (Short.TYPE == instanceComponentClass) {
            if (!Arrays.equals((short[]) instanceValue, (short[]) otherValue)) {
              return false;
            }
          }
          else if (Integer.TYPE == instanceComponentClass) {
            if (!Arrays.equals((int[]) instanceValue, (int[]) otherValue)) {
              return false;
            }
          }
          else if (Long.TYPE == instanceComponentClass) {
            if (!Arrays.equals((long[]) instanceValue, (long[]) otherValue)) {
              return false;
            }
          }
          else if (Float.TYPE == instanceComponentClass) {
            if (!Arrays.equals((float[]) instanceValue, (float[]) otherValue)) {
              return false;
            }
          }
          else if (Double.TYPE == instanceComponentClass) {
            if (!Arrays.equals((double[]) instanceValue, (double[]) otherValue)) {
              return false;
            }
          }
          else {
            // should NEVER happen
            throw new IllegalStateException(
              "unknown primitive type " + instanceComponentClass.getName());
          }
        }
      }
    }
    return true;
  }


  /**
   * Given an object which is of array type, compute it's hashCode by calling the appropriate signature of
   * {@link Arrays}{@code .hashCode()}
   * @param array
   * @return
   */
  protected static int arrayHashCode(Object array) {
    Class<?> componentType = array.getClass().getComponentType();
    if (! componentType.isPrimitive()) {
      return Arrays.hashCode((Object[]) array);
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
}
