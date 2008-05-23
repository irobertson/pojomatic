package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;

/**
 * A formatter for a property.
 * Any implementation of {@code PropertyFormatter} must have a public no-argument constructor.
 */
public interface PropertyFormatter {
  /**
   * Initialize the formatter for use; this method will be called exactly once on an instance, prior
   * to any calls to {@link #format(Object)}.  This method does not need to be
   * thread-safe.
   * @param element
   */
  public void initialize(AnnotatedElement element);

  /**
   * Format a given value.  This method must be thread safe.
   * @param value the value to format
   * @return the value, formatted (must not be null)
   */
  public String format(Object value);
}
