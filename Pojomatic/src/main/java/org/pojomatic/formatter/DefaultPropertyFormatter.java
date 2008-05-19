package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

public class DefaultPropertyFormatter implements PropertyFormatter {
  public void initialize(AnnotatedElement element) {
    // TODO Auto-generated method stub
  }


  public String format(Object value) {
    if (value == null) {
      return "null";
    }
    else if (value.getClass().isArray()) {
      Class<?> baseClass = value.getClass().getComponentType();
      if (baseClass.isPrimitive()) {
        /*
         *      *
     * @see     java.lang.Boolean#TYPE
     * @see     java.lang.Character#TYPE
     * @see     java.lang.Byte#TYPE
     * @see     java.lang.Short#TYPE
     * @see     java.lang.Integer#TYPE
     * @see     java.lang.Long#TYPE
     * @see     java.lang.Float#TYPE
     * @see     java.lang.Double#TYPE
     * @see     java.lang.Void#TYPE

         */
        if (Boolean.TYPE == baseClass) {
          return Arrays.toString((boolean[]) value);
        }
        if (Character.TYPE == baseClass) {
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
        if (Byte.TYPE == baseClass) {
          return Arrays.toString((byte[]) value);
        }
        if (Short.TYPE == baseClass) {
          return Arrays.toString((short[]) value);
        }
        if (Integer.TYPE == baseClass) {
          return Arrays.toString((int[]) value);
        }
        if (Long.TYPE == baseClass) {
          return Arrays.toString((long[]) value);
        }
        if (Float.TYPE == baseClass) {
          return Arrays.toString((float[]) value);
        }
        if (Double.TYPE == baseClass) {
          return Arrays.toString((double[]) value);
        }
        else {
          throw new IllegalStateException("unexpected primative array base type: " + baseClass);
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
