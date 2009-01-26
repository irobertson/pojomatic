package org.pojomatic.diff;

import static org.junit.Assert.*;

import org.junit.Test;

public class DifferenceTest {
  private Difference DIFFERENCE = new Difference("foo", "this", "that");

  @Test
  public void testGetPropertyName() {
    assertEquals("foo", DIFFERENCE.getPropertyName());
  }

  @Test
  public void testGetLhs() {
    assertEquals("this", DIFFERENCE.getLhs());
  }

  @Test
  public void testGetRhs() {
    assertEquals("that", DIFFERENCE.getRhs());
  }

}
