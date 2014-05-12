package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.pojomatic.annotations.SkipArrayCheck;

/**
 * The default property formatter used by Pojomatic.  While the particulars of the formatting
 * strategy are subject to change, the general principle is to provide a meaningful representation.
 * In particular, arrays are formatted "deeply", rather than simply showing the default toString
 * representation of Java arrays.
 */
public class DefaultEnhancedPropertyFormatter implements EnhancedPropertyFormatter {
  private boolean checkForDeepArray;

  /**
   * {@inheritDoc}
   *
   * This implementation checks to see if the element has been annotated with {@link DeepArray}. Overrides of this
   * method should call {@code super.initialize(element)}.
   */
  @Override
  public void initialize(AnnotatedElement element) {
    Class<?> type = getType(element);
    checkForDeepArray = isDeepArray(element, type);
  }

  private boolean isDeepArray(AnnotatedElement element, Class<?> type) {
    if (type.equals(Object.class)) {
      return ! element.isAnnotationPresent(SkipArrayCheck.class);
    }
    else if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      return componentType.equals(Object.class) || componentType.isArray();
    }
    else {
      return false;
    }
  }

  private static Class<?> getType(AnnotatedElement element) {
    if (element instanceof Field) {
      return ((Field) element).getType();
    }
    else if (element instanceof Method) {
      return ((Method) element).getReturnType();
    }
    else throw new IllegalArgumentException("Annotated element has type " + element.getClass().getName());
  }

  @Override
  final public String format(Object value) {
    StringBuilder builder = new StringBuilder();
    appendFormatted(builder, value);
    return builder.toString();
  }

  @Override
  public void appendFormatted(StringBuilder builder, Object value) {
    builder.append(value);
  }

  @Override
  public void appendFormattedPossibleArray(StringBuilder builder, Object value) {
    if (value == null) {
      builder.append("null");
    }
    else if (value.getClass().isArray()) {
      Class<?> componentClass = value.getClass().getComponentType();
      if (componentClass.isPrimitive()) {
        if (Boolean.TYPE == componentClass) {
          appendFormatted(builder, (boolean[]) value);
        }
        else if (Character.TYPE == componentClass) {
          appendFormatted(builder, (char[]) value);
        }
        else if (Byte.TYPE == componentClass) {
          appendFormatted(builder, (byte[]) value);
        }
        else if (Short.TYPE == componentClass) {
          appendFormatted(builder, (short[]) value);
        }
        else if (Integer.TYPE == componentClass) {
          appendFormatted(builder, (int[]) value);
        }
        else if (Long.TYPE == componentClass) {
          appendFormatted(builder, (long[]) value);
        }
        else if (Float.TYPE == componentClass) {
          appendFormatted(builder, (float[]) value);
        }
        else if (Double.TYPE == componentClass) {
          appendFormatted(builder, (double[]) value);
        }
        else {
          throw new IllegalStateException("unexpected primitive array base type: " + componentClass);
        }
      }
      else {
        appendFormatted(builder, (Object[]) value);
      }
    }
    else {
      appendFormatted(builder, value);
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, Object[] array) {
    if (checkForDeepArray) {
      appendFormattedDeep(builder, array, new HashSet<>());
    }
    else {
      appendFormattedShallow(builder, array);
    }
  }

  private void appendFormattedShallow(StringBuilder builder, Object[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  private void appendFormattedDeep(StringBuilder builder, Object[] array, Set<Object> dejaVu) {
    if (array == null) {
      builder.append("null");
    }
    else if (! dejaVu.add(array)) {
      builder.append("[...]");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        Object element = array[i];
        if (element != null && element.getClass().isArray()) {
          Class<?> componentType = element.getClass().getComponentType();
          if (componentType.isPrimitive()) {
            if (componentType == boolean.class) {
              appendFormatted(builder, (boolean[]) element);
            }
            else if (componentType == byte.class) {
              appendFormatted(builder, (byte[]) element);
            }
            else if (componentType == short.class) {
              appendFormatted(builder, (short[]) element);
            }
            else if (componentType == char.class) {
              appendFormatted(builder, (char[]) element);
            }
            else if (componentType == int.class) {
              appendFormatted(builder, (int[]) element);
            }
            else if (componentType == long.class) {
              appendFormatted(builder, (long[]) element);
            }
            else if (componentType == float.class) {
              appendFormatted(builder, (float[]) element);
            }
            else if (componentType == double.class) {
              appendFormatted(builder, (double[]) element);
            }
            else {
              throw new IllegalArgumentException("Unexpected primitive type " + componentType.getName());
            }
          }
          else {
            appendFormattedDeep(builder, (Object[]) element, dejaVu);
          }
        }
        else {
          appendFormatted(builder, element);
        }
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, boolean b) {
    builder.append(b);
  }

  @Override
  public void appendFormatted(StringBuilder builder, byte b) {
    builder.append(b);
  }

  @Override
  public void appendFormatted(StringBuilder builder, short s) {
    builder.append(s);
  }

  @Override
  public void appendFormatted(StringBuilder builder, char c) {
    builder.append('\'');
    if (Character.isISOControl(c)) {
      builder.append("\\u").append(String.format("%04x", (int)c));
    }
    else {
      builder.append(c);
    }
    builder.append('\'');
  }

  @Override
  public void appendFormatted(StringBuilder builder, int i) {
    builder.append(i);
  }

  @Override
  public void appendFormatted(StringBuilder builder, long l) {
    builder.append(l);
  }

  @Override
  public void appendFormatted(StringBuilder builder, float f) {
    builder.append(f);
  }

  @Override
  public void appendFormatted(StringBuilder builder, double d) {
    builder.append(d);
  }

  @Override
  public void appendFormatted(StringBuilder builder, boolean[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, byte[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, short[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, char[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, int[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, long[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, float[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, double[] array) {
    if (array == null) {
      builder.append("null");
    }
    else {
      builder.append('[');
      int iMax = array.length - 1;
      for (int i = 0; i <= iMax; i++) {
        appendFormatted(builder, array[i]);
        if (i != iMax) {
          builder.append(", ");
        }
      }
      builder.append(']');
    }
  }
}
