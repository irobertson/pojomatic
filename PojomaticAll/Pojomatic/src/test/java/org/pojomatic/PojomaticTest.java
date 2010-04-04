package org.pojomatic;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pojomatic.annotations.Property;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.internal.PojomatorImpl;

public class PojomaticTest {
  public static class Bean {
    @Property public final int x;
    public Bean(int x) { this.x = x; }
    public Bean() { x = 0; }
  }

  private static Pojomator<Bean> BEAN_POJOMATOR = new PojomatorImpl<Bean>(Bean.class);
  private static Bean BEAN = new Bean(1);

  @Test
  public void testPojomator() {
    Pojomator<Bean> pojomator = Pojomatic.pojomator(Bean.class);
    assertEquals(BEAN_POJOMATOR.doToString(BEAN), pojomator.doToString(BEAN));
  }

  @Test
  public void testToString() {
    assertEquals(BEAN_POJOMATOR.doToString(BEAN), Pojomatic.toString(BEAN));
  }

  @Test
  public void testDiffNoDifferences() {
    assertEquals(NoDifferences.getInstance(), Pojomatic.diff(BEAN, BEAN));
  }

  @Test(expected=NullPointerException.class)
  public void testDiffBothNull() {
    Pojomatic.diff(null, null);
  }

  @Test(expected=NullPointerException.class)
  public void testDiffNullFirst() {
    Pojomatic.diff(null, BEAN);
  }

  @Test(expected=NullPointerException.class)
  public void testDiffNullSecond() {
    Pojomatic.diff(BEAN, null);
  }

  @Test
  public void testHashCode() {
    assertEquals(BEAN_POJOMATOR.doHashCode(BEAN), Pojomatic.hashCode(BEAN));
  }

  @Test
  public void testEquals() {
    assertTrue(Pojomatic.equals(new Bean(3), new Bean(3)));
    assertFalse(Pojomatic.equals(new Bean(3), new Bean(4)));
  }

  @Test
  public void testCompatibleForEquality() {
    class BeanSubClass extends Bean{};

    class BeanWithExtraData extends Bean {
      @Property public int getY() { return 0; }
    }
    assertTrue(Pojomatic.areCompatibleForEquals(Bean.class, BeanSubClass.class));
    assertFalse(Pojomatic.areCompatibleForEquals(Bean.class, BeanWithExtraData.class));
    assertFalse(Pojomatic.areCompatibleForEquals(BeanWithExtraData.class, Bean.class));
  }
}
