package org.pojomatic.diff;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class DifferenceToNullTest {
  private static final Iterable<OnlyOnLeft> EMPTY_DIFFERENCES = Collections.<OnlyOnLeft>emptyList();

  @Test(expected = NullPointerException.class)
  public void testNullValue() {
    new DifferenceToNull(null, EMPTY_DIFFERENCES);
  }

  @Test(expected = NullPointerException.class)
  public void testNullDifferences() {
    new DifferenceToNull(new Object(), null);
  }

  @Test
  public void testToString() {
    DifferenceToNull differences = new DifferenceToNull(3, EMPTY_DIFFERENCES);
    assertEquals("the object {3} is different than null", differences.toString());
  }

  @Test
  public void testAreEqual() {
    assertFalse(new DifferenceToNull(3, EMPTY_DIFFERENCES).areEqual());
  }
}
