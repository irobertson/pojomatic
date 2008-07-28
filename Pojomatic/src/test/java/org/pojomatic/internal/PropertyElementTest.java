package org.pojomatic.internal;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.pojomatic.PropertyElement;

public class PropertyElementTest {

  @Test(expected=NullPointerException.class)
  public void testGetValueNullField() throws Exception {
    PropertyElement propertyElement = new PropertyField(getTestField());
    propertyElement.getValue(null);
  }

  @Test(expected=NullPointerException.class)
  public void testGetValueNullMethod() throws Exception {
    PropertyElement propertyElement = new PropertyAccessor(getTestMethod());
    propertyElement.getValue(null);
  }

  @Test
  public void testGetValueField() throws Exception {
    PropertyElement propertyElement = new PropertyField(getTestField());
    assertEquals(testField, propertyElement.getValue(this));
  }

  @Test
  public void testGetValueMethod() throws Exception {
    PropertyElement propertyElement =
      new PropertyAccessor(getTestMethod());
    assertEquals(testAccessor(), propertyElement.getValue(this));
  }

  private Method getTestMethod() throws Exception {
    return this.getClass().getMethod("testAccessor", (Class[])null);
  }

  private Field getTestField() throws Exception {
    return this.getClass().getDeclaredField("testField");
  }

  public Object testAccessor() { return "Test string"; }
  private final Object testField = new Object();
}
