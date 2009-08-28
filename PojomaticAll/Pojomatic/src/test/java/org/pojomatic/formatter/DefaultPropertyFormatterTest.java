package org.pojomatic.formatter;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class DefaultPropertyFormatterTest {
  private PropertyFormatter formatter;

  @Before public void setUp() {
    formatter = new DefaultPropertyFormatter();
    formatter.initialize(null);
  }

  @Test public void testFormat() {
    assertEquals("7", formatter.format(7));
  }

  @Test public void testFormatNull() {
    assertEquals("null", formatter.format(null));
  }

  @Test public void testFormatList() {
    assertEquals("[5, 7]", formatter.format(Arrays.asList(5, 7)));
  }

  @Test public void testFormatArrayOfObjects() {
    assertEquals("[5, 7]", formatter.format(new Integer[] {5, 7}));
  }

  @Test public void testFormatArrayOfBooleans() {
    assertEquals("[true, false]", formatter.format(new boolean[] {true, false}));
  }

  @Test public void testFormatArrayOfBytes() {
    assertEquals("[5, 7]", formatter.format(new byte[] {5, 7}));
  }

  @Test public void testFormatArrayOfChars() {
    assertEquals("['0x5', 'b']", formatter.format(new char[] {5, 'b'}));
  }

  @Test public void testFormatArrayOfShorts() {
    assertEquals("[5, 7]", formatter.format(new short[] {5, 7}));
  }

  @Test public void testFormatArrayOfInts() {
    assertEquals("[5, 7]", formatter.format(new int[] {5, 7}));
  }

  @Test public void testFormatArrayOfLongs() {
    assertEquals("[5, 7]", formatter.format(new long[] {5, 7}));
  }

  @Test public void testFormatArrayOfFloats() {
    assertEquals("[5.0, 7.0]", formatter.format(new float[] {5, 7}));
  }

  @Test public void testFormatArrayOfDoubles() {
    assertEquals("[5.0, 7.0]", formatter.format(new double[] {5, 7}));
  }

  @Test public void testFormatDoubleArray() {
    assertEquals(
      "[[1, 2], [3, 4]]",
      formatter.format(new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} }));
  }

  @Test public void testFormatDoubleArrayOfPrimatives() {
    assertEquals(
      "[[1, 2], [3, 4]]",
      formatter.format(new int[][] {new int[] { 1, 2 }, new int[] {3, 4} }));
  }
}
