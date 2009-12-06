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
   * within the implementation of this method.
   *
   * @param first the object which should appear first if the assertion fails. Note that this
   * could be either "expected" or "actual".
   * @param second the object which should appear second if the assertion fails. Note that this
   * @param message the messaage to include with the assertion
   */
  protected abstract void performAssertEquals(Object first, Object second, String message);

  @Test
  public final void assertEqualsBothNull() {
    performAssertEquals(null, null, null);
  }

  @Test(expected=AssertionError.class)
  public final void assertEqualsNullExpected() {
    performAssertEquals(null, new Container(null), null);
  }

  @Test(expected=AssertionError.class)
  public final void assertEqualsNullActual() {
    performAssertEquals(new Container(null), null, null);
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

    performAssertEquals(first, second, null);
  }

  @Test
  public final void assertEqualsMessage() {
    String first = "foo";
    String second = "bar";
    try {
      performAssertEquals(new Container(first), new Container(second), null);
    }
    catch (AssertionError e) {
      assertEquals("[test: {foo} versus {bar}]", e.getMessage());
    }
    try {
      performAssertEquals(new Container(first), new Container(second), "custom message");
    }
    catch (AssertionError e) {
      assertEquals("custom message [test: {foo} versus {bar}]", e.getMessage());
    }
  }
}
