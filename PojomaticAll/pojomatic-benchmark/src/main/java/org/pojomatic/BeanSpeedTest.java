package org.pojomatic;

import java.util.Random;

public class BeanSpeedTest {
  private final static Random rand = new Random();
  
  public static void main(String[] args) {
    Bean[] beans = makeBeans(800);
    BaseChecker[] checkers = new BaseChecker[] {
        new StandardChecker(),
        new PmChecker(), 
        new PmFastChecker(),
        new PmHandRolledChecker(),
    };
    
    while(true) {
      for (BaseChecker checker: checkers) {
        long equalsStart = System.nanoTime();
        checker.checkEquals(beans);
        long equalsElapsed = System.nanoTime() - equalsStart;
        
        long hashStart = System.nanoTime();
        checker.checkHashCode(beans);
        long hashElapsed = System.nanoTime() - hashStart;
        
        long size = beans.length * beans.length;
        System.out.println(equalsElapsed / size 
          + "  -  " + hashElapsed / size 
          + "  - "  + checker.getClass().getSimpleName());
      }
      System.out.println();
    }
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
    int[] ints = new int[0/*rand.nextInt(10)*/];
    for (int i = 0; i < ints.length; i++) {
      ints[i] = rand.nextInt();
    }
    bean.setInts(null); //ints);
    bean.setString(String.valueOf(rand.nextDouble()));
    String[] strings = new String[0]; //rand.nextInt(5)];
    for (int i = 0; i < strings.length; i++) {
      strings[i] = String.valueOf(rand.nextInt());
    }
    bean.setStrings(null); //Arrays.asList(strings));
    return bean;
  }

  public static abstract class BaseChecker {
    public void checkEquals(Bean[] beans) {
      for (int i = 0; i < beans.length; i++) {
        for (int j = 0; j < beans.length; j++) {
          if (equals(beans[i], beans[j]) != (i == j)) {
            System.out.println("error at " + i + ", " + j);
          }
        }
      }
    }

    public void checkHashCode(Bean[] beans) {
      for (int i = 0; i < beans.length; i++) {
        for (int j = 0; j < beans.length; j++) {
          if ((hashCode(beans[i]) == hashCode(beans[j])) != (i == j)) {
            System.out.println("error at " + i + ", " + j);
          }
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
