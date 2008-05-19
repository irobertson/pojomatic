package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;

/**
 * A formatter for a property.
 * Any implementation of {@code PropertyFormatter} must have a public no-argument constructor.
 */
public interface PropertyFormatter {
  /**
   * Initialize the formatter for use; this method will be called exactly once on an instance, prior
   * to any calls to {@link #format(Object, Appendable)}.  This method does not need to be
   * thread-safe.
   * @param element
   */
  public void initialize(AnnotatedElement element);

  /**
   * Format a given value.  This method must be thread safe.
   * @param value the value to format
   * @param appendable where to format the value to
   */
  public void format(Object value, Appendable appendable);
}
