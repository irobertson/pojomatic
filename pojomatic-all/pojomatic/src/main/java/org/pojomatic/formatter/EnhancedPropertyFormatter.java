package org.pojomatic.formatter;

@SuppressWarnings("deprecation")
public interface EnhancedPropertyFormatter extends PropertyFormatter {
  void formatTo(StringBuilder builder, Object o);
  void formatTo(StringBuilder builder, boolean b);
  void formatTo(StringBuilder builder, byte b);
  void formatTo(StringBuilder builder, short s);
  void formatTo(StringBuilder builder, char c);
  void formatTo(StringBuilder builder, int i);
  void formatTo(StringBuilder builder, long l);
  void formatTo(StringBuilder builder, float f);
  void formatTo(StringBuilder builder, double d);
  void formatTo(StringBuilder builder, boolean[] booleans);
  void formatTo(StringBuilder builder, byte[] bytes);
  void formatTo(StringBuilder builder, short[] shorts);
  void formatTo(StringBuilder builder, char[] chars);
  void formatTo(StringBuilder builder, int[] ints);
  void formatTo(StringBuilder builder, long[] longs);
  void formatTo(StringBuilder builder, float[] floats);
  void formatTo(StringBuilder builder, double[] doubles);
}
