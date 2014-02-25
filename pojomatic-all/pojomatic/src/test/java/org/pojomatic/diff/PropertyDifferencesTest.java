package org.pojomatic.diff;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
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
    AssertJUnit.assertEquals(
      "[foo: {3} versus {4}]",
      propertyDifferences.toString());

    propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", null, 4)));
    AssertJUnit.assertEquals(
      "[foo: {null} versus {4}]",
      propertyDifferences.toString());
  }

  @Test
  public void testMultipleDifferencesToString() {
    AssertJUnit.assertEquals(
      "[foo: {3} versus {4}, bar: {this} versus {that}]",
      new PropertyDifferences(Arrays.<Difference>asList(
        new ValueDifference("foo", 3, 4), new ValueDifference("bar", "this", "that"))).toString());
  }

  @Test
  public void testAreEqual() {
    PropertyDifferences propertyDifferences = new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("foo", 3, 4)));
    AssertJUnit.assertFalse(propertyDifferences.areEqual());
  }
}
