package org.pojomatic.testng;

import org.pojomatic.test.AssertTest;

public class PojomaticAssertTest extends AssertTest {

  @Override
  protected void performAssertEquals(Object first, Object second, String message) {
    //in TestNG, the arguments are included in any failure message in reverse order
    PojomaticAssert.assertEqualsWithDiff(second, first, message);
  }

}
