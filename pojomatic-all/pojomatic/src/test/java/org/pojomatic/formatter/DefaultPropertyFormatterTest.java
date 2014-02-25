package org.pojomatic.formatter;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

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
    assertEquals(formatter.format(7), "7");
  }

  @Test public void testFormatNull() {
    assertEquals(formatter.format(null), "null");
  }

  @Test public void testFormatList() {
    assertEquals(formatter.format(Arrays.asList(5, 7)), "[5, 7]");
  }

  @Test public void testFormatArrayOfObjects() {
    assertEquals(formatter.format(new Integer[] {5, 7}), "[5, 7]");
  }

  @Test public void testFormatArrayOfBooleans() {
    assertEquals(formatter.format(new boolean[] {true, false}), "[true, false]");
  }

  @Test public void testFormatArrayOfBytes() {
    assertEquals(formatter.format(new byte[] {5, 7}), "[5, 7]");
  }

  @Test public void testFormatArrayOfChars() {
    assertEquals(formatter.format(new char[] {5, 'b'}), "['0x5', 'b']");
  }

  @Test public void testFormatArrayOfShorts() {
    assertEquals(formatter.format(new short[] {5, 7}), "[5, 7]");
  }

  @Test public void testFormatArrayOfInts() {
    assertEquals(formatter.format(new int[] {5, 7}), "[5, 7]");
  }

  @Test public void testFormatArrayOfLongs() {
    assertEquals(formatter.format(new long[] {5, 7}), "[5, 7]");
  }

  @Test public void testFormatArrayOfFloats() {
    assertEquals(formatter.format(new float[] {5, 7}), "[5.0, 7.0]");
  }

  @Test public void testFormatArrayOfDoubles() {
    assertEquals(formatter.format(new double[] {5, 7}), "[5.0, 7.0]");
  }

  @Test public void testFormatDoubleArray() {
    assertEquals(formatter.format(new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} }), "[[1, 2], [3, 4]]");
  }

  @Test public void testFormatDoubleArrayOfPrimitives() {
    assertEquals(formatter.format(new int[][] {new int[] { 1, 2 }, new int[] {3, 4} }), "[[1, 2], [3, 4]]");
  }
}
