package org.pojomatic.internal;

import java.util.Arrays;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;

public class PojomatorImpl<T> implements Pojomator<T>{
  public PojomatorImpl(Class<T> clazz) {
    this.clazz = clazz;
    classProperties = new ClassProperties(clazz);
  }

  public boolean doEquals(T instance, Object other) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    if (instance == other) {
      return true;
    }
    if (! clazz.isInstance(other)) {
      return false;
    }

    for (PropertyElement prop: classProperties.getEqualsProperties()) {
      Object instanceValue = prop.getValue(instance);
      Object otherValue = prop.getValue(other);
      if (instanceValue == null) {
        if (otherValue != null) {
          return false;
        }
      }
      else { // instanceValue is not null
        if (otherValue == null) {
          return false;
        }
        if (!instanceValue.getClass().isArray()) {
          if (!instanceValue.equals(otherValue)) {
            return false;
          }
        }
        else {
          if (!otherValue.getClass().isArray()) {
            return false;
          }
          //TODO - decide if we should choose to enter this branch based off of the property's
          // declared or runtime type.  Either way, document the choice.  For now, runtime.
          Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();
          Class<?> otherComponentClass = otherValue.getClass().getComponentType();

          if (!instanceComponentClass.isPrimitive()) {
            if (otherComponentClass.isPrimitive()) {
              return false;
            }
            if (!Arrays.deepEquals((Object[]) instanceValue, (Object[]) otherValue)) {
              return false;
            }
          }
          else { // instanceComponentClass is primative
            if (otherComponentClass != instanceComponentClass) {
              return false;
            }

            if (Boolean.TYPE == instanceComponentClass) {
              if(!Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue)) {
                return false;
              }
            }
            else if (Byte.TYPE == instanceComponentClass) {
              if (! Arrays.equals((byte[]) instanceValue, (byte[]) otherValue)) {
                return false;
              }
            }
            else if (Character.TYPE == instanceComponentClass) {
              if(!Arrays.equals((char[]) instanceValue, (char[]) otherValue)) {
                return false;
              }
            }
            else if (Short.TYPE == instanceComponentClass) {
              if(!Arrays.equals((short[]) instanceValue, (short[]) otherValue)) {
                return false;
              }
            }
            else if (Integer.TYPE == instanceComponentClass) {
              if(!Arrays.equals((int[]) instanceValue, (int[]) otherValue)) {
                return false;
              }
            }
            else if (Long.TYPE == instanceComponentClass) {
              if(!Arrays.equals((long[]) instanceValue, (long[]) otherValue)) {
                return false;
              }
            }
            else if (Float.TYPE == instanceComponentClass) {
              if(!Arrays.equals((float[]) instanceValue, (float[]) otherValue)) {
                return false;
              }
            }
            else if (Double.TYPE == instanceComponentClass) {
              if(!Arrays.equals((double[]) instanceValue, (double[]) otherValue)) {
                return false;
              }
            }
          }
        }
      }
    }
    return true;
  }

  public int doHashCode(T instance) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    return 0;
  }

  public String doToString(T instance) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    return null;
  }

  private final Class<T> clazz;
  private final ClassProperties classProperties;
}
