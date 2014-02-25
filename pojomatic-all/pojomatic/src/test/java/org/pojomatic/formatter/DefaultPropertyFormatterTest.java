package org.pojomatic.formatter;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.AssertJUnit;
import java.util.Arrays;

@Deprecated
public class DefaultPropertyFormatterTest {
  private PropertyFormatter formatter;

  @BeforeMethod
  public void setUp() {
    formatter = new DefaultPropertyFormatter();
    formatter.initialize(null);
  }

  @Test public void testFormat() {
    AssertJUnit.assertEquals("7", formatter.format(7));
  }

  @Test public void testFormatNull() {
    AssertJUnit.assertEquals("null", formatter.format(null));
  }

  @Test public void testFormatList() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(Arrays.asList(5, 7)));
  }

  @Test public void testFormatArrayOfObjects() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(new Integer[] {5, 7}));
  }

  @Test public void testFormatArrayOfBooleans() {
    AssertJUnit.assertEquals("[true, false]", formatter.format(new boolean[] {true, false}));
  }

  @Test public void testFormatArrayOfBytes() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(new byte[] {5, 7}));
  }

  @Test public void testFormatArrayOfChars() {
    AssertJUnit.assertEquals("['0x5', 'b']", formatter.format(new char[] {5, 'b'}));
  }

  @Test public void testFormatArrayOfShorts() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(new short[] {5, 7}));
  }

  @Test public void testFormatArrayOfInts() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(new int[] {5, 7}));
  }

  @Test public void testFormatArrayOfLongs() {
    AssertJUnit.assertEquals("[5, 7]", formatter.format(new long[] {5, 7}));
  }

  @Test public void testFormatArrayOfFloats() {
    AssertJUnit.assertEquals("[5.0, 7.0]", formatter.format(new float[] {5, 7}));
  }

  @Test public void testFormatArrayOfDoubles() {
    AssertJUnit.assertEquals("[5.0, 7.0]", formatter.format(new double[] {5, 7}));
  }

  @Test public void testFormatDoubleArray() {
    AssertJUnit.assertEquals(
      "[[1, 2], [3, 4]]",
      formatter.format(new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} }));
  }

  @Test public void testFormatDoubleArrayOfPrimitives() {
    AssertJUnit.assertEquals(
      "[[1, 2], [3, 4]]",
      formatter.format(new int[][] {new int[] { 1, 2 }, new int[] {3, 4} }));
  }
}
