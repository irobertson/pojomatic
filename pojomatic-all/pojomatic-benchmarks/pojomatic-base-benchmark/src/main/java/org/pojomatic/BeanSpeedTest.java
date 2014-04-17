package org.pojomatic;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

public class BeanSpeedTest {
  public static void main(String[] args) throws Exception {
    String[] fullArgs = new String[args.length + 1];
    fullArgs[0] = BeanSpeedTest.class.getName();
    System.arraycopy(args, 0, fullArgs, 1, args.length);
    CaliperMain.exitlessMain(
      fullArgs,
      new PrintWriter(System.out, true),
      new PrintWriter(System.err, true));
  }

  private final static Random rand = new Random();
  private static Bean[] beans;

  @Param({ "STANDARD", "STANDARD_INDIRECT", "POJOMATIC", "POJOMATIC_FAST" })
  private Checker checker;

  @Benchmark
  public void equals(int reps) {
    checker.checkEquals(beans, reps);
  }

  @Benchmark
  public void hashCode(int reps) {
    checker.checkHashCode(beans, reps);
  }

  @BeforeExperiment
  public void setUp() {
    beans = makeBeans(800);
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

  public static enum Checker {
    STANDARD {
      @Override protected boolean equals(Bean bean1, Bean bean2) {
        return bean1.equals(bean2);
      }

      @Override protected long hashCode(Bean bean) {
        return bean.hashCode();
      }
    },
    STANDARD_INDIRECT {
      @Override protected boolean equals(Bean bean1, Bean bean2) {
        return bean1.indirectEquals(bean2);
      }

      @Override protected long hashCode(Bean bean) {
        return bean.hashCode();
      }
    },
    POJOMATIC {
      @Override protected boolean equals(Bean bean1, Bean bean2) {
        return bean1.pmequals(bean2);
      }
      @Override protected long hashCode(Bean bean) {
        return bean.pmHashCode();
      }
    }
,
    POJOMATIC_FAST {
      @Override protected boolean equals(Bean bean1, Bean bean2) {
        return bean1.pmFastequals(bean2);
      }
      @Override protected long hashCode(Bean bean) {
        return bean.pmFastHashCode();
      }
    };

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

}
