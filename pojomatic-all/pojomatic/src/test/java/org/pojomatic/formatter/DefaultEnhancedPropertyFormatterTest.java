package org.pojomatic.formatter;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pojomatic.annotations.CanBeArray;
import org.pojomatic.annotations.DeepArray;

public class DefaultEnhancedPropertyFormatterTest {
  private EnhancedPropertyFormatter formatter;
  private StringBuilder builder;

  @DeepArray
  private Field deepArrayAnnotated;

  @CanBeArray
  private Field canBeArrayAnnotated;

  @DeepArray
  @CanBeArray
  private Field canBeDeepArrayAnnotated;

  @Before public void setUp() throws Exception {
    formatter = new DefaultEnhancedPropertyFormatter();
    builder = new StringBuilder();
    deepArrayAnnotated = getClass().getDeclaredField("deepArrayAnnotated");
    canBeArrayAnnotated = getClass().getDeclaredField("canBeArrayAnnotated");
    canBeDeepArrayAnnotated = getClass().getDeclaredField("canBeDeepArrayAnnotated");
  }

  @Test public void testFormat() {
    formatter.appendFormatted(builder, "7");
    assertFormatted("7");
  }

  @Test public void testFormatNull() {
    formatter.appendFormatted(builder, (Object) null);
    assertFormatted("null");
  }

  @Test public void testFormatBooleanFalse() {
    formatter.appendFormatted(builder, false);
    assertFormatted("false");
  }

  @Test public void testFormatBooleanTrue() {
    formatter.appendFormatted(builder, true);
    assertFormatted("true");
  }

  @Test public void testFormatList() {
    formatter.appendFormatted(builder, Arrays.asList(5, 7));
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfObjects() {
    formatter.appendFormatted(builder, new Integer[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfBooleans() {
    formatter.appendFormatted(builder, new boolean[] {true, false});
    assertFormatted("[true, false]");
  }

  @Test public void testFormatArrayOfBytes() {
    formatter.appendFormatted(builder, new byte[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfChars() {
    formatter.appendFormatted(builder, new char[] {5, 'b'});
    assertFormatted("['\\u0005', 'b']");
  }

  @Test public void testFormatArrayOfShorts() {
    formatter.appendFormatted(builder, new short[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfInts() {
    formatter.appendFormatted(builder, new int[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfLongs() {
    formatter.appendFormatted(builder, new long[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfFloats() {
    formatter.appendFormatted(builder, new float[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatArrayOfDoubles() {
    formatter.appendFormatted(builder, new double[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatDoubleArray() {
    formatter.initialize(deepArrayAnnotated);
    formatter.appendFormatted(builder, new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @Test public void testFormatDoubleArrayOfPrimitives() {
    formatter.initialize(deepArrayAnnotated);
    formatter.appendFormatted(builder, new int[][] {new int[] { 1, 2 }, new int[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatArrayOfObjectsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new Integer[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfBooleansAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new boolean[] {true, false});
    assertFormatted("[true, false]");
  }

  @Test public void testFormatArrayOfBytesAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new byte[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfCharsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new char[] {5, 'b'});
    assertFormatted("['\\u0005', 'b']");
  }

  @Test public void testFormatArrayOfShortsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new short[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfIntsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new int[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfLongsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new long[] {5, 7});
    assertFormatted("[5, 7]");
  }

  @Test public void testFormatArrayOfFloatsAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new float[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @Test public void testFormatArrayOfDoublesAsObject() {
    formatter.initialize(canBeArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new double[] {5, 7});
    assertFormatted("[5.0, 7.0]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatDoubleArrayAsObject() {
    formatter.initialize(canBeDeepArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new Integer[][] {new Integer[] { 1, 2 }, new Integer[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  @SuppressWarnings("cast")
  @Test public void testFormatDoubleArrayOfPrimitivesAsObject() {
    formatter.initialize(canBeDeepArrayAnnotated);
    formatter.appendFormattedPossibleArray(builder, new int[][] {new int[] { 1, 2 }, new int[] {3, 4} });
    assertFormatted("[[1, 2], [3, 4]]");
  }

  private void assertFormatted(String expected) {
    assertEquals(expected, builder.toString());
  }
}
