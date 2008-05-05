package org.pojomatic.annotations;

import org.pojomatic.Pojomatic;

/**
 * Defines which sets of {@link Pojomatic} operations that properties should be included.
 * This are set class-wide using {@link AutoProperty} and for
 * an individual property using {@link Property}.
 */
public enum PojomaticDefaultPolicy {

  /**
   * Anything included in {@code public int hashCode()} should also be included in
   * {@code public boolean equals(Object)} to preserve the general
   * contract of {@link Object#hashCode()}.
   *
   * @see Object#hashCode()
   * @see Object#equals(Object)
   */
  HASHCODE_EQUALS,

  /**
   * {@code public boolean equals(Object)}
   *
   * @see Object#equals(Object)
   */
  EQUALS,

  /**
   * {@code public String toString()}
   *
   * @see Object#toString()
   */
  TO_STRING,

  /**
   * Shorthand for all of the above.
   */
  ALL,

  /**
   * Shorthand for none of the above.
   */
  NONE;
}
