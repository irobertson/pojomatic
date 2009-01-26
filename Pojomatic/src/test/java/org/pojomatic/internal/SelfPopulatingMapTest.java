package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class SelfPopulatingMapTest {
  /**
   * Test case which exposes a subtle threading bug
   */
  @Test public void testThreading() throws Exception {
    final String token = "token";
    final SelfPopulatingMap<String, String> selfPopulatingMap =
      new SelfPopulatingMap<String, String>() {
      @Override protected String create(String key) {
        try {
          Thread.sleep(10); // ensure that two threads have time to collide.
        }
        catch (InterruptedException e) {}
        return new String(key);
      }
    };

    int numThreads = 2;
    Thread[] threads = new Thread[numThreads];
    final String[] results = new String[numThreads];
    for (int i = 0; i < threads.length; i++) {
      final int threadNumber = i;
      threads[i] = new Thread() {
        @Override public void run() {
          results[threadNumber] = selfPopulatingMap.get(token);
        }
      };
    }
    for (Thread t: threads) {
      t.start();
    }
    for (Thread t: threads) {
      t.join();
    }
    assertSame(results[0], results[1]);
  }

  @Test
  public void testBadConstructionFirstTime() throws Exception {
    final AtomicBoolean firstTime = new AtomicBoolean(false);
    final SelfPopulatingMap<String, String> selfPopulatingMap =
      new SelfPopulatingMap<String, String>() {
      @Override protected String create(String key) {
        try {
          Thread.sleep(10); // ensure that two threads have time to collide.
        }
        catch (InterruptedException e) {}
        if (firstTime.getAndSet(true)) {
          return new String(key);
        }
        else {
          throw new RuntimeException("failing on first attempt");
        }
      }
    };
    int numThreads = 2;
    Thread[] threads = new Thread[numThreads];
    final String[] results = new String[numThreads];
    for (int i = 0; i < threads.length; i++) {
      final int threadNumber = i;
      threads[i] = new Thread() {
        @Override public void run() {
          results[threadNumber] = selfPopulatingMap.get("x");
        }
      };
    }
    for (Thread t: threads) {
      t.start();
    }
    for (Thread t: threads) {
      t.join();
    }
    assertNull(results[0]);
    assertEquals("x", results[1]);
  }
}
