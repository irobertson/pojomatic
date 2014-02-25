package org.pojomatic.diff;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;

public class PropertyDifferencesTest {

  @Test(expectedExceptions=NullPointerException.class)
  public void testConstructorNullPointerException() {
    new PropertyDifferences(null);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void testEmptyDifferences() {
    new PropertyDifferences(Collections.<Difference>emptyList());
  }

  @Test
  public void testSingleDifferenceToString() {
    PropertyDifferences propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", 3, 4)));
    assertEquals(propertyDifferences.toString(), "[foo: {3} versus {4}]");

    propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", null, 4)));
    assertEquals(propertyDifferences.toString(), "[foo: {null} versus {4}]");
  }

  @Test
  public void testMultipleDifferencesToString() {
    assertEquals(new PropertyDifferences(Arrays.<Difference>asList(
    new ValueDifference("foo", 3, 4), new ValueDifference("bar", "this", "that"))).toString(), "[foo: {3} versus {4}, bar: {this} versus {that}]");
  }

  @Test
  public void testAreEqual() {
    PropertyDifferences propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", 3, 4)));
    assertFalse(propertyDifferences.areEqual());
  }
}
