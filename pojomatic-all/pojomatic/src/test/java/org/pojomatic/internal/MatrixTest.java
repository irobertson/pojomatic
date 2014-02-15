package org.pojomatic.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.CanBeArray;
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

  @DataProvider(name = "arrayTypes")
  public static Object[][] arrayTypes() {
    List<Object[]> result = new ArrayList<>();
    for (Type type: PrimitiveType.values()) {
      result.add(new Object[] { new ArrayType(type), true });
      result.add(new Object[] { new ArrayType(type), false });
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

  @Test(dataProvider = "arrayTypes")
  public void testArrayAsObjectHashCode(Type type, boolean canBeArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray))));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        canBeArray
          ? 31 + Objects.hashCode(possibleArrayToList(value))
          : 31 + value.hashCode(),
        pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
    }
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Annotation>[] extraAnnotations(boolean canBeArray) {
    return canBeArray ? new Class[] { CanBeArray.class } : new Class[0];
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

  @Test(dataProvider = "arrayTypes")
  public void testArrayAsObjectToString(Type type, boolean canBeArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray))));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        "Pojo{x: {" + (canBeArray ? expectedFormat(value) : value.toString()) + "}}",
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

  @Test(dataProvider = "arrayTypes")
  public void testArrayAsObjectEquals(Type type, boolean canBeArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if the CanBeArray is present
        AssertJUnit.assertEquals(
          "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
          canBeArray && value1 == value2,
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(cloneArray(value2))));
        if (!canBeArray) {
          // however, even if CanBeArray is not present, identical arrays should still match
          AssertJUnit.assertEquals(
            "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
            value1 == value2,
            pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
        }
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
      StringBuilder stringBuilder = new StringBuilder();
      FORMATTER.appendFormatted(stringBuilder, (char[]) value);
      return stringBuilder.toString();
    }
    else {
      return FORMATTER.format(possibleArrayToList(value));
    }
  }

  private Object cloneArray(Object array) {
    Object clone = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
    for (int i = 0; i < Array.getLength(array); i++) {
      Array.set(clone, i, Array.get(array, i));
    }
    return clone;
  }
}
