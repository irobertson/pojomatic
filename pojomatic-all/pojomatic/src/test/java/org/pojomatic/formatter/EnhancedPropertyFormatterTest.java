package org.pojomatic.formatter;

import static org.testng.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.mockito.Mockito;
import org.pojomatic.internal.ArrayType;
import org.pojomatic.internal.EnhancedPropertyFormatterWrapper;
import org.pojomatic.internal.Type;
import org.pojomatic.internal.TypeProviders;
import org.testng.annotations.Test;

@Deprecated
public class EnhancedPropertyFormatterTest {

  // for testInitialize
  int x;

  @Test
  public void testInitialize() throws Exception {
    PropertyFormatter mock = Mockito.mock(PropertyFormatter.class);
    Field field = getClass().getDeclaredField("x");
    new EnhancedPropertyFormatterWrapper(mock).initialize(field);
    Mockito.verify(mock).initialize(field);
  }

  @Test(dataProvider="types", dataProviderClass=TypeProviders.class)
  public void testAppend(Type type) throws ReflectiveOperationException {
    EnhancedPropertyFormatter wrapper = new EnhancedPropertyFormatterWrapper(new DefaultPropertyFormatter());
    Method m = EnhancedPropertyFormatter.class.getDeclaredMethod("appendFormatted", StringBuilder.class, type.getClazz());
    for (Object value: type.getSampleValues()) {
      StringBuilder builder = new StringBuilder();
      m.invoke(wrapper, builder, value);
      assertEquals(builder.toString(), new DefaultPropertyFormatter().format(value));
    }
  }

  @Test(dataProvider="types", dataProviderClass=TypeProviders.class)
  public void testAppendArraysAsObject(Type type) throws ReflectiveOperationException {
    Type arrayType = new ArrayType(type);
    EnhancedPropertyFormatter wrapper = new EnhancedPropertyFormatterWrapper(new DefaultPropertyFormatter());
    Method m = EnhancedPropertyFormatter.class.getDeclaredMethod("appendFormatted", StringBuilder.class, Object.class);
    for (Object value: arrayType.getSampleValues()) {
      StringBuilder builder = new StringBuilder();
      m.invoke(wrapper, builder, value);
      assertEquals(builder.toString(), new DefaultPropertyFormatter().format(value));
    }
  }

  @Test(dataProvider="types", dataProviderClass=TypeProviders.class)
  public void testFormat(Type type) throws ReflectiveOperationException {
    DefaultPropertyFormatter formatter = new DefaultPropertyFormatter();
    EnhancedPropertyFormatter wrapper = new EnhancedPropertyFormatterWrapper(formatter);
    for (Object value: type.getSampleValues()) {
      assertEquals(wrapper.format(value), formatter.format(value));
    }
  }
}
