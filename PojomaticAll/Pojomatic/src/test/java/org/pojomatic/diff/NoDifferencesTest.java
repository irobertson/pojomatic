package org.pojomatic.diff;

import static org.junit.Assert.*;

import org.junit.Test;

public class NoDifferencesTest {

  @Test
  public void testAreEqual() {
    assertTrue(NoDifferences.getInstance().areEqual());
  }

  @Test
  public void testToString() {
    assertEquals("no differences", NoDifferences.getInstance().toString());
  }

  @Test
  public void testDifferences() {
    assertFalse(NoDifferences.getInstance().differences().iterator().hasNext());
  }

}
