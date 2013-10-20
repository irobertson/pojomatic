package org.pojomatic.internal;

import static org.junit.Assert.*;

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
  public void testObjectField() throws Exception {
    class Simple {
      @Property String s;
    }

    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    assertEquals(31, pojomator.doHashCode(simple));
    simple.s = "hello";
    assertEquals(31 + simple.s.hashCode(), pojomator.doHashCode(simple));
  }
}
