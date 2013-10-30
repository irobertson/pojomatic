package org.pojomatic.formatter;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class DefaultEnhancedPropertyFormatterTest {
  private EnhancedPropertyFormatter formatter;
  private StringBuilder builder;

  @Before public void setUp() {
    formatter = new DefaultEnhancedPropertyFormatter();
    builder = new StringBuilder();
  }

  @Test public void testFormat() {
    formatter.formatTo(builder, "7");
    assertFormatted("7");
  }

  @Test public void testFormatNull() {
    formatter.formatTo(builder, (Object) null);
    assertFormatted("null");
  }

  @Test public void testFormatList() {
    formatter.formatTo(builder, Arrays.asList(5, 7));
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfObjects() {
    formatter.formatTo(builder, new Integer[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfBooleans() {
    formatter.formatTo(builder, new boolean[] {true, false});
    assertFormatted("[true, false]");
  }

  @Test public void testFormatArrayOfBytes() {
    formatter.formatTo(builder, new byte[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfChars() {
    formatter.formatTo(builder, new char[] {5, 'b'});
    assertFormatted("['0x5', 'b']");
  }

  @Test public void testFormatArrayOfShorts() {
    formatter.formatTo(builder, new short[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfInts() {
    formatter.formatTo(builder, new int[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfLongs() {
    formatter.formatTo(builder, new long[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfFloats() {
    formatter.formatTo(builder, new float[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatArrayOfDoubles() {
    formatter.formatTo(builder, new double[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatDoubleArray() {
    formatter.formatTo(builder, new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @Test public void testFormatDoubleArrayOfPrimitives() {
    formatter.formatTo(builder, new int[][] {new int[] { 1, 2 }, new int[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatArrayOfObjectsAsObject() {
    formatter.formatTo(builder, (Object) new Integer[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfBooleansAsObject() {
    formatter.formatTo(builder, (Object) new boolean[] {true, false});
    assertFormatted("[true, false]");
  }

  @Test public void testFormatArrayOfBytesAsObject() {
    formatter.formatTo(builder, (Object) new byte[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfCharsAsObject() {
    formatter.formatTo(builder, (Object) new char[] {5, 'b'});
    assertFormatted("['0x5', 'b']");
  }

  @Test public void testFormatArrayOfShortsAsObject() {
    formatter.formatTo(builder, (Object) new short[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfIntsAsObject() {
    formatter.formatTo(builder, (Object) new int[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfLongsAsObject() {
    formatter.formatTo(builder, (Object) new long[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfFloatsAsObject() {
    formatter.formatTo(builder, (Object) new float[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatArrayOfDoublesAsObject() {
    formatter.formatTo(builder, (Object) new double[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatDoubleArrayAsObject() {
    formatter.formatTo(builder, (Object) new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatDoubleArrayOfPrimitivesAsObject() {
    formatter.formatTo(builder, (Object) new int[][] {new int[] { 1, 2 }, new int[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  private void assertFormatted(String expected) {
    assertEquals(expected, builder.toString());
  }
}
