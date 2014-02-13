package org.pojomatic.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Type {
  BOOLEAN(boolean.class, false, true),
  BYTE(byte.class, (byte) 0, (byte) 1, (byte) -1),
  CHAR(char.class, 'a', (char) 0, (char) 12345),
  SHORT(short.class, (short) 0, (short) -1, (short) 1),
  INT(int.class, -12314, 0, 2352362),
  LONG(long.class, 0L, -23413513515L, 2L << 16 + 5L),
  FLOAT(float.class, 0f, 123151f, -151f),
  DOUBLE(double.class, 0.0, -141.2, 12351351.26),
  STRING(String.class, null, "", "hello"),
  ;

  @SafeVarargs
  private <T> Type(Class<T> clazz, T... values) {
    this.clazz = clazz;
    this.sampleValues = Collections.unmodifiableList(Arrays.<Object>asList(values));
  }

  public final Class<?> clazz;
  public final List<Object> sampleValues;

}
