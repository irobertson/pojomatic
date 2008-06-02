package org.pojomatic;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;

public class PropertyElementTest {

  @Test(expected=NullPointerException.class)
  public void testGetValueNullField() throws Exception {
    PropertyElement<PropertyElementTest> propertyElement =
      new PropertyField<PropertyElementTest>(getTestField());
    propertyElement.getValue(null);
  }

  @Test(expected=NullPointerException.class)
  public void testGetValueNullMethod() throws Exception {
    PropertyElement<PropertyElementTest> propertyElement =
      new PropertyAccessor<PropertyElementTest>(getTestMethod());
    propertyElement.getValue(null);
  }

  @Test
  public void testGetValueField() throws Exception {
    PropertyElement<PropertyElementTest> propertyElement =
      new PropertyField<PropertyElementTest>(getTestField());
    assertEquals(testField, propertyElement.getValue(this));
  }

  @Test
  public void testGetValueMethod() throws Exception {
    PropertyElement<PropertyElementTest> propertyElement =
      new PropertyAccessor<PropertyElementTest>(getTestMethod());
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
