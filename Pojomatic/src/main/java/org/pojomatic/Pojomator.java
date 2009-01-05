package org.pojomatic;

import java.util.List;

import org.pojomatic.diff.Difference;

/**
 * An automatically generated provider of the three standard {@code Object} methods,
 * {@link Object#equals(Object)}, {@link Object#hashCode()} and {@link Object#toString()}.
 *
 * @param <T> the class this {@code Pojomator} is generated for.
 */
public interface Pojomator<T> {

  /**
   * Compute the hashCode for a given instance of {@code T}.
   *
   * @param instance the instance to compute the hashCode for - must not be {@code null}
   * @return the hashCode of {@code instance}
   * @see Object#hashCode()
   */
  int doHashCode(T instance);

  /**
   * Compute the {@code toString} representation for a given instance of {@code T}
   *
   * @param instance the instance to compute the {@code toString} representation for - must not be {@code null}
   * @return the {@code toString} representation of {@code instance}
   * @see Object#toString()
   */
  String doToString(T instance);

  /**
   * Compute whether {@code instance} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@link Object#equals(Object) equals} method.
   *
   * @param instance the instance to test against - must not be {@code null}
   * @param other the instance to test
   * @return {@code true} if {@code instance} should be considered equal to {@code other}, and
   *         {@code false} otherwise.
   * @see Object#equals(Object)
   */
  boolean doEquals(T instance, Object other);

  /**
   * Compute the differences between {@code instance} and {@code other} among the properties
   * examined by {@link #doEquals(Object, Object)}.
   *
   * @param instance the instance to diff against - must not be {@code null}
   * @param other the instance to diff
   * @return the list of differences (possibly empty) between {@code instance} and {@code other}
   * among the properties examined by {@link #doEquals(Object, Object)}.
   */
  List<Difference> doDiff(T instance, T other);
}
