package org.pojomatic.internal;

import static org.junit.Assert.*;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_MULTIPLIER;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_SEED;

import org.junit.Test;
import org.pojomatic.Pojomator;
import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.annotations.*;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import com.google.common.collect.Sets;

public class PojomatorImplTest {
  private static Pojomator<ObjectProperty> OBJECT_PROPERTY_POJOMATOR =
    makePojomator(ObjectProperty.class);

  private static Pojomator<ObjectPairProperty> OBJECT_PAIR_PROPERTY_POJOMATOR =
    makePojomator(ObjectPairProperty.class);

  private static final Pojomator<AccessCheckedProperties> ACCESS_CHECKED_PROPERTIES_POJOMATOR =
    makePojomator(AccessCheckedProperties.class);

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
    assertTrue(makePojomator(ExceptionThrowingProperty.class).doEquals(instance, instance));
  }

  @Test public void testCastCheckFailureForEquals() {
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(new ObjectProperty("test"), "differentClass"));
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
    AccessCheckedProperties left = new AccessCheckedProperties(1,1);
    AccessCheckedProperties right = new AccessCheckedProperties(2,2);
    assertFalse(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doEquals(left, right));
    assertFalse(left.getBCalled);
    assertFalse(right.getBCalled);

    assertTrue(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doEquals(left, left));
    assertFalse(left.getBCalled);

    assertFalse(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doEquals(left, null));
    assertFalse(left.getBCalled);

    assertFalse(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doEquals(left, "hello"));
    assertFalse(left.getBCalled);

    assertTrue(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doEquals(left, new AccessCheckedProperties(1,1)));
    assertTrue(left.getBCalled);
  }

  @Test public void testPropertyPairHashCode() {
    assertEquals(
      HASH_CODE_MULTIPLIER * (HASH_CODE_MULTIPLIER * HASH_CODE_SEED + "foo".hashCode())
      + "bar".hashCode(),
      OBJECT_PAIR_PROPERTY_POJOMATOR.doHashCode(new ObjectPairProperty("foo", "bar")));
  }

  @Test public void testToStringNames() {
    assertEquals(
      "AccessCheckedProperties{a: {1}, b: {2}}",
      ACCESS_CHECKED_PROPERTIES_POJOMATOR.doToString(new AccessCheckedProperties(1, 2)));
  }

  @Test public void testCustomFormatters() {
    assertEquals("PREFIXFormattedObject{s: {BEFOREx}}",
      makePojomator(FormattedObject.class).doToString(new FormattedObject("x")));
  }

  @Test(expected=NullPointerException.class)
  public void testDiffNullInstance() {
    ObjectPairProperty other = new ObjectPairProperty("this", "that");
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(null, other);
  }

  @Test(expected=NullPointerException.class)
  public void testDiffNullOther() {
    ObjectPairProperty instance = new ObjectPairProperty("this", "that");
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(instance, null);
  }

  @Test(expected=NullPointerException.class)
  public void testDiffNulls() {
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(null, null);
  }

  @Test public void testDiffDifferentObjectsWithSinglePropertyDifferent() {
    final Differences diffs = OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(
      new ObjectPairProperty("this", "that"), new ObjectPairProperty("THIS", "that"));
    assertTrue(diffs instanceof PropertyDifferences);
    assertEquals(
      Sets.newHashSet(new ValueDifference("s", "this", "THIS")),
      Sets.newHashSet(diffs.differences()));
  }

  @Test public void testDiffDifferentObjectsWithMultiplePropertiesDifferent() {
    final Differences diffs = OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(
      new ObjectPairProperty("this", "that"), new ObjectPairProperty("THIS", "THAT"));
    assertEquals(PropertyDifferences.class, diffs.getClass());
    assertEquals(
      Sets.newHashSet(new ValueDifference("s", "this", "THIS"), new ValueDifference("t", "that", "THAT")),
      Sets.newHashSet(diffs.differences()));
  }

  @Test public void testDiffAgainstWrongType() {
    Pojomator<?> pojomator = OBJECT_PAIR_PROPERTY_POJOMATOR;
    @SuppressWarnings("unchecked") Pojomator<Object> misCastPojomator = (Pojomator<Object>) pojomator;
    try {
      misCastPojomator.doDiff(new ObjectPairProperty(1,2), "wrong");
      fail("exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(
        "other has type java.lang.String which is not compatible for equality with org.pojomatic.internal.PojomatorImplTest$ObjectPairProperty",
        e.getMessage());
    }
  }

  @Test public void testDiffWrongType() {
    Pojomator<?> pojomator = OBJECT_PAIR_PROPERTY_POJOMATOR;
    @SuppressWarnings("unchecked") Pojomator<Object> misCastPojomator = (Pojomator<Object>) pojomator;
    try {
      misCastPojomator.doDiff("wrong", new ObjectPairProperty(1,2));
      fail("exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(
        "instance has type java.lang.String which is not compatible for equality with org.pojomatic.internal.PojomatorImplTest$ObjectPairProperty",
        e.getMessage());
    }
  }

  @Test(expected= NoPojomaticPropertiesException.class)
  public void testNonPojomatedClass() {
    makePojomator(String.class);
  }

  @Test public void testInterface() {
    Pojomator<Interface> pojomator = makePojomator(Interface.class);
    class Impl1 implements Interface {
      @Override
      public int getInt() { return 2; }
      @Override
      public String getString() { return "hello"; }
    }

    class Impl2 implements Interface {
      private final String string;

      Impl2(String string) { this.string = string; }
      @Override
      public int getInt() { return 2; }
      @Override
      public String getString() { return string; }
    }

    assertEquals("Interface{int: {2}, string: {hello}}", pojomator.doToString(new Impl1()));
    assertEquals((
      HASH_CODE_MULTIPLIER + 2)*HASH_CODE_MULTIPLIER + "hello".hashCode(), pojomator.doHashCode(new Impl1()));
    assertTrue(pojomator.doEquals(new Impl1(), new Impl2("hello")));
    assertFalse(pojomator.doEquals(new Impl1(), new Impl2("goodbye")));
    assertFalse(pojomator.doEquals(new Impl1(), "not even in the right hierarchy"));
  }

  @Test public void testIsCompatibleForEquals() {
    assertTrue(OBJECT_PROPERTY_POJOMATOR.isCompatibleForEquality(ObjectProperty.class));
    assertFalse(OBJECT_PROPERTY_POJOMATOR.isCompatibleForEquality(ObjectPairProperty.class));
    assertTrue(makePojomator(Interface.class).isCompatibleForEquality(new Interface() {
      @Override
      public int getInt() { return 0; }
      @Override
      public String getString() { return null; }
    }.getClass()));
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

  public static class SimplePojoFormatter extends DefaultEnhancedPojoFormatter {
    @Override
    public void appendToStringPrefix(StringBuilder builder, Class<?> pojoClass) {
      builder.append("PREFIX");
      super.appendToStringPrefix(builder, pojoClass);
    }
  }

  public static class SimplePropertyFormatter extends DefaultEnhancedPropertyFormatter {
    @Override
    public void appendFormatted(StringBuilder builder, Object value) {
      builder.append("BEFORE");
      super.appendFormatted(builder, value);
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

  private static class ExceptionThrowingProperty {
    @Property public int bomb() {
      throw new RuntimeException();
    }
  }

  private static class AccessCheckedProperties {
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

  @AutoProperty
  private static class PrivateClass {
    @SuppressWarnings("unused")
    private int number;
  }

  @AutoProperty(autoDetect = AutoDetectPolicy.METHOD)
  private static interface Interface {
    public int getInt();
    public String getString();
  }

  private static <T> Pojomator<T> makePojomator(Class<T> clazz) {
    return PojomatorFactory.makePojomator(clazz);
  }
}
