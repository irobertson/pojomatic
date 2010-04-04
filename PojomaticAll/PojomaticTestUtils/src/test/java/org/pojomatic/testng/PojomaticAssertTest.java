package org.pojomatic.testng;

import org.pojomatic.test.AssertTest;

public class PojomaticAssertTest extends AssertTest {

  @Override
  protected void performAssertEquals(Object expected, Object actual, String message) {
    //in TestNG, the arguments are included in any failure message in reverse order
    PojomaticAssert.assertEqualsWithDiff(actual, expected, message);
  }

}
