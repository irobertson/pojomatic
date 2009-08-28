package org.pojomatic.diff;

/**
 * A summary of differences (if any) between two POJOs.
 */
public interface Differences {
  /**
   * @return a description of the differences
   */
  @Override
  String toString();

  /**
   * @return {@code true} if the two POJOs were {@code equal} to each other;
   * {@code false} otherwise.
   */
  boolean areEqual();
}
