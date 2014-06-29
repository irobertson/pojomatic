package org.pojomatic.formatter;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;

/**
 * A formatter to aid in creating a {@code String} representation of a POJO.
 *
 * Any implementation of {@code EnhancedPojoFormatter} must have a public no-argument constructor. A new instance will
 * be created for each time that {@link Pojomator#doToString(Object)} is called. Consequently, implementations do
 * <em>not</em> need to be thread safe.
 *
 * @since 2.0
 * @see DefaultEnhancedPojoFormatter
 */
@SuppressWarnings("deprecation")
public interface EnhancedPojoFormatter extends PojoFormatter {
  /**
   * Append the {@code String} which should appear at the beginning of the result of
   * {@code toString()} to the supplied StringBuilder.
   *
   * @param builder the builder to append to.
   * @param pojoClass the class for which {@code toString()} is being called
   * @see Object#toString()
   */
  void appendToStringPrefix(StringBuilder builder, Class<?> pojoClass);

  /**
   * Append the {@code String} which should appear at the end of the result of
   * {@code toString()} to the supplied StringBuilder.
   *
   * @param builder the builder to append to.
   * @param pojoClass the class for which {@code toString()} is being called
   * @see Object#toString()
   */
  void appendToStringSuffix(StringBuilder builder, Class<?> pojoClass);

  /**
   * Append the {@code String} prefix for a given {@code PropertyElement} to the supplied
   * StringBuilder. This method will be called once for each property used in the result of
   * {@code toString()}, in the order in which those properties will appear in that result,
   * and before the call to {@link PropertyFormatter#format(Object)} for the property's value.
   *
   * @param builder the builder to append to.
   * @param property the property for which to generate a prefix
   */
  void appendPropertyPrefix(StringBuilder builder, PropertyElement property);

  /**
   * Append the {@code String} suffix for a given {@code PropertyElement} to the supplied
   * StringBuilder. This method will be called once after each call to
   * {@link PropertyFormatter#format(Object)} for the property's value.
   *
   * @param builder the builder to append to.
   * @param property the property for which to generate a suffix
   */
  void appendPropertySuffix(StringBuilder builder, PropertyElement property);
}
