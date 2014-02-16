package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;

/**
 * A formatter for a property.
 * <p>
 * Any implementation of {@code EnhancedPropertyFormatter} must have a public no-argument constructor.
 * @see DefaultEnhancedPropertyFormatter
 */
@SuppressWarnings("deprecation")
public interface EnhancedPropertyFormatter extends PropertyFormatter {
  @Override // avoid deprecation warning
  void initialize(AnnotatedElement element);

  /**
   * Format an object; no attempt will be made to format it as an array.
   * @param builder the builder to append the formatted representation of the object to
   * @param o the object to format
   */
  void appendFormatted(StringBuilder builder, Object o);

  /**
   * Format a boolean.
   * @param builder the builder to append the formatted representation of the boolean to
   * @param b the boolean to format
   */
  void appendFormatted(StringBuilder builder, boolean b);

  /**
   * Format a byte
   * @param builder the builder to append the formatted representation of the byte to
   * @param b the byte to format
   */
  void appendFormatted(StringBuilder builder, byte b);

  /**
   * Format a short
   * @param builder the builder to append the formatted representation of the short to
   * @param s the short to format
   */
  void appendFormatted(StringBuilder builder, short s);

  /**
   * Format a character
   * @param builder the builder to append the formatted representation of the character to
   * @param c the character to format
   */
  void appendFormatted(StringBuilder builder, char c);

  /**
   * Format an integer
   * @param builder the builder to append the formatted representation of the integer to
   * @param i the integer to format
   */
  void appendFormatted(StringBuilder builder, int i);

  /**
   * Format a long
   * @param builder the builder to append the formatted representation of the long to
   * @param l the long to format
   */
  void appendFormatted(StringBuilder builder, long l);

  /**
   * Format a float
   * @param builder the builder to append the formatted representation of the float to
   * @param f the float to format
   */
  void appendFormatted(StringBuilder builder, float f);

  /**
   * Format a double
   * @param builder the builder to append the formatted representation of the double to
   * @param d the double to format
   */
  void appendFormatted(StringBuilder builder, double d);

  /**
   * Format an array of booleans
   * @param builder the builder to append the formatted representation of the array to
   * @param booleans the array to format
   */
  void appendFormatted(StringBuilder builder, boolean[] booleans);

  /**
   * Format an array of bytes
   * @param builder the builder to append the formatted representation of the array to
   * @param bytes the array to format
   */
  void appendFormatted(StringBuilder builder, byte[] bytes);

  /**
   * Format an array of shorts
   * @param builder the builder to append the formatted representation of the array to
   * @param shorts the array to format
   */
  void appendFormatted(StringBuilder builder, short[] shorts);

  /**
   * Format an array of characters
   * @param builder the builder to append the formatted representation of the array to
   * @param chars the array to format
   */
  void appendFormatted(StringBuilder builder, char[] chars);

  /**
   * Format an array of integers
   * @param builder the builder to append the formatted representation of the array to
   * @param ints the array to format
   */
  void appendFormatted(StringBuilder builder, int[] ints);

  /**
   * Format an array of longs
   * @param builder the builder to append the formatted representation of the array to
   * @param longs the array to format
   */
  void appendFormatted(StringBuilder builder, long[] longs);

  /**
   * Format an array of floats
   * @param builder the builder to append the formatted representation of the array to
   * @param floats the array to format
   */
  void appendFormatted(StringBuilder builder, float[] floats);

  /**
   * Format an array of doubles
   * @param builder the builder to append the formatted representation of the array to
   * @param doubles the array to format
   */
  void appendFormatted(StringBuilder builder, double[] doubles);

  /**
   * Format an array of Objects
   * @param builder the builder to append the formatted representation of the array to
   * @param objects the array to format
   */
  void appendFormatted(StringBuilder builder, Object[] objects);

  /**
   * Format an object, which may be an array.
   * @param builder the builder to append the formatted representation of the object to
   * @param o the object to format
   */
  void appendFormattedPossibleArray(StringBuilder builder, Object o);
}
