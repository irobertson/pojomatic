package org.pojomatic.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pojomatic.Pojomatic;
import org.pojomatic.junit.PojomaticAssert;

/**
 * Tests assertion methods such as assertEquals(Object, Object) for correctness of assertions.
 */
public abstract class AssertTest {


  /**
   * Only the unit under test should throw {@link AssertionError}, so no assertions are allowed
   * to be thrown from within the implementation of this method.
   *
   * @param expected the expected object
   * @param actual the actual object
   */
  protected abstract void performAssertEquals(Object expected, Object actual);

  /**
   * Only the unit under test should throw {@link AssertionError}, so no assertions are allowed
   * to be thrown from within the implementation of this method.
   *
   * @param expected the expected object
   * @param actual the actual object
   * @param message the messaage to include with the assertion
   */
  protected abstract void performAssertEquals(Object expected, Object actual, String message);

  private void performAssertEquals(
    Object expected, Object actual, String message, String expectedMessage) {
    try {
      performAssertEquals(expected, actual, message);
      fail("exception expected");
    }
    catch (AssertionError e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  @Test
  public final void assertEqualsWhenEqual() {
    performAssertEquals(new Container(3), new Container(3), "message");
  }

  @Test
  public final void assertEqualsWhenEqualNoMessage() {
    performAssertEquals(new Container(3), new Container(3));
  }

  @Test
  public final void assertEqualsBothNull() {
    performAssertEquals(null, null, null);
  }

  @Test
  public final void assertEqualsBothNullNoMessage() {
    performAssertEquals(null, null);
  }

  @Test
  public final void assertEqualsNullExpected() {
    performAssertEquals(
      null, new Container(null), null,
      "expected is null, but actual is Container{test: {null}}");
  }

  @Test
  public final void assertEqualsNullExpectedNoMessage() {
    performAssertEquals(
      null, new Container(null), null,
      "expected is null, but actual is Container{test: {null}}");
  }

  @Test
  public final void assertEqualsNullActual() {
    performAssertEquals(
      new Container(null), null, null, "actual is null, but expected is Container{test: {null}}");
  }

  /**
   * Tests that {@link PojomaticAssert#assertEqualsWithDiff(Object, Object)}
   * uses {@link Object#equals(Object)} instead of {@link Pojomatic#equals(Object, Object)}.
   */
  @Test
  public final void assertEqualsViaInheritedEquals() {
    //create objects which are never equal via Object.equals(Object), but are equal via
    //Pojomatic.equals(Object, Object)
    OnlyPojomaticEqual first = new OnlyPojomaticEqual();
    OnlyPojomaticEqual second = new OnlyPojomaticEqual();
    performAssertEquals(first, second, null,
      "differences between expected and actual:no differences (expected:<toString> but was:<toString>)");
  }

  @Test
  public final void assertEqualsNoMessage() {
    try {
      performAssertEquals(new Container("foo"), new Container("bar"));
    }
    catch (AssertionError e) {
      assertEquals("differences between expected and actual:[test: {foo} versus {bar}]" +
          " (expected:<Container{test: {foo}}> but was:<Container{test: {bar}}>)", e.getMessage());
    }
  }

  @Test
  public final void assertEqualsNullMessage() {
    performAssertEquals(
      new Container("foo"), new Container("bar"), null,
      "differences between expected and actual:[test: {foo} versus {bar}]" +
        " (expected:<Container{test: {foo}}> but was:<Container{test: {bar}}>)");
  }

  @Test
  public final void assertEqualsMessage2() {
    String first = "foo";
    String second = "bar";
    performAssertEquals(
      new Container(first), new Container(second), null,
      "differences between expected and actual:[test: {foo} versus {bar}]" +
        " (expected:<Container{test: {foo}}> but was:<Container{test: {bar}}>)");
  }

  @Test
  public final void assertEqualsCustomMessage() {
    performAssertEquals(
      new Container("foo"), new Container("bar"), "hello",
      "hello differences between expected and actual:[test: {foo} versus {bar}]" +
        " (expected:<Container{test: {foo}}> but was:<Container{test: {bar}}>)");
  }

  @Test
  public final void assertEqualsNonPojomatic() {
    performAssertEquals("string a", "string b", null, "expected:<string a> but was:<string b>");
  }

  @Test
  public final void assertEqualsNonComparable() {
    performAssertEquals(new Container("foo"), new DifferentPojo("foo"), null,
      "expected:<Container{test: {foo}}> but was:<DifferentPojo{test: {foo}}>");
  }
}
