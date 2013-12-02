package org.pojomatic.internal;

import static org.junit.Assert.*;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_MULTIPLIER;
import static org.pojomatic.internal.PojomatorImpl.HASH_CODE_SEED;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.annotations.*;
import org.pojomatic.diff.Difference;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;

import com.google.common.collect.Sets;

public class PojomatorImplTest {
  private static Pojomator<ObjectProperty> OBJECT_PROPERTY_POJOMATOR =
    makePojomator(ObjectProperty.class);

  private static Pojomator<ObjectPairProperty> OBJECT_PAIR_PROPERTY_POJOMATOR =
    makePojomator(ObjectPairProperty.class);

  private static Pojomator<IntProperty> INT_PROPERTY_POJOMATOR =
    makePojomator(IntProperty.class);

  private static final Pojomator<AccessCheckedProperties> ACCESS_CHECKED_PROPERTIES_POJOMATOR =
    makePojomator(AccessCheckedProperties.class);

  private static final List<Class<?>> PRIMITIVE_TYPES = Arrays.<Class<?>>asList(
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
    assertTrue(makePojomator(ExceptionThrowingProperty.class).doEquals(instance, instance));
  }

  @Test public void testCastCheckFailureForEquals() {
    assertFalse(OBJECT_PROPERTY_POJOMATOR.doEquals(new ObjectProperty("test"), "differentClass"));
  }

  @Test public void testPropertyEquals() {
    String s1 = "hello";
    String s2 = copyString(s1); // ensure we are using .equals, and not ==

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

  @Test public void testPrimitivePropertyEquals() {
    assertTrue(INT_PROPERTY_POJOMATOR.doEquals(new IntProperty(3), new IntProperty(3)));
    assertFalse(INT_PROPERTY_POJOMATOR.doEquals(new IntProperty(3), new IntProperty(4)));
  }

  @Test public void testObjectArrayPropertyEquals() {
    class StringArrayProperty {
      public StringArrayProperty(String... strings) { this.strings = strings; }
      @Property String[] strings;
    }

    Pojomator<StringArrayProperty> STRING_ARRAY_PROPERTY_POJOMATOR = makePojomator(StringArrayProperty.class);

    String s1 = "hello";
    String s2 = copyString(s1);
    assertTrue(STRING_ARRAY_PROPERTY_POJOMATOR.doEquals(
      new StringArrayProperty(s1, "goodbye"), new StringArrayProperty(s2, "goodbye")));
    assertFalse(STRING_ARRAY_PROPERTY_POJOMATOR.doEquals(
      new StringArrayProperty(s1, "goodbye"), new StringArrayProperty("goodbye", s1)));
  }

  private String copyString(String s1) {
    return new String(s1); //NOPMD - we mean to make a copy
  }

  @Test
  public void testPrimitiveArrayAsObjectEquals() throws Exception {
    class Simple { @Property @CanBeArray Object x; public Simple(Object x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);
    final Simple nullProperty = new Simple(null);
    final Simple objectArrayProperty = new Simple(new String[] {"foo"});
    for (Class<?> primitiveType : PRIMITIVE_TYPES) {
      Simple main = new Simple(Array.newInstance(primitiveType, 3));
      Simple other = new Simple(Array.newInstance(primitiveType, 3));
      Simple different = new Simple(Array.newInstance(primitiveType, 4));
      assertTrue(pojomator.doEquals(main, other));
      assertFalse(pojomator.doEquals(nullProperty, main));
      assertFalse(pojomator.doEquals(main, nullProperty));
      assertFalse(pojomator.doEquals(objectArrayProperty, main));
      assertFalse(pojomator.doEquals(main, objectArrayProperty));
      assertFalse(pojomator.doEquals(main, different));
    }
  }

  @Test
  public void testBooleanArray() throws Exception {
    class Simple { @Property boolean[] x; public Simple(boolean... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple(true, false), new Simple(true, false)));
    assertFalse(pojomator.doEquals(new Simple(true, true), new Simple(true, false)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple(true), new Simple(true)));
    assertEquals(
      new PropertyDifferences(
        Arrays.<Difference>asList(new ValueDifference("x", new boolean[] {true}, new boolean[] {false}))),
      pojomator.doDiff(new Simple(true), new Simple(false)));


    assertEquals(31 + Arrays.hashCode(new boolean[] { true, false }), pojomator.doHashCode(new Simple(true, false)));
    assertEquals("Simple{x: {[true, false]}}", pojomator.doToString(new Simple(true, false)));
  }

  @Test
  public void testByteArray() throws Exception {
    class Simple { @Property byte[] x; public Simple(byte... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple((byte) 1, (byte) 2), new Simple((byte) 1, (byte) 2)));
    assertFalse(pojomator.doEquals(new Simple((byte) 1, (byte) 2), new Simple((byte) 2, (byte) 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple((byte) 1), new Simple((byte) 1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new byte[] {1}, new byte[] {2}))),
      pojomator.doDiff(new Simple((byte) 1), new Simple((byte) 2)));

    assertEquals(31 + Arrays.hashCode(new byte[] { 1, 2 }), pojomator.doHashCode(new Simple((byte) 1, (byte) 2)));

    assertEquals("Simple{x: {[1, 2]}}", pojomator.doToString(new Simple((byte) 1, (byte) 2)));
  }

  @Test
  public void testCharArray() throws Exception {
    class Simple { @Property char[] x; public Simple(char... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple('a', 'b'), new Simple('a', 'b')));
    assertFalse(pojomator.doEquals(new Simple('a', 'b'), new Simple('a', 'a')));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple('a'), new Simple('a')));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new char[] {'a'}, new char[] {'b'}))),
      pojomator.doDiff(new Simple('a'), new Simple('b')));

    assertEquals(31 + Arrays.hashCode(new char[] { 'a', 'b' }), pojomator.doHashCode(new Simple('a', 'b')));

    assertEquals("Simple{x: {['a', 'b', '0x2']}}", pojomator.doToString(new Simple('a', 'b', (char) 2)));
  }

  @Test
  public void testShortArray() throws Exception {
    class Simple { @Property short[] x; public Simple(short... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple((short) 1, (short) 2), new Simple((short) 1, (short) 2)));
    assertFalse(pojomator.doEquals(new Simple((short) 1, (short) 2), new Simple((short) 2, (short) 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple((short) 1), new Simple((short) 1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new short[] {1}, new short[] {2}))),
      pojomator.doDiff(new Simple((short) 1), new Simple((short) 2)));

    assertEquals(31 + Arrays.hashCode(new short[] { 1, 2 }), pojomator.doHashCode(new Simple((short) 1, (short) 2)));
    assertEquals("Simple{x: {[1, 2]}}", pojomator.doToString(new Simple((short) 1, (short) 2)));
  }

  @Test
  public void testIntArray() throws Exception {
    class Simple { @Property int[] x; public Simple(int... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple(1, 2), new Simple(1, 2)));
    assertFalse(pojomator.doEquals(new Simple(1, 2), new Simple(2, 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple(1), new Simple(1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new int[] {1}, new int[] {2}))),
      pojomator.doDiff(new Simple(1), new Simple(2)));

    assertEquals(31 + Arrays.hashCode(new int[] { 1, 2 }), pojomator.doHashCode(new Simple(1, 2)));

    assertEquals("Simple{x: {[1, 2]}}", pojomator.doToString(new Simple(1, 2)));
  }

  @Test
  public void testLongArray() throws Exception {
    class Simple { @Property long[] x; public Simple(long... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple(1, 2), new Simple(1, 2)));
    assertFalse(pojomator.doEquals(new Simple(1, 2), new Simple(2, 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple(1), new Simple(1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new long[] {1}, new long[] {2}))),
      pojomator.doDiff(new Simple(1), new Simple(2)));

    assertEquals(31 + Arrays.hashCode(new long[] { 1, 2 }), pojomator.doHashCode(new Simple(1, 2)));
    assertEquals("Simple{x: {[1, 2]}}", pojomator.doToString(new Simple(1, 2)));
  }

  @Test
  public void testFloatArray() throws Exception {
    class Simple { @Property float[] x; public Simple(float... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple(1, 2), new Simple(1, 2)));
    assertFalse(pojomator.doEquals(new Simple(1, 2), new Simple(2, 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple(1), new Simple(1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new float[] {1}, new float[] {2}))),
      pojomator.doDiff(new Simple(1), new Simple(2)));

    assertEquals(31 + Arrays.hashCode(new float[] { 1, 2 }), pojomator.doHashCode(new Simple(1, 2)));
    assertEquals("Simple{x: {[1.0, 2.0]}}", pojomator.doToString(new Simple(1, 2)));
  }

  @Test
  public void testDoubleArray() throws Exception {
    class Simple { @Property double[] x; public Simple(double... x) { this.x = x; } }
    Pojomator<Simple> pojomator = makePojomator(Simple.class);

    assertTrue(pojomator.doEquals(new Simple(1, 2), new Simple(1, 2)));
    assertFalse(pojomator.doEquals(new Simple(1, 2), new Simple(2, 2)));

    assertEquals(NoDifferences.getInstance(), pojomator.doDiff(new Simple(1), new Simple(1)));
    assertEquals(
      new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", new double[] {1}, new double[] {2}))),
      pojomator.doDiff(new Simple(1), new Simple(2)));

    assertEquals(31 + Arrays.hashCode(new double[] { 1, 2 }), pojomator.doHashCode(new Simple(1, 2)));
    assertEquals("Simple{x: {[1.0, 2.0]}}", pojomator.doToString(new Simple(1, 2)));
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

  @Test
  public void testDeepVersusShallowArrays() {
    class Shallow { @Property Object[] x; public Shallow(Object[] x) { this.x = x; } }
    class Deep { @Property @DeepArray Object[] x; public Deep(Object[] x) { this.x = x; } }

    Pojomator<Shallow> shallowPojomator = makePojomator(Shallow.class);
    Pojomator<Deep> deepPojomator = makePojomator(Deep.class);

    Object[] array1 = { new Object[] { "a", "b" }, new Object[] { "c", "d" } };
    Object[] array2 = { new Object[] { "a", "b" }, new Object[] { "c", "d" } };
    Object[] otherArray = new Object[][] { new Object[] { "a", "b" }, new Object[] { "c", "other" } };

    assertFalse(shallowPojomator.doEquals(new Shallow(array1), new Shallow(array2)));
    assertNotEquals(shallowPojomator.doHashCode(new Shallow(array1)), shallowPojomator.doHashCode(new Shallow(array2)));
    assertNotEquals(shallowPojomator.doToString(new Shallow(array1)), shallowPojomator.doToString(new Shallow(array2)));

    assertTrue(deepPojomator.doEquals(new Deep(array1), new Deep(array2)));
    assertEquals(deepPojomator.doHashCode(new Deep(array1)), deepPojomator.doHashCode(new Deep(array2)));
    assertEquals(deepPojomator.doToString(new Deep(array1)), deepPojomator.doToString(new Deep(array2)));

    assertFalse(deepPojomator.doEquals(new Deep(array1), new Deep(otherArray)));
    assertNotEquals(deepPojomator.doHashCode(new Deep(array1)), deepPojomator.doHashCode(new Deep(otherArray)));
    assertNotEquals(deepPojomator.doToString(new Deep(array1)), deepPojomator.doToString(new Deep(otherArray)));
  }

  @Test
  public void testDeepArrayAnnotatedPrimitiveArray() {
    class Simple { @Property @DeepArray int[] x = { 1, 2, 3 }; }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    assertEquals("Simple{x: {[1, 2, 3]}}", pojomator.doToString(new Simple()));
    pojomator.doHashCode(new Simple());
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

  @Test public void testPrimitiveHashCode() {
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

  @Test public void testPrimitiveArrayHashCode() throws Exception {
    for (Class<?> primitiveType : PRIMITIVE_TYPES) {
      Object primitiveArray = Array.newInstance(primitiveType, 2);
      int expected = HASH_CODE_MULTIPLIER * HASH_CODE_SEED +
        (Integer) Arrays.class.getDeclaredMethod("hashCode", primitiveArray.getClass())
        .invoke(null, primitiveArray);
      assertEquals(
        "primitive type " + primitiveType,
        expected,
        OBJECT_PROPERTY_POJOMATOR.doHashCode(
          new ObjectProperty(primitiveArray)));
    }
  }

  @Test public void testSimpleToString() {
    String actual = OBJECT_PAIR_PROPERTY_POJOMATOR.doToString(new ObjectPairProperty("ess", "tee"));
    assertEquals("ObjectPairProperty{s: {ess}, t: {tee}}", actual);
    // check that we use a new formatter each time
    assertEquals("ObjectPairProperty{s: {ess}, t: {tee}}", actual);
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

  @Test public void testDiffSameObject() {
    ObjectPairProperty objectPairProperty = new ObjectPairProperty("this", "that");
    assertTrue(
      OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(objectPairProperty, objectPairProperty).areEqual());
  }

  @Test public void testDiffEqualObjects() {
    assertTrue(
      OBJECT_PAIR_PROPERTY_POJOMATOR.doDiff(
        new ObjectPairProperty("this", "that"), new ObjectPairProperty("this", "that")).areEqual());
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

  @Test public void testPrivateClass() {
    Pojomator<PrivateClass> pojomator = makePojomator(PrivateClass.class);
    assertTrue(pojomator.doToString(new PrivateClass()).contains("number"));
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

  @Test public void testToString() {
    class SimplePojo {
      @Property(policy=PojomaticPolicy.EQUALS) int x;
      @Property(policy=PojomaticPolicy.HASHCODE_EQUALS) int y;
      @Property(policy=PojomaticPolicy.TO_STRING) int z;
    }
    assertEquals(
      "Pojomator for org.pojomatic.internal.PojomatorImplTest$1SimplePojo" +
      " with equals properties {x,y}," +
      " hashCodeProperties {y}," +
      " and toStringProperties {z}",
      Pojomatic.pojomator(SimplePojo.class).toString());
  }

  @PojoFormat(SimplePojoFormatter.class)
  public static class FormattedObject {
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

  public static class ObjectProperty {
    public ObjectProperty(Object s) {
      this.s = s;
    }
    @Property public Object s;
  }

  public static class ObjectPairProperty {
    public ObjectPairProperty(Object s, Object t) {
      this.s = s;
      this.t = t;
    }
    @Property public Object s;
    @Property public Object t;
  }

  public static class IntProperty {
    public IntProperty(int i) {
      this.i = i;
    }
    @Property int i;
  }

  public static class ExceptionThrowingProperty {
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
