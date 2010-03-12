package org.pojomatic.diff;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

public class OnlyOnRightTest {
  private static final OnlyOnRight INSTANCE = new OnlyOnRight("name", "value");

  @Test(expected = NullPointerException.class)
  public void testNullPropertyName() {
    new OnlyOnRight(null, new Object());
  }

  @Test
  public void testPropertyName() {
    assertEquals("name", INSTANCE.propertyName());
  }

  @Test(expected = NoSuchElementException.class)
  public void testLeftValue() {
    INSTANCE.leftValue();
  }

  @Test
  public void testExistsOnLeft() {
    assertFalse(INSTANCE.existsOnLeft());
  }

  @Test
  public void testExistsOnRight() {
    assertTrue(INSTANCE.existsOnRight());
  }

  @Test
  public void testRightValue() {
    assertEquals("value", INSTANCE.rightValue());
  }

}
