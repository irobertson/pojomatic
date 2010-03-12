package org.pojomatic.diff;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValueDifferenceTest {
  private ValueDifference DIFFERENCE = new ValueDifference("foo", "this", "that");

  @Test
  public void testPropertyName() {
    assertEquals("foo", DIFFERENCE.propertyName());
  }

  @Test
  public void testLeftValue() {
    assertEquals("this", DIFFERENCE.leftValue());
  }

  @Test
  public void testRightValue() {
    assertEquals("that", DIFFERENCE.rightValue());
  }

  @Test
  public void testExistsOnLeft() {
    assertTrue(DIFFERENCE.existsOnLeft());
  }

  @Test
  public void testExistsOnRight() {
    assertTrue(DIFFERENCE.existsOnRight());
  }

}
