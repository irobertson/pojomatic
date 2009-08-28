package org.pojomatic.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pojomatic.Pojomatic;
import org.pojomatic.junit.PojomaticAssert;

/**
 * Tests assertion methods such as assertEquals(Object, Object) for correctness of assertions.
 */
public abstract class AssertTest {

  /**
   * Only the unit under test should throw {@link AssertionError}, so no assertions are allowed
   * within the implementation of this method.
   *
   * @param first the object which should appear first if the assertion fails. Note that this
   * could be either "expected" or "actual".
   * @param second the object which should appear second if the assertion fails. Note that this
   * could be either "expected" or "actual".
   */
  protected abstract void performAssertEquals(Object first, Object second);

  @Test
  public final void assertEqualsBothNull() {
    performAssertEquals(null, null);
  }

  @Test(expected=AssertionError.class)
  public final void assertEqualsNullExpected() {
    performAssertEquals(null, new Container(null));
  }

  @Test(expected=AssertionError.class)
  public final void assertEqualsNullActual() {
    performAssertEquals(new Container(null), null);
  }

  /**
   * Tests that {@link PojomaticAssert#assertEqualsWithDiff(Object, Object)}
   * uses {@link Object#equals(Object)} instead of {@link Pojomatic#equals(Object, Object)}.
   */
  @Test(expected=AssertionError.class)
  public final void assertEqualsViaInheritedEquals() {
    //create objects which are never equal via Object.equals(Object), but are equal via
    //Pojomatic.equals(Object, Object)
    OnlyPojomaticEqual first = new OnlyPojomaticEqual();
    OnlyPojomaticEqual second = new OnlyPojomaticEqual();

    performAssertEquals(first, second);
  }

  @Test
  public final void assertEqualsMessagingOrder() {
    String first = "foo";
    String second = "bar";
    try {
      performAssertEquals(new Container(first), new Container(second));
    }
    catch (AssertionError e) {
      //expected, check the message
      assertAppearanceOrder(e.getMessage(), first, second);
    }
  }

  /**
   * Asserts that {@code first} appears before {@code second} in {@code text}.
   *
   * @param text must contain both {@code first} and {@code second}
   * @param first cannot be {@code null}
   * @param second cannot be {@code null}
   * @throws NullPointerException if {@code first} or {@code second} is {@code null}
   * @throws IllegalArgumentException if {@code text} does not contain both
   * {@code first} and {@code second}
   */
  protected final void assertAppearanceOrder(String text, String first, String second) {
    if (first == null || second == null) {
      throw new NullPointerException();
    }
    int firstIndex = text.indexOf(first);
    int secondIndex = text.indexOf(second);
    if (firstIndex < 0 || secondIndex < 0) {
      throw new IllegalArgumentException("The string \"" + text + "\" does not conatin " +
        "both \"" + first + "\" and \"" + second + "\"");
    }
    String message = "In the string \"" + text + "\", " +
      "\"" + first + "\" should come before \"" + second + "\"";
    assertTrue(message, firstIndex < secondIndex);
  }

}
