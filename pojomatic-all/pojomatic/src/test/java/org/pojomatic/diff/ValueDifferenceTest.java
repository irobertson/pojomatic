package org.pojomatic.diff;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class ValueDifferenceTest {
  private ValueDifference DIFFERENCE = new ValueDifference("foo", "this", "that");

  @Test
  public void testPropertyName() {
    assertEquals(DIFFERENCE.propertyName(), "foo");
  }

  @Test
  public void testLeftValue() {
    assertEquals(DIFFERENCE.leftValue(), "this");
  }

  @Test
  public void testRightValue() {
    assertEquals(DIFFERENCE.rightValue(), "that");
  }
}
