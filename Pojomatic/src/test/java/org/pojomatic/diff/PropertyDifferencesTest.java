package org.pojomatic.diff;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class PropertyDifferencesTest {

  @Test(expected=NullPointerException.class)
  public void testConstructorNullPointerException() {
    new PropertyDifferences(null);
  }
  @Test
  public void testEmptyToString() {
    assertEquals(
      "no differences", new PropertyDifferences(Collections.<Difference>emptyList()).toString());
  }

  @Test
  public void testSingleDifferenceToString() {
    assertEquals(
      "[foo: {3} versus {4}]",
      new PropertyDifferences(Arrays.asList(new Difference("foo", 3, 4))).toString());
    assertEquals(
      "[foo: {null} versus {4}]",
      new PropertyDifferences(Arrays.asList(new Difference("foo", null, 4))).toString());
  }

  @Test
  public void testMultipleDifferencesToString() {
    assertEquals(
      "[foo: {3} versus {4}, bar: {this} versus {that}]",
      new PropertyDifferences(Arrays.asList(
        new Difference("foo", 3, 4), new Difference("bar", "this", "that"))).toString());
  }

  @Test
  public void testAreEqual() {
    assertTrue(new PropertyDifferences(Collections.<Difference>emptyList()).areEqual());
    assertFalse(new PropertyDifferences(Arrays.asList(new Difference("foo", 3, 4))).areEqual());
  }
}
