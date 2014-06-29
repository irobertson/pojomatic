package org.pojomatic.testng;

import org.pojomatic.Pojomatic;
import org.pojomatic.test.AssertUtils;

/**
 * Pojomatic-related TestNG-style assertion methods useful for writing tests.
 * @see org.testng.Assert
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
   * @throws AssertionError if the objects are not equal, with details of the differences
   * included in the message
   * @see #assertEqualsWithDiff(Object, Object, String)
   */
  public static void assertEqualsWithDiff(Object actual, Object expected) {
    assertEqualsWithDiff(actual, expected, null);
  }

  /**
   * Asserts that two objects are either both null or are equal according to
   * {@link Object#equals(Object)}. If not, an {@code AssertionError} is thrown. If the objects are
   * not equal, but the types of two objects are compatible for equality, then the differences as
   * determined by {@link Pojomatic#diff(Object, Object)} are included in the failure message.
   *
   * @param expected the expected object
   * @param actual the object which should be tested to equal the expected object
   * @param message an optional message provided along with the diff if the objects are not equal
   * @throws AssertionError if the objects are not equal, with details of the differences
   * included in the message
   */
  public static void assertEqualsWithDiff(Object actual, Object expected, String message) {
    //the arguments are passed as follows according to display order for a potential error message
    AssertUtils.assertEquals(message, expected, actual);
  }

  private PojomaticAssert() {}
}
