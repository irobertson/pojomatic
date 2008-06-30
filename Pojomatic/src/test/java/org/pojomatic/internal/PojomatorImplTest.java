package org.pojomatic.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.Property;


public class PojomatorImplTest {
  private static Pojomator<StringProperty> STRING_PROPERTY_POJOMATOR =
    makePojomatorImpl(StringProperty.class);

  private static Pojomator<IntProperty> INT_PROPERTY_POJOMATOR =
    makePojomatorImpl(IntProperty.class);

  private static Pojomator<StringArrayProperty> STRING_ARRAY_PROPERTY_POJOMATOR =
    makePojomatorImpl(StringArrayProperty.class);

  @Test(expected=NullPointerException.class) public void testNullHashCode() {
    STRING_PROPERTY_POJOMATOR.doHashCode(null);
  }

  @Test(expected=NullPointerException.class) public void testToString() {
    STRING_PROPERTY_POJOMATOR.doToString(null);
  }

  @Test(expected=NullPointerException.class) public void testNullInstanceEquals() {
    STRING_PROPERTY_POJOMATOR.doEquals(null, new StringProperty("e"));
  }

  @Test public void testNullEquals() {
    assertFalse(STRING_PROPERTY_POJOMATOR.doEquals(new StringProperty(null), null));
  }

  @Test public void testReflexiveEquals() {
    ExceptionThrowingProperty instance = new ExceptionThrowingProperty();
    assertTrue(makePojomatorImpl(ExceptionThrowingProperty.class).doEquals(instance, instance));
  }

  @Test public void testCastCheckFailureForEquals() {
    assertFalse(STRING_PROPERTY_POJOMATOR.doEquals(new StringProperty("test"), "differentClass"));
  }

  @Test public void testPropertyEquals() {
    String s1 = "hello";
    String s2 = new String(s1); // ensure we are using .equals, and not ==

    assertTrue(STRING_PROPERTY_POJOMATOR.doEquals(new StringProperty(s1), new StringProperty(s2)));
    assertFalse(STRING_PROPERTY_POJOMATOR.doEquals(
      new StringProperty("hello"), new StringProperty("goodbye")));
  }

  @Test public void testNullPropertyEquals() {
    assertTrue(STRING_PROPERTY_POJOMATOR.doEquals(
      new StringProperty(null), new StringProperty(null)));
    assertFalse(STRING_PROPERTY_POJOMATOR.doEquals(
      new StringProperty(null), new StringProperty("not null over here")));
  }

  @Test public void testPrimativePropertyEquals() {
    assertTrue(INT_PROPERTY_POJOMATOR.doEquals(new IntProperty(3), new IntProperty(3)));
    assertFalse(INT_PROPERTY_POJOMATOR.doEquals(new IntProperty(3), new IntProperty(4)));
  }

  @Test public void testObjectArrayPropertyEquals() {
    String s1 = "hello";
    String s2 = new String(s1);
    assertTrue(STRING_ARRAY_PROPERTY_POJOMATOR.doEquals(
      new StringArrayProperty(s1, "goodbye"), new StringArrayProperty(s2, "goodbye")));
    assertFalse(STRING_ARRAY_PROPERTY_POJOMATOR.doEquals(
      new StringArrayProperty(s1, "goodbye"), new StringArrayProperty("goodbye", s1)));
  }

  //tests to write:
  // primitive arrays (all types)
  // deep object arrays
  // object array vs primitive and visa versa
  // array vs non array and visa versa
  // array vs null and visa versa

  private static class StringProperty {
    public StringProperty(String s) {
      this.s = s;
    }
    @Property public String s;
  }

  private static class IntProperty {
    public IntProperty(int i) {
      this.i = i;
    }
    @Property int i;
  }

  private static class StringArrayProperty {
    public StringArrayProperty(String... strings) {
      this.strings = strings;
    }

    @Property String[] strings;
  }

  private static class ExceptionThrowingProperty {
    @Property public int bomb() {
      throw new RuntimeException();
    }
  }

  private static <T> Pojomator<T> makePojomatorImpl(Class<T> clazz) {
    return new PojomatorImpl<T>(clazz);
  }
}
