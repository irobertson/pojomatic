package org.pojomatic.junit;

import org.pojomatic.test.AssertTest;

public class PojomaticAssertTest extends AssertTest {

  @Override
  protected void performAssertEquals(Object first, Object second) {
    PojomaticAssert.assertEqualsWithDiff(first, second);
  }

}
