package org.pojomatic.diff;

/**
 * A summary of differences (if any) between two POJOs.
 */
public interface Differences {

  /**
   * The differences between the two objects. If there are no differences, an
   * empty {@link Iterable}.
   *
   * @return the differences between the two objects, or an empty {@link Iterable} if there are none
   */
  Iterable<? extends Difference> differences();

  /**
   * @return {@code true} if the two POJOs were {@code equal} to each other;
   * {@code false} otherwise.
   */
  boolean areEqual();

  /**
   * @return a description of the differences
   */
  @Override
  String toString();
}
