package org.pojomatic.diff;

import static org.junit.Assert.*;

import org.junit.Test;

public class DifferenceFromNullTest {

  @Test(expected=NullPointerException.class)
  public void testConstructorNullPointerException() {
    new DifferenceFromNull(null);
  }

  @Test
  public void testToString() {
    assertEquals("null is different than the object {3}", new DifferenceFromNull(3).toString());
  }

  @Test
  public void testAreEqual() {
    assertFalse(new DifferenceFromNull(3).areEqual());
  }
}
