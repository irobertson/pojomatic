package org.pojomatic.internal;

import static org.testng.Assert.*;

import java.lang.reflect.Method;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PropertyAccessorTest {

  public static class MethodHolder {
    public int getFoo() { return 0; }
    public int isFoo() { return 0; }
    public boolean getBar() { return true; }
    public boolean isBar() { return true; }
  }

  @Test
  public void testGetter() {
    assertEquals(new PropertyAccessor(getMethod("getFoo"), "").getName(), "foo");
  }

  @Test
  public void testBooleanGetter() {
    assertEquals(new PropertyAccessor(getMethod("getBar"), "").getName(), "bar");
  }

  @Test
  public void testNonBooleanIs() {
    assertEquals(new PropertyAccessor(getMethod("isFoo"), "").getName(), "isFoo");
  }

  @Test
  public void testBooleanIs() {
    assertEquals(new PropertyAccessor(getMethod("isBar"), "").getName(), "bar");
  }


  private Method getMethod(String methodName) {
    for (Method m: MethodHolder.class.getMethods()) {
      if (methodName.equals(m.getName())) {
        return m;
      }
    }
    throw new IllegalArgumentException("No method named " + methodName);
  }
}
