package org.pojomatic.internal;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.pojomatic.PropertyElement;

public class PropertyElementTest {

  @Retention(RetentionPolicy.RUNTIME) @interface Expected {
    String value();
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testGetValueNullField() throws Exception {
    PropertyElement propertyElement = new PropertyField(getTestField(), "");
    propertyElement.getValue(null);
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void testGetValueNullMethod() throws Exception {
    PropertyElement propertyElement = new PropertyAccessor(getTestMethod(), "");
    propertyElement.getValue(null);
  }

  @Test
  public void testGetValueField() throws Exception {
    PropertyElement propertyElement = new PropertyField(getTestField(), "");
    AssertJUnit.assertEquals(testField, propertyElement.getValue(this));
  }

  @Test
  public void testGetValueMethod() throws Exception {
    PropertyElement propertyElement = new PropertyAccessor(getTestMethod(), "");
    AssertJUnit.assertEquals(testAccessor(), propertyElement.getValue(this));
  }

  @Test
  public void testGetDeclaringClass() throws Exception {
    AssertJUnit.assertEquals(PropertyElementTest.class, new PropertyAccessor(getTestMethod(), "").getDeclaringClass());
    AssertJUnit.assertEquals(PropertyElementTest.class, new PropertyField(getTestField(), "").getDeclaringClass());
  }

  @Test
  public void testEquals() throws Exception {
    PropertyAccessor testMethodProperty = new PropertyAccessor(getTestMethod(), "");
    AssertJUnit.assertEquals(testMethodProperty, testMethodProperty);
    AssertJUnit.assertFalse(testMethodProperty.equals(null));
    AssertJUnit.assertFalse(testMethodProperty.equals("someOtherClass"));
    AssertJUnit.assertEquals(testMethodProperty, new PropertyAccessor(getTestMethod(), ""));
    final PropertyField testFieldProperty = new PropertyField(getTestField(), "");
    AssertJUnit.assertFalse(testMethodProperty.equals(testFieldProperty));
    AssertJUnit.assertFalse(testFieldProperty.equals(testMethodProperty));
  }

  @Test
  public void testToString() throws Exception {
    AssertJUnit.assertEquals(getTestMethod().toString(), new PropertyAccessor(getTestMethod(), "").toString());
  }

  @Test
  public void testMethodHashCode() throws Exception {
    AssertJUnit.assertEquals(getTestMethod().hashCode(), new PropertyAccessor(getTestMethod(), "salt").hashCode());
  }

  @Test
  public void testFieldHashCode() throws Exception {
    AssertJUnit.assertEquals(getTestField().hashCode(), new PropertyField(getTestField(), "salt").hashCode());
  }

  @Test
  public void testGetNameForField() throws Exception {
    AssertJUnit.assertEquals("testField", new PropertyField(getTestField(), "").getName());
    AssertJUnit.assertEquals("foo", new PropertyField(getTestField(), "foo").getName());
  }

  @Test
  public void testOverrideNameForAccessor() throws Exception {
    AssertJUnit.assertEquals("bar", new PropertyAccessor(getTestMethod(), "bar").getName());
  }

  @Test
  public void testGetNameForAccessor() throws Exception {
    for (Method method: getClass().getDeclaredMethods()) {
      Expected expected = method.getAnnotation(Expected.class);
      if (expected != null) {
        AssertJUnit.assertEquals(
          "name for method " + method.getName(),
          expected.value(),
          new PropertyAccessor(method, "").getName());
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
    return this.getClass().getMethod(name, (Class[])null);
  }

  private Field getTestField() throws Exception {
    return getClass().getDeclaredField("testField");
  }

  // test properties
  public Object testAccessor() { return "Test string"; }
  private final Object testField = new Object();
}
