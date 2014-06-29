package org.pojomatic.internal;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PropertyElementTest {

  @Retention(RetentionPolicy.RUNTIME) @interface Expected {
    String value();
  }

  @Test
  public void testGetDeclaringClass() throws Exception {
    assertEquals(new PropertyAccessor(getTestMethod(), "").getDeclaringClass(), PropertyElementTest.class);
    assertEquals(new PropertyField(getTestField(), "").getDeclaringClass(), PropertyElementTest.class);
  }

  @Test
  public void testEquals() throws Exception {
    PropertyAccessor testMethodProperty = new PropertyAccessor(getTestMethod(), "");
    assertEquals(testMethodProperty, testMethodProperty);
    assertFalse(testMethodProperty.equals(null));
    assertFalse(testMethodProperty.equals("someOtherClass"));
    assertEquals(new PropertyAccessor(getTestMethod(), ""), testMethodProperty);
    final PropertyField testFieldProperty = new PropertyField(getTestField(), "");
    assertFalse(testMethodProperty.equals(testFieldProperty));
    assertFalse(testFieldProperty.equals(testMethodProperty));
  }

  @Test
  public void testToString() throws Exception {
    assertEquals(new PropertyAccessor(getTestMethod(), "").toString(), getTestMethod().toString());
  }

  @Test
  public void testMethodHashCode() throws Exception {
    assertEquals(new PropertyAccessor(getTestMethod(), "salt").hashCode(), getTestMethod().hashCode());
  }

  @Test
  public void testFieldHashCode() throws Exception {
    assertEquals(new PropertyField(getTestField(), "salt").hashCode(), getTestField().hashCode());
  }

  @Test
  public void testGetNameForField() throws Exception {
    assertEquals(new PropertyField(getTestField(), "").getName(), "testField");
    assertEquals(new PropertyField(getTestField(), "foo").getName(), "foo");
  }

  @Test
  public void testOverrideNameForAccessor() throws Exception {
    assertEquals(new PropertyAccessor(getTestMethod(), "bar").getName(), "bar");
  }

  @Test
  public void testGetNameForAccessor() throws Exception {
    for (Method method: getClass().getDeclaredMethods()) {
      Expected expected = method.getAnnotation(Expected.class);
      if (expected != null) {
        assertEquals(new PropertyAccessor(method, "").getName(), expected.value(), "name for method " + method.getName());
      }
    }
  }

  // methods tested in testGetNameForAccessor
  @Expected("foo") public Object getFoo() { return null; }
  @Expected("URL") public Object getURL() { return null; }
  @Expected("get") public Object get() { return null; }
  @Expected("getter") public Object getter() { return null; }
  @Expected("isString") public Object isString() { return null; }
  @Expected("boolean") public Boolean isBoolean() { return null; }
  @Expected("bool") public boolean isBool() { return false; }
  @Expected("is") public boolean is() { return false; }

  private Method getTestMethod() throws Exception {
    return getMethod("testAccessor");
  }

  private Method getMethod(String name) throws Exception {
    return this.getClass().getDeclaredMethod(name, (Class[])null);
  }

  private Field getTestField() throws Exception {
    return getClass().getDeclaredField("testField");
  }

  // test properties
  @SuppressWarnings("unused")
  private Object testAccessor() { return "Test string"; }
  @SuppressWarnings("unused")
  private final Object testField = new Object();
}
