package org.pojomatic.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MatrixTest {

  private static final DefaultEnhancedPropertyFormatter FORMATTER = new DefaultEnhancedPropertyFormatter();

  @DataProvider(name = "types")
  public static Object[][] types() {
    List<Object[]> result = new ArrayList<>();
    for (Type type: PrimitiveType.values()) {
      result.add(new Object[] { type });
      result.add(new Object[] { new ArrayType(type) });
    }
    return result.toArray(new Object[0][0]);
  }

  @Test(dataProvider = "types")
  public void testHashCode(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        31 + Objects.hashCode(possibleArrayToList(value)),
        pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "types")
  public void testToString(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        "Pojo{x: {" + expectedFormat(value) + "}}",
        pojoFactory.pojomator().doToString(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "types")
  public void testEquals(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        AssertJUnit.assertEquals(
          "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
          value1 == value2,
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
      }
      AssertJUnit.assertFalse(
        "type: " + type.getClazz() + ", value1: " + value1,
        pojoFactory.pojomator().doEquals(pojoFactory.create(value1), null));
    }
  }

  /**
   * Convert arrays to lists, leaving other types alone.
   * @param value
   * @return {@code value} if value is not an array, or the List equivalent of {@code value} if value is an array
   */
  private Object possibleArrayToList(Object value) {
    if (value == null || ! value.getClass().isArray()) {
      return value;
    }
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < Array.getLength(value); i++) {
      result.add(Array.get(value, i));
    }
    return result;
  }

  private String expectedFormat(Object value) {
    if (value != null && value.getClass().isArray() && value.getClass().getComponentType() == char.class) {
      return FORMATTER.format(value);
    }
    else {
      return FORMATTER.format(possibleArrayToList(value));
    }
  }
}
