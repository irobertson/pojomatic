package org.pojomatic.diff;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;

public class ValueDifferenceTest {
  private ValueDifference DIFFERENCE = new ValueDifference("foo", "this", "that");

  @Test
  public void testPropertyName() {
    AssertJUnit.assertEquals("foo", DIFFERENCE.propertyName());
  }

  @Test
  public void testLeftValue() {
    AssertJUnit.assertEquals("this", DIFFERENCE.leftValue());
  }

  @Test
  public void testRightValue() {
    AssertJUnit.assertEquals("that", DIFFERENCE.rightValue());
  }
}
