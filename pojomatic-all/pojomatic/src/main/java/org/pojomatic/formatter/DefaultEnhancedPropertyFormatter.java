package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.pojomatic.annotations.CanBeArray;
import org.pojomatic.annotations.DeepArray;

/**
 * The default property formatter used by Pojomatic.  While the particulars of the formatting
 * strategy are subject to change, the general principle is to provide a meaningful representation.
 * In particular, arrays are formatted "deeply", rather than simply showing the default toString
 * representation of Java arrays.
 */
public class DefaultEnhancedPropertyFormatter implements EnhancedPropertyFormatter {
  private boolean isDeepArray;
  private boolean canBeArray;

  @Override
  public void initialize(AnnotatedElement element) {
    isDeepArray = element.isAnnotationPresent(DeepArray.class);
    canBeArray = element.isAnnotationPresent(CanBeArray.class);
  }

  @Override
  public String format(Object value) {
    StringBuilder builder = new StringBuilder();
    appendFormatted(builder, value);
    return builder.toString();
  }

  @Override
  public void appendFormatted(StringBuilder builder, Object value) {
    if (value == null) {
      builder.append("null");
    }
    else if (canBeArray && value.getClass().isArray()) {
      // FIXME - avoid allocating new builders
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
      builder.append(value);
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, Object[] array) {
    if (isDeepArray) {
      builder.append( Arrays.deepToString(array));
    }
    else {
      builder.append( Arrays.toString(array));
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
  public void appendFormatted(StringBuilder builder, boolean[] booleans) {
    builder.append(Arrays.toString(booleans));
  }

  @Override
  public void appendFormatted(StringBuilder builder, byte[] bytes) {
    builder.append(Arrays.toString(bytes));
  }

  @Override
  public void appendFormatted(StringBuilder builder, short[] shorts) {
    builder.append(Arrays.toString(shorts));
  }

  @Override
  public void appendFormatted(StringBuilder builder, char[] chars) {
    if (chars == null) {
      builder.append("null");
    }
    else {
      builder.append("[");
      boolean seenOne = false;
      for (char c: chars) {
        if(seenOne) {
          builder.append(", ");
        }
        else {
          seenOne = true;
        }
        appendFormatted(builder, c);
      }
      builder.append(']');
    }
  }

  @Override
  public void appendFormatted(StringBuilder builder, int[] ints) {
    builder.append(Arrays.toString(ints));
  }

  @Override
  public void appendFormatted(StringBuilder builder, long[] longs) {
    builder.append(Arrays.toString(longs));
  }

  @Override
  public void appendFormatted(StringBuilder builder, float[] floats) {
    builder.append(Arrays.toString(floats));
  }

  @Override
  public void appendFormatted(StringBuilder builder, double[] doubles) {
    builder.append(Arrays.toString(doubles));
  }
}
