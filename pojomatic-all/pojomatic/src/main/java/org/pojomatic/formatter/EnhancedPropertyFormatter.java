package org.pojomatic.formatter;

@SuppressWarnings("deprecation")
public interface EnhancedPropertyFormatter extends PropertyFormatter {
  void appendFormatted(StringBuilder builder, Object o);
  void appendFormatted(StringBuilder builder, boolean b);
  void appendFormatted(StringBuilder builder, byte b);
  void appendFormatted(StringBuilder builder, short s);
  void appendFormatted(StringBuilder builder, char c);
  void appendFormatted(StringBuilder builder, int i);
  void appendFormatted(StringBuilder builder, long l);
  void appendFormatted(StringBuilder builder, float f);
  void appendFormatted(StringBuilder builder, double d);
  void appendFormatted(StringBuilder builder, boolean[] booleans);
  void appendFormatted(StringBuilder builder, byte[] bytes);
  void appendFormatted(StringBuilder builder, short[] shorts);
  void appendFormatted(StringBuilder builder, char[] chars);
  void appendFormatted(StringBuilder builder, int[] ints);
  void appendFormatted(StringBuilder builder, long[] longs);
  void appendFormatted(StringBuilder builder, float[] floats);
  void appendFormatted(StringBuilder builder, double[] doubles);
}
