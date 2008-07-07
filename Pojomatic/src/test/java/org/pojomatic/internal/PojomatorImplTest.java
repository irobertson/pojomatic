package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.Property;


public class PojomatorImplTest {
  private static Pojomator<ObjectProperty> OBJECT_PROPERTY_POJOMATOR =
    makePojomatorImpl(ObjectProperty.class);

  private static Pojomator<IntProperty> INT_PROPERTY_POJOMATOR =
    makePojomatorImpl(IntProperty.class);

  private static Pojomator<StringArrayProperty> STRING_ARRAY_PROPERTY_POJOMATOR =
    makePojomatorImpl(StringArrayProperty.class);

  @Test(expected=NullPointerException.class) public void testNullHashCode() {
    OBJECT_PROPERTY_POJOMATOR.doHashCode(null);
  }

  @Test(expected=NullPointerException.class) public void testToString() {
    OBJECT_PROPERTY_POJOMATOR.doToString(null);
  }

  @Test(expected=NullPointerException.class) public void testNullInstanceEquals() {
    OBJECT_PROPERTY_POJOMATOR.doEquals(null, new ObjectProperty("e"));
  }

  @Test public void testNullEquals() {
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(new ObjectProperty(null), null));
  }

  @Test public void testReflexiveEquals() {
    ExceptionThrowingProperty instance = new ExceptionThrowingProperty();
    assertTrue(makePojomatorImpl(ExceptionThrowingProperty.class).doEquals(instance, instance));
  }

  @Test public void testCastCheckFailureForEquals() {
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(new ObjectProperty("test"), "differentClass"));
  }

  @Test public void testPropertyEquals() {
    String s1 = "hello";
    String s2 = new String(s1); // ensure we are using .equals, and not ==

    assertTrue(OBJECT_PROPERTY_POJOMATOR.doEquals(new ObjectProperty(s1), new ObjectProperty(s2)));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(
      new ObjectProperty("hello"), new ObjectProperty("goodbye")));
  }

  @Test public void testNullPropertyEquals() {
    assertTrue(OBJECT_PROPERTY_POJOMATOR.doEquals(
      new ObjectProperty(null), new ObjectProperty(null)));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(
      new ObjectProperty(null), new ObjectProperty("not null over here")));
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

  @Test public void testPrimitiveArrayEquals() throws Exception {
    List<Class<?>> primitiveTypes = Arrays.<Class<?>>asList(
      Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
      Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE);

    final ObjectProperty nullProperty = new ObjectProperty(null);
    final ObjectProperty objectArrayProperty = new ObjectProperty(new String[] {"foo"});
    for (Class<?> primitiveType : primitiveTypes) {
      ObjectProperty main = new ObjectProperty(Array.newInstance(primitiveType, 3));
      ObjectProperty other = new ObjectProperty(Array.newInstance(primitiveType, 3));
      assertTrue(OBJECT_PROPERTY_POJOMATOR.doEquals(main, other));
      assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(nullProperty, main));
      assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(main, nullProperty));
      assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(objectArrayProperty, main));
      assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(main, objectArrayProperty));
    }
  }

  @Test public void testDeepObjectArrayEquals() {
    //tests array of arrays, and that .equals is being called per element
    assertTrue(OBJECT_PROPERTY_POJOMATOR.doEquals(
      new ObjectProperty(new Object[] { "foo", new String[] {"bar"} }),
      new ObjectProperty(new Object[] { new String("foo"), new String[] {new String("bar")} })));

    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(
      new ObjectProperty(new Object[] { "foo", new String[] {"bar"} }),
      new ObjectProperty(new Object[] { new String("foo"), new String[] {new String("baz")} })));
  }

  @Test public void testArrayVsNonArrayEquals() {
    ObjectProperty arrayProperty = new ObjectProperty(new String[] {""});
    ObjectProperty stringProperty = new ObjectProperty("");
    ObjectProperty nullProperty = new ObjectProperty(null);

    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(arrayProperty, stringProperty));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(stringProperty, arrayProperty));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(arrayProperty, nullProperty));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(nullProperty, arrayProperty));
  }

  private static class ObjectProperty {
    public ObjectProperty(Object s) {
      this.s = s;
    }
    @Property public Object s;
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
