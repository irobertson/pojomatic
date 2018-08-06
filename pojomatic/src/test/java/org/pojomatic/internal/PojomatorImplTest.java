package org.pojomatic.internal;

import static org.testng.Assert.*;

import java.lang.reflect.Field;

import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class PojomatorImplTest {
  private final static int HASH_CODE_MULTIPLIER = 31;
  private final static int HASH_CODE_SEED = 1;

  private static Pojomator<ObjectProperty> OBJECT_PROPERTY_POJOMATOR =
    makePojomator(ObjectProperty.class);

  private static Pojomator<ObjectPairProperty> OBJECT_PAIR_PROPERTY_POJOMATOR =
    makePojomator(ObjectPairProperty.class);

  private static final Pojomator<AccessCheckedProperties> ACCESS_CHECKED_PROPERTIES_POJOMATOR =
    makePojomator(AccessCheckedProperties.class);

  @Test(expectedExceptions=NullPointerException.class) public void testNullHashCode() {
    OBJECT_PROPERTY_POJOMATOR.doHashCode(null);
  }

  @Test(expectedExceptions=NullPointerException.class) public void testToStringOnNull() {
    OBJECT_PROPERTY_POJOMATOR.doToString(null);
  }

  @Test(expectedExceptions=NullPointerException.class) public void testNullInstanceEquals() {
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
    assertEquals(OBJECT_PAIR_PROPERTY_POJOMATOR.doHashCode(new ObjectPairProperty("foo", "bar")), HASH_CODE_MULTIPLIER * (HASH_CODE_MULTIPLIER * HASH_CODE_SEED + "foo".hashCode())
    + "bar".hashCode());
  }

  @Test public void testToStringNames() {
    assertEquals(ACCESS_CHECKED_PROPERTIES_POJOMATOR.doToString(new AccessCheckedProperties(1, 2)), "AccessCheckedProperties{a: {1}, b: {2}}");
  }

  @Test public void testCustomFormatters() {
    assertEquals(makePojomator(FormattedObject.class).doToString(new FormattedObject("x")), "PREFIXFormattedObject{s: {BEFOREx}}");
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffNullInstance() {
    ObjectPairProperty other = new ObjectPairProperty("this", "that");
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(null, other);
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffNullOther() {
    ObjectPairProperty instance = new ObjectPairProperty("this", "that");
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(instance, null);
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffNulls() {
    OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(null, null);
  }

  @Test public void testDiffDifferentObjectsWithSinglePropertyDifferent() {
    final Differences diffs = OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(
      new ObjectPairProperty("this", "that"), new ObjectPairProperty("THIS", "that"));
    assertTrue(diffs instanceof PropertyDifferences);
    assertEquals(Sets.newHashSet(diffs.differences()), Sets.newHashSet(new ValueDifference("s", "this", "THIS")));
  }

  @Test public void testDiffDifferentObjectsWithMultiplePropertiesDifferent() {
    final Differences diffs = OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(
      new ObjectPairProperty("this", "that"), new ObjectPairProperty("THIS", "THAT"));
    assertEquals(diffs.getClass(), PropertyDifferences.class);
    assertEquals(Sets.newHashSet(diffs.differences()), Sets.newHashSet(new ValueDifference("s", "this", "THIS"), new ValueDifference("t", "that", "THAT")));
  }

  @Test public void testDiffAgainstWrongType() {
    Pojomator<?> pojomator = OBJECT_PAIR_PROPERTY_POJOMATOR;
    @SuppressWarnings("unchecked") Pojomator<Object> misCastPojomator = (Pojomator<Object>) pojomator;
    try {
      misCastPojomator.doDiff(new ObjectPairProperty(1,2), "wrong");
      fail("exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "other has type java.lang.String which is not compatible for equality with org.pojomatic.internal.PojomatorImplTest$ObjectPairProperty");
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
      assertEquals(e.getMessage(), "instance has type java.lang.String which is not compatible for equality with org.pojomatic.internal.PojomatorImplTest$ObjectPairProperty");
    }
  }

  @Test(expectedExceptions= NoPojomaticPropertiesException.class)
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

    assertEquals(pojomator.doToString(new Impl1()), "Interface{int: {2}, string: {hello}}");
    assertEquals(pojomator.doHashCode(new Impl1()), (HASH_CODE_MULTIPLIER + 2) * HASH_CODE_MULTIPLIER + "hello".hashCode());
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

  @Test public void testEqualsScopedProperties() throws Exception {
    assertFalse(PolicyProperties.policyUsedInEquals(PojomaticPolicy.NONE));
    assertFalse(PolicyProperties.policyUsedInEquals(PojomaticPolicy.TO_STRING));

    assertTrue(PolicyProperties.policyUsedInEquals(PojomaticPolicy.EQUALS));
    assertTrue(PolicyProperties.policyUsedInEquals(PojomaticPolicy.EQUALS_TO_STRING));
    assertTrue(PolicyProperties.policyUsedInEquals(PojomaticPolicy.HASHCODE_EQUALS));
    assertTrue(PolicyProperties.policyUsedInEquals(PojomaticPolicy.ALL));
  }

  @Test public void testHashCodeScopedProperties() throws Exception {
    assertFalse(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.NONE));
    assertFalse(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.TO_STRING));
    assertFalse(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.EQUALS));
    assertFalse(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.EQUALS_TO_STRING));

    assertTrue(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.HASHCODE_EQUALS));
    assertTrue(PolicyProperties.policyUsedInHashCode(PojomaticPolicy.ALL));
  }

  @Test public void testToStringScopedProperties() throws Exception {
    assertFalse(PolicyProperties.policyUsedInToString(PojomaticPolicy.NONE));
    assertFalse(PolicyProperties.policyUsedInToString(PojomaticPolicy.EQUALS));
    assertFalse(PolicyProperties.policyUsedInToString(PojomaticPolicy.HASHCODE_EQUALS));

    assertTrue(PolicyProperties.policyUsedInToString(PojomaticPolicy.TO_STRING));
    assertTrue(PolicyProperties.policyUsedInToString(PojomaticPolicy.EQUALS_TO_STRING));
    assertTrue(PolicyProperties.policyUsedInToString(PojomaticPolicy.ALL));
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

  private static class PolicyProperties {
      @Property(policy = PojomaticPolicy.ALL)
      public int all;

      @Property(policy = PojomaticPolicy.EQUALS)
      public int equals;

      @Property(policy = PojomaticPolicy.EQUALS_TO_STRING)
      public int equalsToString;

      @Property(policy = PojomaticPolicy.HASHCODE_EQUALS)
      public int hashCodeEquals;

      @Property(policy = PojomaticPolicy.TO_STRING)
      public int toString;

      @Property(policy = PojomaticPolicy.NONE)
      public int none;

      private static PolicyProperties withPolicyProperty(PojomaticPolicy policy, int value) throws Exception {
        PolicyProperties instance = new PolicyProperties();
        for (Field field : instance.getClass().getDeclaredFields()) {
          if (field.getAnnotation(Property.class).policy() == policy) {
            field.setInt(instance, value);
            return instance;
          }
        }
        throw new IllegalArgumentException("No field with policy " + policy.name());
      }

      public static boolean policyUsedInEquals(PojomaticPolicy policy) throws Exception {
        return ! POJOMATOR.doEquals(
          PolicyProperties.withPolicyProperty(policy, 1),
          PolicyProperties.withPolicyProperty(policy, 2));
      }

      public static boolean policyUsedInHashCode(PojomaticPolicy policy) throws Exception {
        return POJOMATOR.doHashCode(PolicyProperties.withPolicyProperty(policy, 1))
          != POJOMATOR.doHashCode(PolicyProperties.withPolicyProperty(policy, 2));
      }

      public static boolean policyUsedInToString(PojomaticPolicy policy) throws Exception {
        return ! POJOMATOR.doToString(PolicyProperties.withPolicyProperty(policy, 1)).equals(
          POJOMATOR.doToString(PolicyProperties.withPolicyProperty(policy, 2)));
      }

      private final static Pojomator<PolicyProperties> POJOMATOR = makePojomator(PolicyProperties.class);
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
