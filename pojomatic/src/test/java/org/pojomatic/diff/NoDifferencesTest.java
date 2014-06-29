package org.pojomatic.diff;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class NoDifferencesTest {

  @Test
  public void testAreEqual() {
    assertTrue(NoDifferences.getInstance().areEqual());
  }

  @Test
  public void testToString() {
    assertEquals(NoDifferences.getInstance().toString(), "no differences");
  }

  @Test
  public void testDifferences() {
    assertFalse(NoDifferences.getInstance().differences().iterator().hasNext());
  }

}
