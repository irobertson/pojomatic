package org.pojomatic;

import com.google.caliper.Benchmark;

public class MicroBenchmark {
  public static void main(String[] args) {
  }

  public void timeSleep1(int reps) throws Exception {
    for (int i = 0; i < reps; i++) {
      Thread.sleep(1);
    }
  }

  public void timeSleep2(int reps) throws Exception {
    for (int i = 0; i < reps; i++) {
      Thread.sleep(2);
    }
  }
}
