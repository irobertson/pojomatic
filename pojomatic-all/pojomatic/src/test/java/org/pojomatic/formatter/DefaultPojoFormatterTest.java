package org.pojomatic.formatter;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;
import org.pojomatic.PropertyElement;
import org.pojomatic.TestUtils;

@Deprecated
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

  @BeforeMethod
  public void setUp() {
    formatter = new DefaultPojoFormatter();
  }

  @Test
  public void testGetPropertyPrefix() {
    assertEquals(formatter.getPropertyPrefix(FIRST_NAME_FIELD), "firstName: {");
    assertEquals(formatter.getPropertyPrefix(LAST_NAME_FIELD), ", lastName: {");
    assertEquals(formatter.getPropertyPrefix(AGE_FIELD), ", age: {");
  }

  @Test
  public void testGetPropertySuffix() {
    assertEquals(formatter.getPropertySuffix(FIRST_NAME_FIELD), "}");
    assertEquals(formatter.getPropertySuffix(LAST_NAME_FIELD), "}");
    assertEquals(formatter.getPropertySuffix(AGE_FIELD), "}");
  }

  @Test
  public void testGetToStringPrefix() {
    assertEquals(formatter.getToStringPrefix(Integer.class), "Integer{");
  }

  @Test
  public void testGetToStringSuffix() {
    assertEquals(formatter.getToStringSuffix(Integer.class), "}");
  }
}
