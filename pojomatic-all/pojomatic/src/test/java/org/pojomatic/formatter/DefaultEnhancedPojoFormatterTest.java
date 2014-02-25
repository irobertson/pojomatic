package org.pojomatic.formatter;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;
import org.pojomatic.PropertyElement;
import org.pojomatic.TestUtils;

public class DefaultEnhancedPojoFormatterTest {

  private DefaultEnhancedPojoFormatter formatter;
  private StringBuilder builder;

  private final static class Foo {
    @SuppressWarnings("unused")
    private String firstName, lastName, age;
  }

  private final static PropertyElement FIRST_NAME_FIELD, LAST_NAME_FIELD, AGE_FIELD;
  static {
    try {
      FIRST_NAME_FIELD = TestUtils.field(Foo.class, "firstName");
      LAST_NAME_FIELD = TestUtils.field(Foo.class, "lastName");
      AGE_FIELD = TestUtils.field(Foo.class, "age");
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeMethod
  public void setUp() {
    formatter = new DefaultEnhancedPojoFormatter();
    builder = new StringBuilder();
  }

  @Test
  public void testGetPropertyPrefix() {
    formatter.appendPropertyPrefix(builder, FIRST_NAME_FIELD);
    builder.append('|');
    formatter.appendPropertyPrefix(builder, LAST_NAME_FIELD);
    builder.append('|');
    formatter.appendPropertyPrefix(builder, AGE_FIELD);

    assertFormatted("firstName: {|, lastName: {|, age: {");
  }

  @Test
  public void testGetPropertySuffix() {
    formatter.appendPropertySuffix(builder, FIRST_NAME_FIELD);
    builder.append('|');
    formatter.appendPropertySuffix(builder, LAST_NAME_FIELD);
    builder.append('|');
    formatter.appendPropertySuffix(builder, AGE_FIELD);
    assertFormatted("}|}|}");
  }

  @Test
  public void testGetToStringPrefix() {
    formatter.appendToStringPrefix(builder, Integer.class);
    assertFormatted("Integer{");
  }

  @Test
  public void testGetToStringSuffix() {
    formatter.appendToStringSuffix(builder, Integer.class);
    assertFormatted("}");
  }

  private void assertFormatted(String expected) {
    assertEquals(builder.toString(), expected);
  }
}
