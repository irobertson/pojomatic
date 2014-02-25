package org.pojomatic;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import org.pojomatic.annotations.Property;
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
    AssertJUnit.assertEquals(BEAN_POJOMATOR.doToString(BEAN), pojomator.doToString(BEAN));
  }

  @Test
  public void testToString() {
    AssertJUnit.assertEquals(BEAN_POJOMATOR.doToString(BEAN), Pojomatic.toString(BEAN));
  }

  @Test
  public void testDiffNoDifferences() {
    AssertJUnit.assertEquals(NoDifferences.getInstance(), Pojomatic.diff(BEAN, BEAN));
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
    AssertJUnit.assertEquals(BEAN_POJOMATOR.doHashCode(BEAN), Pojomatic.hashCode(BEAN));
  }

  @Test
  public void testEquals() {
    AssertJUnit.assertTrue(Pojomatic.equals(new Bean(3), new Bean(3)));
    AssertJUnit.assertFalse(Pojomatic.equals(new Bean(3), new Bean(4)));
  }

  @Test
  public void testCompatibleForEquality() {
    class BeanSubClass extends Bean{}

    class BeanWithExtraData extends Bean {
      @Property public int getY() { return 0; }
    }
    AssertJUnit.assertTrue(Pojomatic.areCompatibleForEquals(Bean.class, BeanSubClass.class));
    AssertJUnit.assertFalse(Pojomatic.areCompatibleForEquals(Bean.class, BeanWithExtraData.class));
    AssertJUnit.assertFalse(Pojomatic.areCompatibleForEquals(BeanWithExtraData.class, Bean.class));
  }
}
