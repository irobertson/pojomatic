package org.pojomatic.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;

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
  public void testNullEquals() throws Exception {
    class Simple {
      @Property int x;
    }
    Simple instance = new Simple();
    assertFalse(PojomatorFactory.makePojomator(Simple.class).doEquals(instance, null));
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
