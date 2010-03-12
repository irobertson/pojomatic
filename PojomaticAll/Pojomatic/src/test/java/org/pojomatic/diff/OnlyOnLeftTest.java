package org.pojomatic.diff;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

public class OnlyOnLeftTest {
  private static final OnlyOnLeft INSTANCE = new OnlyOnLeft("name", "value");

  @Test(expected = NullPointerException.class)
  public void testNullPropertyName() {
    new OnlyOnLeft(null, new Object());
  }

  @Test
  public void testPropertyName() {
    assertEquals("name", INSTANCE.propertyName());
  }

  @Test
  public void testLeftValue() {
    assertEquals("value", INSTANCE.leftValue());
  }

  @Test
  public void testExistsOnLeft() {
    assertTrue(INSTANCE.existsOnLeft());
  }

  @Test
  public void testExistsOnRight() {
    assertFalse(INSTANCE.existsOnRight());
  }

  @Test(expected = NoSuchElementException.class)
  public void testRightValue() {
    INSTANCE.rightValue();
  }

}
