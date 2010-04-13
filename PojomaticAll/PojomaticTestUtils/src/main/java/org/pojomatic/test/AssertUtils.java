package org.pojomatic.test;

import org.pojomatic.Pojomatic;
import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.diff.Differences;

/**
 * This class is not meant to be a part of the public API.
 */
public class AssertUtils {

  /**
   * Determines if two objects are both null or are equal according to
   * {@link Object#equals(Object)}.
   *
   * @return {@code true} if both objects are null,
   * or {@code first} is non-null and {@code first.equals(second)},
   * {@code false} otherwise
   * @see Object#equals(Object)
   */
  public static boolean equal(Object first, Object second) {
    return first == null ? second == null : first.equals(second);
  }

  /**
   * Asserts that two objects are either both null or are equal according to
   * {@link Object#equals(Object)}. If not, an {@code AssertionError} is thrown. If the objects are
   * not equal, but the types of two objects are compatible for equality, then the differences as
   * determined by {@link Pojomatic#diff(Object, Object)} are included in the failure message.
   *
   * @param message the message to add if the assertion fails
   * @param expected will be displayed first if the assertion fails
   * @param actual will be displayed second if the assertion fails
   * @throws AssertionError if the objects are not equal. {@link AssertionError#getMessage()} will
   * include information about the differences
   */
  public static void assertEquals(String message, Object expected, Object actual) {
    if (!equal(expected, actual)) {
      if (expected == null) {
        throw new AssertionError(
          makeBuilder(message).append("expected is null, but actual is ").append(actual));
      }
      if (actual == null) {
        throw new AssertionError(
          makeBuilder(message).append("actual is null, but expected is ").append(expected));
      }
      try {
        if (Pojomatic.areCompatibleForEquals(expected.getClass(), actual.getClass())) {
          throw new AssertionError(appendStandardEqualityMessage(
            makeBuilder(message).append("differences between expected and actual:")
            .append(Pojomatic.diff(expected, actual))
              .append(" ("), expected, actual).append(")").toString());
        }
      }
      catch (NoPojomaticPropertiesException e) {}
      throw new AssertionError(
        appendStandardEqualityMessage(makeBuilder(message), expected, actual).toString());
      }
  }

  private static StringBuilder appendStandardEqualityMessage(
    StringBuilder builder, Object expected, Object actual) {
    return builder
      .append("expected:<").append(expected).append("> but was:<").append(actual).append(">");
  }

  private static StringBuilder makeBuilder(String message) {
    return message == null ? new StringBuilder() : new StringBuilder(message).append(" ");
  }

  private AssertUtils() {}

}
