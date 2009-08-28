package org.pojomatic.junit;

import org.pojomatic.Pojomatic;
import org.pojomatic.test.AssertUtils;

/**
 * Pojomatic-related JUnit-style assertion methods useful for writing tests.
 * @see org.junit.Assert
 */
public class PojomaticAssert {

  /**
   * Asserts that two possibly {@code null} objects are equal according to
   * {@link Object#equals(Object)}. If not, the differences,
   * via {@link Pojomatic#diff(Object, Object)}, are included in the failure message.
   *
   * @param expected the expected object
   * @param actual the object which should be tested to equal the expected object
   * @throws AssertionError if the objects are not equal, with details of the differences
   * included in the message
   * @see #assertEqualsWithDiff(Object, Object, String)
   */
  public static void assertEqualsWithDiff(Object expected, Object actual) {
    assertEqualsWithDiff(expected, actual, null);
  }

  /**
   * Asserts that two possibly {@code null} objects are equal according to
   * {@link Object#equals(Object)}. If not, the differences,
   * via {@link Pojomatic#diff(Object, Object)}, are included in the failure message.
   *
   * @param expected the expected object
   * @param actual the object which should be tested to equal the expected object
   * @throws AssertionError if the objects are not equal, with details of the differences
   * included in the message
   */
  public static void assertEqualsWithDiff(Object expected, Object actual, String message) {
    AssertUtils.assertEquals(message, expected, actual);
  }

  private PojomaticAssert() {}
}
