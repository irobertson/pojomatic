package org.pojomatic;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pojomatic.annotations.Property;
import org.pojomatic.diff.DifferenceFromNull;
import org.pojomatic.diff.DifferenceToNull;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.OnlyOnLeft;
import org.pojomatic.diff.OnlyOnRight;
import org.pojomatic.internal.PojomatorImpl;

import com.google.common.collect.Sets;

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

  @Test
  public void testDiffBothNull() {
    assertEquals(NoDifferences.getInstance(), Pojomatic.diff(null, null));
  }

  @Test
  public void testDiffNullFirst() {
    Iterable<OnlyOnRight> beanDifferences = Sets.newHashSet(new OnlyOnRight("x", BEAN.x));
    Differences differences = Pojomatic.diff(null, BEAN);
    assertEquals(new DifferenceFromNull(BEAN, beanDifferences), differences);
    assertEquals(beanDifferences, Sets.newHashSet(differences.differences()));
  }

  @Test
  public void testDiffNullSecond() {
    Iterable<OnlyOnLeft> beanDifferences = Sets.newHashSet(new OnlyOnLeft("x", BEAN.x));
    Differences differences = Pojomatic.diff(BEAN, null);
    assertEquals(new DifferenceToNull(BEAN, beanDifferences), differences);
    assertEquals(beanDifferences, Sets.newHashSet(differences.differences()));
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
