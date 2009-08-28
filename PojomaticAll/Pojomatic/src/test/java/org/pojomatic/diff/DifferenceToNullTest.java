package org.pojomatic.diff;

import static org.junit.Assert.*;

import org.junit.Test;

public class DifferenceToNullTest {

  @Test(expected=NullPointerException.class)
  public void testConstructorNullPointerException() {
    new DifferenceToNull(null);
  }

  @Test
  public void testToString() {
    assertEquals("the object {3} is different than null", new DifferenceToNull(3).toString());
  }

  @Test
  public void testAreEqual() {
    assertFalse(new DifferenceToNull(3).areEqual());
  }
}
