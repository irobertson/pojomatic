package org.pojomatic.formatter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.pojomatic.PropertyElement;
import org.pojomatic.TestUtils;

public class DefaultPojoFormatterTest {

  private DefaultPojoFormatter formatter;

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

  @Before public void setUp() {
    formatter = new DefaultPojoFormatter();
  }

  @Test
  public void testGetPropertyPrefix() {
    assertEquals("firstName: {", formatter.getPropertyPrefix(FIRST_NAME_FIELD));
    assertEquals(", lastName: {", formatter.getPropertyPrefix(LAST_NAME_FIELD));
    assertEquals(", age: {", formatter.getPropertyPrefix(AGE_FIELD));
  }

  @Test
  public void testGetPropertySuffix() {
    assertEquals("}", formatter.getPropertySuffix(FIRST_NAME_FIELD));
    assertEquals("}", formatter.getPropertySuffix(LAST_NAME_FIELD));
    assertEquals("}", formatter.getPropertySuffix(AGE_FIELD));
  }

  @Test
  public void testGetToStringPrefix() {
    assertEquals("Integer{", formatter.getToStringPrefix(Integer.class));
  }

  @Test
  public void testGetToStringSuffix() {
    assertEquals("}", formatter.getToStringSuffix(Integer.class));
  }
}
