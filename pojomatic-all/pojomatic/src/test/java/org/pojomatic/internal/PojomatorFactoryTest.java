package org.pojomatic.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.junit.Test;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;

import com.google.common.io.ByteStreams;

public class PojomatorFactoryTest {
  public static class ToBeDuplicated {
    @Property int x;
  }

  @Test
  public void testDuplciateClassNames() throws Exception {
    final String simpleName = ToBeDuplicated.class.getName().replace(".", "/");
    ClassLoader reloader = new ClassLoader(getClass().getClassLoader()) {
      @Override
      protected java.lang.Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.equals(simpleName)) {
          byte[] bytes;
          try {
            bytes = ByteStreams.toByteArray(ToBeDuplicated.class.getClassLoader().getResourceAsStream(name + ".class"));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Class<?> clazz = defineClass(ToBeDuplicated.class.getName(), bytes, 0, bytes.length);
          if (resolve) {
            resolveClass(clazz);
          }
          return clazz;
        }
        else {
          return super.loadClass(name, resolve);
        }
      }
    };
    Class<?> simple2 = reloader.loadClass(simpleName);
    assertNotEquals(ToBeDuplicated.class, simple2);
    Pojomator<ToBeDuplicated> pojomator1 = PojomatorFactory.makePojomator(ToBeDuplicated.class);
    assertTrue(pojomator1.doEquals(ToBeDuplicated.class.newInstance(), ToBeDuplicated.class.newInstance()));
    @SuppressWarnings("unchecked")
    Pojomator<Object> pojomator2 = (Pojomator<Object>) PojomatorFactory.makePojomator(simple2);
    assertTrue(pojomator2.doEquals(simple2.newInstance(), simple2.newInstance()));
  }

  @Test
  public void testBoolean() throws Exception{
    class Simple {
        @Property boolean x;
    }
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Boolean.FALSE.hashCode(), pojomator.doHashCode(simple));
    simple.x = true;
    assertEquals(31 + Boolean.TRUE.hashCode(), pojomator.doHashCode(simple));
  }

  @Test
  public void testByte() throws Exception{
    class Simple {
      @Property byte x;
    }
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Byte.valueOf(simple.x).hashCode(), pojomator.doHashCode(simple));
    simple.x = -5;
    assertEquals(31 + Byte.valueOf(simple.x).hashCode(), pojomator.doHashCode(simple));
  }

  @Test
  public void testIntField() throws Exception {
    class Simple {
      @Property int x = 3;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + 3, pojomator.doHashCode(simple));
  }

  @Test
  public void testIntGetter() throws Exception {
    class Simple {
      @Property int getX() { return 3; }
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + 3, pojomator.doHashCode(simple));
  }

  @Test
  public void testLongField() throws Exception {
    class Simple {
      @Property long x = 3;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + 3, pojomator.doHashCode(simple));
    simple.x = 5L << 16 + 3L;
    assertEquals(31 + Long.valueOf(simple.x).hashCode(), pojomator.doHashCode(simple));
  }

  @Test
  public void testFloatField() throws Exception {
    class Simple {
      @Property float x = 3;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Float.valueOf(simple.x).hashCode(), pojomator.doHashCode(simple));
  }

  @Test
  public void testDoubleField() throws Exception {
    class Simple {
      @Property double x = Math.PI;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Double.valueOf(simple.x).hashCode(), pojomator.doHashCode(simple));
  }

  private static class Inaccessible {
    @Override
    public int hashCode() {
      return 7;
    }
  }

  @Test
  public void testFieldWithInaccessibleType() throws Exception {
    class Simple {
      @Property Inaccessible x = new Inaccessible();
    }
    assertEquals (31 + 7, PojomatorFactory.makePojomator(Simple.class).doHashCode(new Simple()));
  }

  @Test
  public void testIntArrayField() throws Exception {
    class Simple {
      @Property int[] x = new int[] { 3, 4 };
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Arrays.hashCode(simple.x), pojomator.doHashCode(simple));
  }


  @Test
  public void testLongArrayField() throws Exception {
    class Simple {
      @Property long[] x = new long[] { 3, 4 };
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31 + Arrays.hashCode(simple.x), pojomator.doHashCode(simple));
  }

  @Test
  public void testStringField() throws Exception {
    class Simple {
      @Property String s;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31, pojomator.doHashCode(simple));
    simple.s = "hello";
    assertEquals(31 + simple.s.hashCode(), pojomator.doHashCode(simple));
  }

  @Test
  public void testObjectField() throws Exception {
    class Simple {
      @Property Object o;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    simple.o = null;
    assertEquals(31 + 0, pojomator.doHashCode(simple));
    simple.o = "hello";
    assertEquals(31 + simple.o.hashCode(), pojomator.doHashCode(simple));
    simple.o = new int[] { 2, 3 };
    assertEquals(31 + Arrays.hashCode((int[])simple.o), pojomator.doHashCode(simple));
    simple.o = new String[] { "hello", "goodbye"};
    assertEquals(31 + Arrays.hashCode((Object[])simple.o), pojomator.doHashCode(simple));
  }

  @Test
  public void testComplexObject() throws Exception {
    class Complex {
      @Property int i = 3;
      @Property Object o = "hello";
      @Property float f = 4.0f;
    }
    Complex complex = new Complex();
    Pojomator<Complex> pojomator = PojomatorFactory.makePojomator(Complex.class);
    assertEquals(
      31 * (31 * (31  + complex.i) + complex.o.hashCode()) + Float.floatToIntBits(complex.f),
      pojomator.doHashCode(complex));
  }

  @Test
  public void testSameInstanceEquals() throws Exception {
    class Simple {
      @Property int x;
    }
    Simple instance = new Simple();
    assertTrue(PojomatorFactory.makePojomator(Simple.class).doEquals(instance, instance));
  }

  @Test
  public void testNullEquals() throws Exception {
    class Simple {
      @Property int x;
    }
    Simple instance = new Simple();
    assertFalse(PojomatorFactory.makePojomator(Simple.class).doEquals(instance, null));
  }

  @Test
  public void testPrimitiveEquals() throws Exception {
    class Simple {
      @Property int x;
      Simple(int x) { this.x = x; }
    }
    Simple instance1 = new Simple(2);
    Simple instance2 = new Simple(3);
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    assertFalse(pojomator.doEquals(instance1, instance2));
    instance2.x = instance1.x;
    assertTrue(pojomator.doEquals(instance1, instance2));
  }

  @Test
  public void testArrayEquals() throws Exception {
    class Simple {
      @Property int[] x;
      Simple(int... x) { this.x = x; }
      Simple(@SuppressWarnings("unused") boolean b) { this.x = null; }
    }
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    assertTrue(pojomator.doEquals(new Simple(1,2,3), new Simple(1,2,3)));
    assertFalse(pojomator.doEquals(new Simple(1,2), new Simple(1,2,3)));
    assertTrue(pojomator.doEquals(new Simple(false), new Simple(false)));
    assertFalse(pojomator.doEquals(new Simple(1,2,3), new Simple(false)));
    assertFalse(pojomator.doEquals(new Simple(false), new Simple(1,2,3)));
    int[] ints = new int[] {1,2,3};
    assertTrue(pojomator.doEquals(new Simple(ints), new Simple(ints)));
  }

  @Test
  public void testStringEquals() throws Exception {
    class Simple {
      @Property String x;
      Simple(String x) { this.x = x; }
    }
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    assertFalse(pojomator.doEquals(new Simple("2"), new Simple("3")));
    assertTrue(pojomator.doEquals(new Simple("2"), new Simple(new String("2"))));
    assertTrue(pojomator.doEquals(new Simple(null), new Simple(null)));
    assertFalse(pojomator.doEquals(new Simple(null), new Simple("x")));
    assertFalse(pojomator.doEquals(new Simple("x"), new Simple(null)));
  }

  @Test
  public void testIncompatibleClassEquals() throws Exception {
    class Simple1 {
      @Property int x;
    }
    class Simple2 {
      @Property int x;
    }
    assertFalse(PojomatorFactory.makePojomator(Simple1.class).doEquals(new Simple1(), new Simple2()));
  }

  @Test
  public void testSimpleToString() throws Exception {
    class Simple {
      @Property public String x() { return "foo"; }
    }
    assertEquals("Simple{x: {foo}}", PojomatorFactory.makePojomator(Simple.class).doToString(new Simple()));
  }

  @Test
  public void testNonEnhancedPojoFormatter() throws Exception {
    @SuppressWarnings("deprecation")
    @PojoFormat(org.pojomatic.formatter.DefaultPojoFormatter.class)
    class Simple {
      @Property public String x() { return "foo"; }
    }
    assertEquals("Simple{x: {foo}}", PojomatorFactory.makePojomator(Simple.class).doToString(new Simple()));
  }

  @Test
  public void testRepeatedFieldNames() {
    class Parent {
      protected Parent(int x) { this.x = x; }
      @Property private int x;
    }
    class Child extends Parent {
      public Child(int x1, int x2) { super(x1); this.x = x2; }
      @Property private int x;
    }
    Pojomator<Child> pojomator = PojomatorFactory.makePojomator(Child.class);
    assertTrue(pojomator.doEquals(new Child(1, 2), new Child(1, 2)));
    assertFalse(pojomator.doEquals(new Child(1, 2), new Child(2, 1)));
  }

  static class InitializablePropertyFormatter extends DefaultEnhancedPropertyFormatter {
    private boolean initCalled;

    @Override
    public void initialize(AnnotatedElement element) {
      initCalled = true;
    }

    @Override
    public void appendFormatted(StringBuilder builder, int i) {
      super.appendFormatted(builder, initCalled ? i * 2 : i);
    }
  }

  @Test
  public void testPropertyFormatterRequringIntialization() {

    class Simple {
      @Property
      @PropertyFormat(InitializablePropertyFormatter.class)
      int i = 3;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    assertEquals("Simple{i: {6}}", pojomator.doToString(new Simple()));
  }
}
