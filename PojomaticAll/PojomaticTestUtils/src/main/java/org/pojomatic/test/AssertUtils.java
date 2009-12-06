package org.pojomatic.test;

import org.pojomatic.Pojomatic;
import org.pojomatic.diff.Differences;

/**
 * This class is not meant to be a part of the public API.
 */
public class AssertUtils {

  /**
   * Determines if two possibly {@code null} objects are equal.
   *
   * @return {@code true} if both objects are null,
   * or {@code first} is non-null and {@code first.equals(second)},
   * {@code false} otherwise
   * @see Object#equals(Object)
   */
  public static boolean equal(Object first, Object second) {
    if (first == null && second == null) {
      return true;
    }
    if (first != null && first.equals(second)) {
      return true;
    }
    return false;
  }

  /**
   * Asserts that the objects are equal via {@link #equal(Object, Object)}.
   *
   * @param message the message to add if the assertion fails
   * @param first will be displayed first if the assertion fails
   * @param second will be displayed second if the assertion fails
   * @throws AssertionError if the objects are not equal. {@link AssertionError#getMessage()} will
   * include information about the differences
   */
  public static void assertEquals(String message, Object first, Object second) {
    if (!equal(first, second)) {
      throw new AssertionError(buildMessage(message, Pojomatic.diff(first, second)));
    }
  }

  private static String buildMessage(String message, Differences differences) {
    StringBuilder formatted = new StringBuilder();
    if (message != null) {
      formatted.append(message).append(" ");
    }

    return formatted.append(differences).toString();
  }

  private AssertUtils() {}

}
