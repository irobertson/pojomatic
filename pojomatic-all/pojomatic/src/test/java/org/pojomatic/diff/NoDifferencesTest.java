package org.pojomatic.diff;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;

public class NoDifferencesTest {

  @Test
  public void testAreEqual() {
    AssertJUnit.assertTrue(NoDifferences.getInstance().areEqual());
  }

  @Test
  public void testToString() {
    AssertJUnit.assertEquals("no differences", NoDifferences.getInstance().toString());
  }

  @Test
  public void testDifferences() {
    AssertJUnit.assertFalse(NoDifferences.getInstance().differences().iterator().hasNext());
  }

}
