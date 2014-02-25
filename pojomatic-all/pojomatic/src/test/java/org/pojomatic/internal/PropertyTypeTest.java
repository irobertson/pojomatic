package org.pojomatic.internal;

import static org.testng.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.CanBeArray;
import org.pojomatic.annotations.DeepArray;
import org.pojomatic.diff.Difference;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;
import org.testng.annotations.Test;

public class PropertyTypeTest {

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testHashCode(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      checkHashCode(pojoFactory, value, type.hashCode(value));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectHashCode(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      int propertyHashCode = deepArray
        ? type.deepHashCode(value)
        : canBeArray
          ? type.hashCode(value)
          : Objects.hashCode(value);

      checkHashCode(pojoFactory, value, propertyHashCode);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayHashCode(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      int propertyHashCode = deepArray ? type.deepHashCode(value) : type.hashCode(value);
      checkHashCode(pojoFactory, value, propertyHashCode);
    }
  }

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testToString(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      checkToString(pojoFactory, value, type.toString(value));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectToString(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      String expectedPropertyValue =
        deepArray
          ? type.deepToString(value)
          : canBeArray
            ? type.toString(value)
            : Objects.toString(value);
      checkToString(pojoFactory, value, expectedPropertyValue);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayToString(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      String expectedPropertyValue = deepArray ? type.deepToString(value) : type.toString(value);
      checkToString(pojoFactory, value, expectedPropertyValue);
    }
  }

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testEqualsAndDiff(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value1: type.getSampleValues()) {
      Object pojo1 = pojoFactory.create(value1);
      for (Object value2: type.getSampleValues()) {
        boolean expectedToBeEqual = value1 == null ? value2 == null : value1.equals(value2);
        Object pojo2 = pojoFactory.create(value2);
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);

      }
      assertFalse(
        pojoFactory.pojomator().doEquals(pojo1, null),
        "type: " + type.getClazz() + ", value1: " + value1);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectEqualsAndDiff(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value1: type.getSampleValues()) {
      Object pojo1 = pojoFactory.create(value1);
      for (Object value2: type.getSampleValues()) {
        // Equality of different arrays should only be detected if the CanBeArray or DeepArray is present.
        // Note that in this test, we only clone the inner array if deepArray is true.
        boolean expectedToBeEqual = (value1 == value2) && (value1 == null || canBeArray || deepArray);
        Object pojo2 = pojoFactory.create(cloneArray(value2, deepArray));
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
        if (!canBeArray) {
          // however, even if CanBeArray is not present, identical arrays should still match
          checkEqualsAndDiff(value1 == value2, pojoFactory, value1, value2, pojo1, pojoFactory.create(value2));
        }
      }
      assertFalse(
        pojoFactory.pojomator().doEquals(pojo1, null),
        "type: " + type.getClazz() + ", value1: " + value1);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayEqualsAndDiff(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(canBeArray, deepArray))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if the CanBeArray is present
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        boolean expectedToBeEqual =
          (value1 == value2) && ((type.arrayDepth() < 2) || deepArray || noNestedArrays(value1));
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
        if (!canBeArray) {
          // however, even if CanBeArray is not present, identical arrays should still match
          checkEqualsAndDiff(value1 == value2, pojoFactory, value1, value2, pojo1, pojoFactory.create(value2));
        }
      }
    }
  }

  /**
   * Verify that doEquals honors the @{@link DeepArray} annotation on properties of type {@link Object}.
   * @param type
   * @param canBeArray
   * @param deepArray
   */
  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArrayAsObjectEqualsAndDiff(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if the CanBeArray is present
        // If value1 != value 2, then doEquals should always return false.
        // If value1 == value2, but canBeArray is false, then the only way doEquals can return true is if value1 == null
        // If value1 == value2 != null and canBeArray is true, then without deepArray, they can only be equal if
        //  there are no second level arrays that can be cloned.
        // The presence of @DeepArray on a field of type @Object should imply @CanBeArray
        boolean expectedToBeEqual =
          (value1 == value2) && (value1 == null || canBeArray || deepArray) && (deepArray || (noNestedArrays(value1)));
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
      }
    }
  }

  /**
   * Verify that doEquals honors the @{@link DeepArray} annotation on properties of array type
   * @param type
   * @param canBeArray - this should have no impact
   * @param deepArray
   */
  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArrayAsArrayEquals(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(canBeArray, deepArray))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // If value1 != value 2, then doEquals should always return false.
        // If value1 == value2, but canBeArray is false, then the only way doEquals can return true is if value1 == null
        // If value1 == value2 != null and canBeArray is true, then without deepArray, they can only be equal if
        //  there are no second level arrays that can be cloned.
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        boolean expectedToBeEqual = value1 == value2 && (deepArray || (noNestedArrays(value1)));
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Class<? extends Annotation>[] extraAnnotations(boolean canBeArray, boolean deepArray) {
    List<Class<? extends Annotation>> classes = new ArrayList<>();
    if (canBeArray) {
      classes.add(CanBeArray.class);
    }
    if (deepArray) {
      classes.add(DeepArray.class);
    }
    return classes.toArray(new Class[0]);
  }

  private void checkHashCode(PojoFactory pojoFactory, Object value,
    int propertyHashCode) {
    assertEquals(
      pojoFactory.pojomator().doHashCode(pojoFactory.create(value)),
      31 + propertyHashCode,
      label(value));
  }

  private void checkToString(PojoFactory pojoFactory, Object value,
    String expectedPropertyValue) {
    assertEquals(pojoFactory.pojomator().doToString(pojoFactory.create(value)), "Pojo{x: {" + expectedPropertyValue + "}}", label(value));
  }

  private void checkEqualsAndDiff(boolean expectedToBeEqual, PojoFactory pojoFactory,
    Object value1, Object value2, Object pojo1, Object pojo2) {
    assertEquals((Object) pojoFactory.pojomator().doEquals(pojo1, pojo2), (Object) expectedToBeEqual, label(value1, value2));
    assertEquals(pojoFactory.pojomator().doDiff(pojo1, pojo2), expectedDifferences(expectedToBeEqual, value1, value2), label(value1, value2));
  }

  /**
   * Return the expected Differences object for a pair of object
   * @param expectedToBeEqual whether we expect these two objects to be considered equal
   * @param value1 the first object
   * @param value2 the second object
   * @return the Differences we expect between the two
   */
  private Differences expectedDifferences(boolean expectedToBeEqual,
    Object value1, Object value2) {
    return expectedToBeEqual
      ? NoDifferences.getInstance()
      : new PropertyDifferences(Arrays.<Difference>asList(new ValueDifference("x", value1, value2)));
  }

  /**
   * Determine if the passed array has no nested arrays
   * @param array an object of array type, where elements are presumed to be either null, or themselves arrays.
   * @return {@code true} if {@code array} has no nested elements.
   */
  private boolean noNestedArrays(Object array) {
    if (array == null) {
      return true;
    }
    for (Object o: (Object[]) array) {
      if (o != null && o.getClass().isArray()) {
        return false;
      }
    }
    return true;
  }

  private String label(Object value1, Object value2) {
    return "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2);
  }

  private String label(Object value) {
    return "value: " + possibleArrayToList(value);
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
      result.add(possibleArrayToList(Array.get(value, i)));
    }
    return result;
  }

  private Object cloneArray(Object array, boolean deep) {
    if (array == null) {
      return null;
    }
    Object clone = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
    for (int i = 0; i < Array.getLength(array); i++) {
      Object element = Array.get(array, i);
      if (deep && element != null && element.getClass().isArray()) {
        element = cloneArray(element, deep);
      }
      Array.set(clone, i, element);
    }
    return clone;
  }
}
