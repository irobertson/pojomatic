package org.pojomatic;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.SkipArrayCheck;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.internal.PojomatorFactory;

public class PojomaticTest {
  public static class Bean {
    @Property public final int x;
    public Bean(int x) { this.x = x; }
    public Bean() { x = 0; }
  }

  private static Pojomator<Bean> BEAN_POJOMATOR = PojomatorFactory.makePojomator(Bean.class);
  private static Bean BEAN = new Bean(1);

  @Test
  public void testPojomator() {
    Pojomator<Bean> pojomator = Pojomatic.pojomator(Bean.class);
    assertEquals(pojomator.doToString(BEAN), BEAN_POJOMATOR.doToString(BEAN));
  }

  @Test
  public void testToString() {
    assertEquals(Pojomatic.toString(BEAN), BEAN_POJOMATOR.doToString(BEAN));
  }

  @Test
  public void testDiffNoDifferences() {
    assertEquals(Pojomatic.diff(BEAN, BEAN), NoDifferences.getInstance());
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffBothNull() {
    Pojomatic.diff(null, null);
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffNullFirst() {
    Pojomatic.diff(null, BEAN);
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testDiffNullSecond() {
    Pojomatic.diff(BEAN, null);
  }

  @Test
  public void testHashCode() {
    assertEquals(Pojomatic.hashCode(BEAN), BEAN_POJOMATOR.doHashCode(BEAN));
  }

  @Test
  public void testEquals() {
    assertTrue(Pojomatic.equals(new Bean(3), new Bean(3)));
    assertFalse(Pojomatic.equals(new Bean(3), new Bean(4)));
  }

  @Test
  public void testCompatibleForEquality() {
    class BeanSubClass extends Bean{}

    class BeanWithExtraData extends Bean {
      @Property public int getY() { return 0; }
    }
    assertTrue(Pojomatic.areCompatibleForEquals(Bean.class, BeanSubClass.class));
    assertFalse(Pojomatic.areCompatibleForEquals(Bean.class, BeanWithExtraData.class));
    assertFalse(Pojomatic.areCompatibleForEquals(BeanWithExtraData.class, Bean.class));
  }

  @Test
  public void testSkipArrayCheck() {
    class Box {
      @Property
      @SkipArrayCheck
      Object o;

      Box(Object o) { this.o = o; }
    }
    assertFalse(Pojomatic.equals(new Box(new String[] { "x" }), new Box(new String[] { "x" })));
    assertNotEquals(Pojomatic.hashCode(new Box(new String[] { "x" })), Pojomatic.hashCode(new Box(new String[] { "x" })));

    String[] array = new String[] { "y" };
    assertTrue(Pojomatic.equals(new Box(array),  new Box(array)));
    assertEquals(Pojomatic.hashCode(new Box(array)), Pojomatic.hashCode(new Box(array)));
  }

  @Test
  public void testNoSkipArrayCheck() {
    class Box {
      @Property
      Object o;

      Box(Object o) { this.o = o; }
    }
    assertTrue(Pojomatic.equals(new Box(new String[] { "x" }), new Box(new String[] { "x" })));
    assertEquals(Pojomatic.hashCode(new Box(new String[] { "x" })), Pojomatic.hashCode(new Box(new String[] { "x" })));
  }

  @Test
  public void testSkipArrayCheckIgnoredForArrayType() {
    class Box {
      @Property
      @SkipArrayCheck
      Object[] os;

      Box(Object o) { this.os = new Object[] { o }; }
    }
    assertTrue(Pojomatic.equals(new Box(new String[] { "x" }), new Box(new String[] { "x" })));
    assertEquals(Pojomatic.hashCode(new Box(new String[] { "x" })), Pojomatic.hashCode(new Box(new String[] { "x" })));
  }


}
