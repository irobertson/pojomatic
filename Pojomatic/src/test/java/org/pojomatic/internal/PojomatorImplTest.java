package org.pojomatic.internal;

import static org.junit.Assert.*;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_MULTIPLIER;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_SEED;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;

public class PojomatorImplTest {
  private static Pojomator<ObjectProperty> OBJECT_PROPERTY_POJOMATOR =
    makePojomatorImpl(ObjectProperty.class);

  private static Pojomator<ObjectPairProperty> OBJECT_PAIR_PROPERTY_POJOMATOR =
    makePojomatorImpl(ObjectPairProperty.class);

  private static Pojomator<IntProperty> INT_PROPERTY_POJOMATOR =
    makePojomatorImpl(IntProperty.class);

  private static Pojomator<StringArrayProperty> STRING_ARRAY_PROPERTY_POJOMATOR =
    makePojomatorImpl(StringArrayProperty.class);

  private static final List<Class<?>> PRIMATIVE_TYPES = Arrays.<Class<?>>asList(
    Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
    Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE);

  @Test(expected=NullPointerException.class) public void testNullHashCode() {
    OBJECT_PROPERTY_POJOMATOR.doHashCode(null);
  }

  @Test(expected=NullPointerException.class) public void testToStringOnNull() {
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
    final ObjectProperty nullProperty = new ObjectProperty(null);
    final ObjectProperty objectArrayProperty = new ObjectProperty(new String[] {"foo"});
    for (Class<?> primitiveType : PRIMATIVE_TYPES) {
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

  @Test public void testShortCircuitEquals() {
    final Pojomator<AccessCheckedProperties> pojomator = makePojomatorImpl(AccessCheckedProperties.class);

    AccessCheckedProperties left = new AccessCheckedProperties(1,1);
    AccessCheckedProperties right = new AccessCheckedProperties(2,2);
    assertFalse(pojomator.doEquals(left, right));
    assertFalse(left.getBCalled);
    assertFalse(right.getBCalled);

    assertTrue(pojomator.doEquals(left, left));
    assertFalse(left.getBCalled);

    assertFalse(pojomator.doEquals(left, null));
    assertFalse(left.getBCalled);

    assertFalse(pojomator.doEquals(left, "hello"));
    assertFalse(left.getBCalled);

    assertTrue(pojomator.doEquals(left, new AccessCheckedProperties(1,1)));
    assertTrue(left.getBCalled);
  }

  @Test public void testNullValueHashCode() {
    assertEquals(HASH_CODE_MULTIPLIER * HASH_CODE_SEED,
      OBJECT_PROPERTY_POJOMATOR.doHashCode(new ObjectProperty(null)));
  }

  @Test public void testPropertyHashCode() {
    assertEquals(
      HASH_CODE_MULTIPLIER * HASH_CODE_SEED + "foo".hashCode(),
      OBJECT_PROPERTY_POJOMATOR.doHashCode(new ObjectProperty("foo")));
  }

  @Test public void testPropertyPairHashCode() {
    assertEquals(
      HASH_CODE_MULTIPLIER * (HASH_CODE_MULTIPLIER * HASH_CODE_SEED + "foo".hashCode())
      + "bar".hashCode(),
      OBJECT_PAIR_PROPERTY_POJOMATOR.doHashCode(new ObjectPairProperty("foo", "bar")));
  }

  @Test public void testPrimativeHashCode() {
    assertEquals(
      HASH_CODE_MULTIPLIER * HASH_CODE_SEED + 7,
      INT_PROPERTY_POJOMATOR.doHashCode(new IntProperty(7)));
  }

  @Test public void testObjectArrayHashCode() {
    String[] strings = new String[] {"hello", "world" };
    assertEquals(
      HASH_CODE_MULTIPLIER *  HASH_CODE_SEED + Arrays.hashCode(strings),
      OBJECT_PROPERTY_POJOMATOR.doHashCode(new ObjectProperty(strings)));
  }

  @Test public void testPrimativeArrayHashCode() throws Exception {
    for (Class<?> primitiveType : PRIMATIVE_TYPES) {
      Object primativeArray = Array.newInstance(primitiveType, 2);
      int expected = HASH_CODE_MULTIPLIER * HASH_CODE_SEED +
        (Integer) Arrays.class.getDeclaredMethod("hashCode", primativeArray.getClass())
        .invoke(null, primativeArray);
      assertEquals(
        "primative type " + primitiveType,
        expected,
        OBJECT_PROPERTY_POJOMATOR.doHashCode(
          new ObjectProperty(primativeArray)));
    }
  }

  @Test public void testSimpleToString() {
    String actual = OBJECT_PAIR_PROPERTY_POJOMATOR.doToString(new ObjectPairProperty("ess", "tee"));
    assertEquals("ObjectPairProperty{s: {ess}, t: {tee}}", actual);
  }

  @Test public void testCustomFormatters() {
    assertEquals("PREFIXFormattedObject{s: {BEFOREx}}",
      makePojomatorImpl(FormattedObject.class).doToString(new FormattedObject("x")));
  }

  @PojoFormat(SimplePojoFormatter.class)
  private static class FormattedObject {
    public FormattedObject(Object s) {
      this.s = s;
    }
    @Property
    @PropertyFormat(SimplePropertyFormatter.class)
    public Object s;
  }

  public static class SimplePojoFormatter extends DefaultPojoFormatter {

    @Override
    public String getToStringPrefix(Class<?> pojoClass) {
      return "PREFIX" + super.getToStringPrefix(pojoClass);
    }
  }

  public static class SimplePropertyFormatter extends DefaultPropertyFormatter {
    @Override
    public String format(Object value) {
      return "BEFORE" + super.format(value);
    }
  }

  private static class ObjectProperty {
    public ObjectProperty(Object s) {
      this.s = s;
    }
    @Property public Object s;
  }

  private static class ObjectPairProperty {
    public ObjectPairProperty(Object s, Object t) {
      this.s = s;
      this.t = t;
    }
    @Property public Object s;
    @Property public Object t;
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

  public static class AccessCheckedProperties {
    public AccessCheckedProperties(int a, int b) {
      this.a = a;
      this.b = b;
    }

    @Property public int getA() {
      return a;
    }

    @Property public int getB() {
      getBCalled = true;
      return b;
    }

    private int a, b;
    private boolean getBCalled;
  }

  private static <T> Pojomator<T> makePojomatorImpl(Class<T> clazz) {
    return new PojomatorImpl<T>(clazz);
  }
}
