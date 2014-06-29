package org.pojomatic.junit;

import org.pojomatic.Pojomatic;
import org.pojomatic.test.AssertUtils;

/**
 * Pojomatic-related JUnit-style assertion methods useful for writing tests.
 * @see org.junit.Assert
 */
public class PojomaticAssert {

  /**
   * Asserts that two objects are either both null or are equal according to
   * {@link Object#equals(Object)}. If not, an {@code AssertionError} is thrown. If the objects are
   * not equal, but the types of two objects are compatible for equality, then the differences as
   * determined by {@link Pojomatic#diff(Object, Object)} are included in the failure message.
   *
   * @param expected the expected object
   * @param actual the object which should be tested to equal the expected object
   * @throws AssertionError if the objects are not equal.
   * @see #assertEqualsWithDiff(String, Object, Object)
   */
  public static void assertEqualsWithDiff(Object expected, Object actual) {
    assertEqualsWithDiff(null, expected, actual);
  }

  /**
   * Asserts that two objects are either both null or are equal according to
   * {@link Object#equals(Object)}. If not, an {@code AssertionError} is thrown. If the objects are
   * not equal, but the types of two objects are compatible for equality, then the differences as
   * determined by {@link Pojomatic#diff(Object, Object)} are included in the failure message.
   * @param message a message (possibly {@code null}) to include at the beginning of the
   *   {@code AssertionError} message.
   * @param expected the expected object
   * @param actual the object which should be tested to equal the expected object
   *
   * @throws AssertionError if the objects are not equal.
   */
  public static void assertEqualsWithDiff(String message, Object expected, Object actual) {
    AssertUtils.assertEquals(message, expected, actual);
  }

  private PojomaticAssert() {}
}
