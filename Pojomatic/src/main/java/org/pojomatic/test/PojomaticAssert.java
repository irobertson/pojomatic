package org.pojomatic.test;

import org.pojomatic.Pojomatic;
import org.pojomatic.diff.Differences;

public class PojomaticAssert {
  private PojomaticAssert() {}

  public static void assertPojomaticEquals(Object expected, Object actual) {
    assertPojomaticEquals(null, expected, actual);
  }

  public static void assertPojomaticEquals(String message, Object expected, Object actual) {
    //TODO test this
    //TODO verify the error messages are similar to JUnit
    if (expected == null) {
      if (actual != null) {
        StringBuilder failureMessage = createFailureMessage(message);
        failureMessage.append("expected null, but was: ").append(actual);
        throw new AssertionError(failureMessage.toString());
      }
    }
    else { //expected is not null
      if (!expected.equals(actual)) {
        Differences diff = Pojomatic.diff(expected, actual);
        if (!diff.areEqual()) { //TODO rename this method to diff.hasDifferences()?
          StringBuilder failureMessage = createFailureMessage(message)
            .append("found differences: ")
            .append(diff)
            .append("; expected: ")
            .append(expected)
            .append(", but was: ")
            .append(actual);
          throw new AssertionError(failureMessage.toString());
        }
        else { //no differences found, but objects are not equal
          //TODO different message for this case
        }
      }
    }
  }

  private static StringBuilder createFailureMessage(String message) {
    StringBuilder failureMessage = new StringBuilder();
    if (message != null && message.length() > 0) {
      failureMessage.append(message).append(' ');
    }
    return failureMessage;
  }

}
