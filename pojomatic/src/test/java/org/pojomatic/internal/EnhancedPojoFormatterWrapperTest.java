package org.pojomatic.internal;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.pojomatic.PropertyElement;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Deprecated
public class EnhancedPojoFormatterWrapperTest {

  private static class SimplePojoFormatter implements PojoFormatter {
    @Override public String getToStringPrefix(Class<?> pojoClass) { return "pre-" + pojoClass.getSimpleName(); }
    @Override public String getToStringSuffix(Class<?> pojoClass) { return "post-" + pojoClass.getSimpleName(); }
    @Override public String getPropertyPrefix(PropertyElement property) { return property.getName() + "-pre"; }
    @Override public String getPropertySuffix(PropertyElement property) { return property.getName() + "-post"; }
  }

  int sampleField;

  private static EnhancedPojoFormatter wrapper = new EnhancedPojoFormatterWrapper(new SimplePojoFormatter());

  private static PropertyElement property;

  private StringBuilder builder;

  @BeforeClass
  public static void initProperty() throws ReflectiveOperationException {
   property = new PropertyField(EnhancedPojoFormatterWrapperTest.class.getDeclaredField("sampleField"), "foo");
  }

  @BeforeMethod
  public void initStringBuidler() {
    builder = new StringBuilder();
  }

  @Test
  public void appendPropertyPrefix() {
    wrapper.appendPropertyPrefix(builder, property);
    assertEquals(builder.toString(), "foo-pre");//delegate.getPropertyPrefix(property));
  }

  @Test
  public void appendPropertySuffix() {
    wrapper.appendPropertySuffix(builder, property);
    assertEquals(builder.toString(), "foo-post");
  }

  @Test
  public void appendToStringPrefix() {
    wrapper.appendToStringPrefix(builder, List.class);
    assertEquals(builder.toString(), "pre-List");
  }

  @Test
  public void appendToStringSuffix() {
    wrapper.appendToStringSuffix(builder, List.class);
    assertEquals(builder.toString(), "post-List");
  }

  @Test
  public void getPropertyPrefix() {
    assertEquals(wrapper.getPropertyPrefix(property), "foo-pre");
  }

  @Test
  public void getPropertySuffix() {
    assertEquals(wrapper.getPropertySuffix(property), "foo-post");
  }

  @Test
  public void getToStringPrefix() {
    assertEquals(wrapper.getToStringPrefix(List.class), "pre-List");
  }

  @Test
  public void getToStringSuffix() {
    assertEquals(wrapper.getToStringSuffix(List.class), "post-List");
  }
}
