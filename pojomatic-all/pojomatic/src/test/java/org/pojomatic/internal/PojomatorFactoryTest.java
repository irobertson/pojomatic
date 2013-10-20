package org.pojomatic.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.Property;

public class PojomatorFactoryTest {
  static class Simple {
    @Property int x;
  }

  @Test
  public void test() throws Exception {
    Pojomator<Simple> pojomator = PojomatorFactory.makePojomator(Simple.class);
    Simple simple = new Simple();
    simple.x = 3;
    assertEquals(31 + 3, pojomator.doHashCode(simple));
  }

}
