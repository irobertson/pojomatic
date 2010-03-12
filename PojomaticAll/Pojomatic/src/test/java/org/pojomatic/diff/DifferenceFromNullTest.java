package org.pojomatic.diff;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class DifferenceFromNullTest {
  private static final Iterable<OnlyOnRight> EMPTY_DIFFERENCES =
    Collections.<OnlyOnRight>emptyList();

  @Test(expected = NullPointerException.class)
  public void testNullValue() {
    new DifferenceFromNull(null, EMPTY_DIFFERENCES);
  }

  @Test(expected = NullPointerException.class)
  public void testNullDifferences() {
    new DifferenceFromNull(new Object(), null);
  }

  @Test
  public void testToString() {
    DifferenceFromNull difference = new DifferenceFromNull(3, EMPTY_DIFFERENCES);
    assertEquals("null is different than the object {3}", difference.toString());
  }

  @Test
  public void testAreEqual() {
    assertFalse(new DifferenceFromNull(3, EMPTY_DIFFERENCES).areEqual());
  }
}
