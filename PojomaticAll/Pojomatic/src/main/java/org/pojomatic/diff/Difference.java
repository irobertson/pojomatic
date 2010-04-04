package org.pojomatic.diff;

import java.util.NoSuchElementException;

/**
 * A difference between two objects, 'left' and 'right'.
 */
public interface Difference {

  /**
   * The name of the property.
   *
   * @return the name of the property
   */
  String propertyName();

  /**
   * The value from the left instance (possibly {@code null}).
   *
   * @return the value from the left instance (possibly {@code null})
   * @throws NoSuchElementException if the value does not exist on the left instance
   * @see #existsOnLeft()
   */
  Object leftValue() throws NoSuchElementException;

  /**
   * The value from the right instance (possibly {@code null}).
   *
   * @return the value from the right instance (possibly {@code null})
   * @throws NoSuchElementException if the value does not exist on the right instance
   * @see #existsOnRight()
   */
  Object rightValue() throws NoSuchElementException;

}
