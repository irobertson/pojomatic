package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.Property;

public class PojomatorFactoryTest {
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
    assertEquals(31 + Float.floatToIntBits(simple.x), pojomator.doHashCode(simple));
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
  public void testIncompatibleClassEquals() throws Exception {
    class Simple1 {
      @Property int x;
    }
    class Simple2 {
      @Property int x;
    }
    assertFalse(PojomatorFactory.makePojomator(Simple1.class).doEquals(new Simple1(), new Simple2()));
  }
}
