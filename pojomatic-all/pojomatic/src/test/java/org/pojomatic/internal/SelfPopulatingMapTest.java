package org.pojomatic.internal;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    AssertJUnit.assertSame(results[0], results[1]);
  }

  @Test
  public void testBadConstructionFirstTime() {
    final AtomicBoolean firstTime = new AtomicBoolean(false);
    final SelfPopulatingMap<String, String> selfPopulatingMap =
      new SelfPopulatingMap<String, String>() {
      @Override protected String create(String key) {
        if (firstTime.getAndSet(true)) {
          return new String(key);
        }
        else {
          throw new RuntimeException("first");
        }
      }
    };

    try {
      selfPopulatingMap.get("x");
      Assert.fail("Exception expected");
    }
    catch(RuntimeException e) {
      AssertJUnit.assertEquals("first", e.getMessage());
    }

    AssertJUnit.assertEquals("x", selfPopulatingMap.get("x"));
  }
}
