package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

public class DefaultPropertyFormatter implements PropertyFormatter {
  public void initialize(@SuppressWarnings("unused") AnnotatedElement element) {
    //Not applicable
  }


  public String format(Object value) {
    if (value == null) {
      return "null";
    }
    else if (value.getClass().isArray()) {
      Class<?> componentClass = value.getClass().getComponentType();
      if (componentClass.isPrimitive()) {
        if (Boolean.TYPE == componentClass) {
          return Arrays.toString((boolean[]) value);
        }
        if (Character.TYPE == componentClass) {
          StringBuilder builder = new StringBuilder().append('[');
          boolean seenOne = false;
          for (char c: ((char[]) value)) {
            if(seenOne) {
              builder.append(", ");
            }
            else {
              seenOne = true;
            }
            builder.append('\'');
            if (Character.isISOControl(c)) {
              builder.append("0x").append(Integer.toHexString(c));
            }
            else {
              builder.append(c);
            }
            builder.append('\'');
          }
          return builder.append(']').toString();
        }
        if (Byte.TYPE == componentClass) {
          return Arrays.toString((byte[]) value);
        }
        if (Short.TYPE == componentClass) {
          return Arrays.toString((short[]) value);
        }
        if (Integer.TYPE == componentClass) {
          return Arrays.toString((int[]) value);
        }
        if (Long.TYPE == componentClass) {
          return Arrays.toString((long[]) value);
        }
        if (Float.TYPE == componentClass) {
          return Arrays.toString((float[]) value);
        }
        if (Double.TYPE == componentClass) {
          return Arrays.toString((double[]) value);
        }
        else {
          throw new IllegalStateException("unexpected primative array base type: " + componentClass);
        }
      }
      else {
        return Arrays.deepToString((Object[]) value);
      }
    }
    else {
      return value.toString();
    }
  }

}
