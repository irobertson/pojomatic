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

  @Test(expected=IllegalArgumentException.class)
  public void testEmptyDifferences() {
    new PropertyDifferences(Collections.<Difference>emptyList());
  }

  @Test
  public void testSingleDifferenceToString() {
    PropertyDifferences propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", 3, 4)));
    assertEquals(
      "[foo: {3} versus {4}]",
      propertyDifferences.toString());

    propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", null, 4)));
    assertEquals(
      "[foo: {null} versus {4}]",
      propertyDifferences.toString());
  }

  @Test
  public void testMultipleDifferencesToString() {
    assertEquals(
      "[foo: {3} versus {4}, bar: {this} versus {that}]",
      new PropertyDifferences(Arrays.<Difference>asList(
        new ValueDifference("foo", 3, 4), new ValueDifference("bar", "this", "that"))).toString());
  }

  @Test
  public void testAreEqual() {
    PropertyDifferences propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", 3, 4)));
    assertFalse(propertyDifferences.areEqual());
  }
}
