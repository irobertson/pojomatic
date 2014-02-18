package org.pojomatic.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.CanBeArray;
import org.pojomatic.annotations.DeepArray;
import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class MatrixTest {

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testHashCode(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        31 + type.hashCode(value),
        pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectHashCode(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        (canBeArray || deepArray)
          ? 31 + (deepArray ? type.deepHashCode(value) : type.hashCode(value))
          : 31 + Objects.hashCode(value),
        pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
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

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testToString(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        "Pojo{x: {" + type.toString(value) + "}}",
        pojoFactory.pojomator().doToString(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectToString(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value: type.getSampleValues()) {
      String expectedPropertyValue =
        (canBeArray || deepArray) ?
          (deepArray ?
            type.deepToString(value) :
            type.toString(value)) :
          Objects.toString(value);
      AssertJUnit.assertEquals(
        "value: " + possibleArrayToList(value),
        "Pojo{x: {" + expectedPropertyValue + "}}",
        pojoFactory.pojomator().doToString(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
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

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectEquals(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(canBeArray, deepArray))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if the CanBeArray is present
        AssertJUnit.assertEquals(
          "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
          (value1 == null && value2 == null) || ((canBeArray || deepArray) && value1 == value2),
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(cloneArray(value2, deepArray))));
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

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayEquals(Type type, boolean canBeArray, boolean deepArray) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(canBeArray, deepArray))));
    if (type.arrayDepth() == 2) {
      System.out.println("time to debug");
    }
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if the CanBeArray is present
        AssertJUnit.assertEquals(
          "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
          value1 == value2 && ((type.arrayDepth() < 2) || deepArray || noNestedArrays(value1)),
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(cloneArray(value2, true))));
        if (!canBeArray) {
          // however, even if CanBeArray is not present, identical arrays should still match
          AssertJUnit.assertEquals(
            "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
            value1 == value2,
            pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
        }
      }
    }
  }

  /**
   * Verify that doEquals honors the @{@link DeepArray} annotation.
   * @param type
   * @param canBeArray
   * @param deepArray
   */
  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArrayAsObjectEquals(Type type, boolean canBeArray, boolean deepArray) {
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
        AssertJUnit.assertEquals(
          "value1: " + possibleArrayToList(value1) + ", value2: " + possibleArrayToList(value2),
          value1 == value2 && (value1 == null || canBeArray || deepArray) && (deepArray || (noNestedArrays(value1))),
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(cloneArray(value2, true))));
      }
    }
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
