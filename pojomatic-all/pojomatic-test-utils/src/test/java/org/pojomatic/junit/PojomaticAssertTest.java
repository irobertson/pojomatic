package org.pojomatic.junit;

import org.pojomatic.test.AssertTest;

public class PojomaticAssertTest extends AssertTest {
  @Override
  protected void performAssertEquals(Object expected, Object actual) {
    PojomaticAssert.assertEqualsWithDiff(expected, actual);
  }

  @Override
  protected void performAssertEquals(Object expected, Object actual, String message) {
    PojomaticAssert.assertEqualsWithDiff(message, expected, actual);
  }


}
