package org.pojomatic;

import java.util.Arrays;
import java.util.Random;

import com.google.caliper.Benchmark;
//import com.google.caliper.Runner;
//import com.google.caliper.SimpleBenchmark;
import com.google.caliper.runner.CaliperMain;

public class BeanSpeedTest extends Benchmark {
  private static final PmHandRolledChecker PM_HAND_ROLLED_CHECKER = new PmHandRolledChecker();
  private static final PmFastChecker PM_FAST_CHECKER = new PmFastChecker();
  private static final PmChecker PM_CHECKER = new PmChecker();
  private static final StandardChecker STANDARD_CHECKER = new StandardChecker();
  private static final AsmChecker ASM_CHECKER = new AsmChecker();
  private final static Random rand = new Random();
  private static Bean[] beans;
/*
  public void timeStandardCheckerEquals(int reps) {
    System.out.println("The beans are " + Arrays.toString(beans));
    STANDARD_CHECKER.checkEquals(beans, reps);
  }

  public void timePojomaticFastCheckerEquals(int reps) {
    PM_FAST_CHECKER.checkEquals(beans, reps);
  }

  public void timePojomaticCheckerEquals(int reps) {
    PM_CHECKER.checkEquals(beans, reps);
  }

  public void timePojomaticHandRolledCheckerEquals(int reps) {
    PM_HAND_ROLLED_CHECKER.checkEquals(beans, reps);
  }
*/
  public void timeStandardCheckerHashCode(int reps) {
    STANDARD_CHECKER.checkHashCode(beans, reps);
  }

  public void timePojomaticFastCheckerHashCode(int reps) {
    PM_FAST_CHECKER.checkHashCode(beans, reps);
  }

  public void timePojomaticCheckerHashCode(int reps) {
    PM_CHECKER.checkHashCode(beans, reps);
  }

  public void timePojomaticHandRolledCheckerHashCode(int reps) {
    PM_HAND_ROLLED_CHECKER.checkHashCode(beans, reps);
  }

  public void timeAsmHashCode(int reps) {
    ASM_CHECKER.checkHashCode(beans, reps);
  }

  public void setUp() {
    beans = makeBeans(800);
  }

  public static void main(String[] args) {
    System.out.println("The beans are " + Arrays.toString(beans));

    CaliperMain.main(BeanSpeedTest.class, args);
  }

  private static Bean[] makeBeans(int beanCount) {
    Bean[] beans = new Bean[beanCount];
    for (int i = 0; i < beanCount; i++) {
      beans[i] = randomBean();
    }
    return beans;
  }

  private static Bean randomBean() {
    Bean bean = new Bean();
    bean.setI(rand.nextInt());
    bean.setInteger(rand.nextInt());
    int[] ints = new int[rand.nextInt(10)];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = rand.nextInt();
    }
    bean.setInts(ints);
    bean.setString(String.valueOf(rand.nextDouble()));
    String[] strings = new String[rand.nextInt(5)];
    for (int i = 0; i < strings.length; i++) {
      strings[i] = String.valueOf(rand.nextInt());
    }
    bean.setStrings(Arrays.asList(strings));
    return bean;
  }

  public static abstract class BaseChecker {
    public void checkEquals(Bean[] beans) {
      checkEquals(beans, beans.length * beans.length);
    }

    public void checkEquals(Bean[] beans, int reps) {
      int i = 0, j = 0, rep = 0;

      while (rep++ < reps) {
        if (equals(beans[i], beans[j]) != (i == j)) {
          System.out.println("error at " + i + ", " + j);
        }
        if (++j == beans.length) {
          j = 0;
          i = (i + 1) % beans.length;
        }
      }
    }

    public void checkHashCode(Bean[] beans) {
      checkHashCode(beans, beans.length * beans.length);
    }

    public void checkHashCode(Bean[] beans, int reps) {
      int i = 0, j = 0, rep = 0;

      while (rep++ < reps) {
        if ((hashCode(beans[i]) == hashCode(beans[j])) != (i == j)) {
          System.out.println("error at " + i + ", " + j);
        }
        if (++j == beans.length) {
          j = 0;
          i = (i + 1) % beans.length;
        }
      }
    }

    protected abstract long hashCode(Bean bean);

    protected abstract boolean equals(Bean bean1, Bean bean2);
  }

  public static class StandardChecker extends BaseChecker {
    @Override protected boolean equals(Bean bean1, Bean bean2) {
      return bean1.equals(bean2);
    }

    @Override protected long hashCode(Bean bean) {
      return bean.hashCode();
    }
  }

  public static class AsmChecker extends BaseChecker {
    @Override protected boolean equals(Bean bean1, Bean bean2) {
      return bean1.equals(bean2); // FIXME
    }

    @Override protected long hashCode(Bean bean) {
      return bean.asmHashCode();
    }
  }

  public static class PmChecker extends BaseChecker {
    @Override protected boolean equals(Bean bean1, Bean bean2) {
      return bean1.pmequals(bean2);
    }
    @Override protected long hashCode(Bean bean) {
      return bean.pmHashCode();
    }
  }

  public static class PmFastChecker extends BaseChecker {
    @Override protected boolean equals(Bean bean1, Bean bean2) {
      return bean1.pmFastequals(bean2);
    }
    @Override protected long hashCode(Bean bean) {
      return bean.pmFastHashCode();
    }
  }

  public static class PmHandRolledChecker extends BaseChecker {
    @Override protected boolean equals(Bean bean1, Bean bean2) {
      return bean1.handRolledPmEquals(bean2);
    }
    @Override protected long hashCode(Bean bean) {
      return bean.handRolledPmHashCode();
    }
  }

}
