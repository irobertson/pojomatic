package org.pojomatic.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public enum BaseType implements Type {
  BOOLEAN(boolean.class, false, true),
  BYTE(byte.class, (byte) 0, (byte) 1, (byte) -1),
  CHAR(char.class, 'a', '\u0000', '\u009a', '\u1234') {
    @Override
    public String toString(Object value) {
      switch ((char) value) {
        case 'a': return "'a'";
        case '\u0000': return "'\\u0000'";
        case '\u009a': return "'\\u009a'";
        case '\u1234': return "'" + '\u1234' + "'";
        default:
          throw new IllegalArgumentException("unexpected character " + value);
      }
    }
  },
  SHORT(short.class, (short) 0, (short) -1, (short) 1),
  INT(int.class, -12314, 0, 2352362),
  LONG(long.class, 0L, -23413513515L, 2L << 16 + 5L),
  FLOAT(float.class, 0f, 123151f, -151f),
  DOUBLE(double.class, 0.0, -141.2, 12351351.26),
  OBJECT(Object.class, null, "", "hello", 29),
  ;

  private final Class<?> clazz;
  private final List<Object> sampleValues;

  @SafeVarargs
  private <T> BaseType(Class<T> clazz, T... values) {
    this.clazz = clazz;
    this.sampleValues = Collections.unmodifiableList(Arrays.<Object>asList(values));
  }

  @Override
  public Class<?> getClazz() {
    return clazz;
  }

  @Override
  public List<Object> getSampleValues() {
    return sampleValues;
  }

  @Override
  public int hashCode(Object value) {
    return Objects.hashCode(value);
  }

  @Override
  public int deepHashCode(Object value) {
    return hashCode(value);
  }

  @Override
  public String toString(Object value) {
    return Objects.toString(value);
  }

  @Override
  public String deepToString(Object value) {
    return toString(value);
  }

  @Override
  public int arrayDepth() {
    return 0;
  }

}
