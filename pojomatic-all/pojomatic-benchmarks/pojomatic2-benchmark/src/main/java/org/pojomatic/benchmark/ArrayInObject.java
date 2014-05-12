package org.pojomatic.benchmark;

import java.io.PrintWriter;
import java.util.Random;

import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.SkipArrayCheck;

import com.google.caliper.BeforeExperiment;
import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

public class ArrayInObject {
  public static void main(String[] args) throws Exception {
    String[] fullArgs = new String[args.length + 1];
    fullArgs[0] = ArrayInObject.class.getName();
    System.arraycopy(args, 0, fullArgs, 1, args.length);
    CaliperMain.exitlessMain(
      fullArgs,
      new PrintWriter(System.out, true),
      new PrintWriter(System.err, true));
  }

  private final static Random rand = new Random();

  public interface ObjectBean {
    void setX(Object x);
  }

  public static class ArrayPossible implements ObjectBean {
    @Property
    Object x;

    @Override
    public void setX(Object x) {
      this.x = x;
    }
  }

  public static class ArrayNotPossible implements ObjectBean {
    @Property
    @SkipArrayCheck
    Object x;

    @Override
    public void setX(Object x) {
      this.x = x;
    }
  }

  private static ObjectBean[] beans;

  @Param({ "ARRAY_POSSIBLE", "ARRAY_NOT_POSSIBLE" })
  private Checker checker;

  @Benchmark
  public void equals(int reps) {
    checker.checkEquals(beans, reps);
  }

  @BeforeExperiment
  public void setUp() {
    beans = makeBeans(800);
  }

  private ObjectBean[] makeBeans(int beanCount) {
    ObjectBean[] beans = new ObjectBean[beanCount];
    for (int i = 0; i < beanCount; i++) {
      ObjectBean bean = checker.makeBean();
      bean.setX(String.valueOf(rand.nextDouble()));
      beans[i] = bean;;
    }
    return beans;
  }

  public static enum Checker {
    ARRAY_POSSIBLE(ArrayPossible.class), ARRAY_NOT_POSSIBLE(ArrayNotPossible.class);

    public void checkEquals(ObjectBean[] beans, int reps) {
      int i = 0, j = 0, rep = 0;

      while (rep++ < reps) {
        if (pojomator.doEquals(beans[i], beans[j]) != (i == j)) {
          System.out.println("error at " + i + ", " + j);
        }
        if (++j == beans.length) {
          j = 0;
          i = (i + 1) % beans.length;
        }
      }
    }

    @SuppressWarnings("unchecked")
    private Checker(Class<? extends ObjectBean> beanClass) {
      pojomator = (Pojomator<ObjectBean>) Pojomatic.pojomator(beanClass);
      this.beanClass = beanClass;
    }
    private final Pojomator<ObjectBean> pojomator;
    private final Class<? extends ObjectBean> beanClass;

    public ObjectBean makeBean() {
      try {
        return beanClass.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
