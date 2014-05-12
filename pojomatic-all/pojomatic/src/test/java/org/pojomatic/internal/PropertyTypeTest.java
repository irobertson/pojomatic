package org.pojomatic.internal;

import static org.testng.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.SkipArrayCheck;
import org.pojomatic.diff.Difference;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;

public class PropertyTypeTest {

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testHashCode(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.getClazz())));
    for (Object value: type.getSampleValues()) {
      checkHashCode(pojoFactory, value, type.hashCode(value));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectHashCode(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(skipArrayCheck))));
    for (Object value: type.getSampleValues()) {
      int propertyHashCode = skipArrayCheck
        ? Objects.hashCode(value)
        : type.deepHashCode(value);

      checkHashCode(pojoFactory, value, propertyHashCode);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayHashCode(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(skipArrayCheck))));
    for (Object value: type.getSampleValues()) {
      checkHashCode(pojoFactory, value, type.deepHashCode(value));
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
  public void testArrayAsObjectToString(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(skipArrayCheck))));
    for (Object value: type.getSampleValues()) {
      String expectedPropertyValue = skipArrayCheck ? Objects.toString(value) : type.deepToString(value);
      checkToString(pojoFactory, value, expectedPropertyValue);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayToString(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(skipArrayCheck))));
    for (Object value: type.getSampleValues()) {
      checkToString(pojoFactory, value, type.deepToString(value));
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

  @Test(dataProvider = "annotations", dataProviderClass = TypeProviders.class)
  public void testMixedTypesAsObjectEqualsAndDiff(boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(skipArrayCheck))));
    Iterable<Type> allTypes =
      Iterables.concat(Arrays.asList(BaseType.OBJECT), TypeProviders.simpleArrays(), TypeProviders.doubleArrays());
    List<Object> allValues = new ArrayList<>();
    for (Type type: allTypes) {
      allValues.addAll(type.getSampleValues());
    }
    // Ideally, this would be a data provider. However, as there are 90 different possible values, and we're doing a
    // self-cartesian product, it would add 8100 test cases, and just be a pain. Instead, delegate the real work
    // to a sub method, so that if we have problems, we can do a drop-to-frame in that method to diagnose.
    for (Object value1: allValues) {
      Object pojo1 = pojoFactory.create(value1);
      for (Object value2: allValues) {
        testMixedTypesAsObjectEqualsAndDiffWorker(skipArrayCheck, pojoFactory, value1, pojo1, value2);
      }
    }
  }

  private void testMixedTypesAsObjectEqualsAndDiffWorker(boolean skipArrayCheck,
    PojoFactory pojoFactory, Object value1, Object pojo1, Object value2) {
    Object value2PossibleClone = maybeCloneObject(value2);
    Object pojo2 = pojoFactory.create(value2PossibleClone);;
    boolean expectedToBeEqual;
    if (value1 == value2PossibleClone) {
      expectedToBeEqual = true;
    }
    else if (value1 == null || value2 == null) {
      expectedToBeEqual = false;
    }
    else {
      Class<?> type1 = value1.getClass();
      Class<?> type2 = value2.getClass();
      if (type1.equals(type2)) {
        if (! type1.isArray()) {
          expectedToBeEqual = value1.equals(value2PossibleClone);
        }else if (!skipArrayCheck) {
          expectedToBeEqual = value1 == value2;  // FIXME - is this right?
        }
        else {
          expectedToBeEqual = false;
        }
      }
      else {
        expectedToBeEqual = false;
      }
    }
    checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2PossibleClone, pojo1, pojo2);
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectEqualsAndDiff(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(skipArrayCheck))));
    for (Object value1: type.getSampleValues()) {
      Object pojo1 = pojoFactory.create(value1);
      for (Object value2: type.getSampleValues()) {
        // Equality of different arrays should only be detected if SkipArrayCheck is not present
        // Note that in this test, we only clone the inner array if skipArrayCheck is false.
        boolean expectedToBeEqual = (value1 == value2) && (value1 == null || (!skipArrayCheck));
        Object pojo2 = pojoFactory.create(cloneArray(value2, !skipArrayCheck));
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
        if (skipArrayCheck) {
          // however, even if SkipArrayCheck is mpresent, identical arrays should still match
          checkEqualsAndDiff(value1 == value2, pojoFactory, value1, value2, pojo1, pojoFactory.create(value2));
        }
      }
      assertFalse(
        pojoFactory.pojomator().doEquals(pojo1, null),
        "type: " + type.getClazz() + ", value1: " + value1);
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsArrayEqualsAndDiff(Type type, boolean skipArrayCheck) { // skipArrayCheck shouldn't matter here
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(skipArrayCheck))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        boolean expectedToBeEqual = (value1 == value2);
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
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
  public void testDeepArrayAsObjectEqualsAndDiff(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object.class, extraAnnotations(skipArrayCheck))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // equality of different arrays should only be detected if SkipArrayCheck is not present
        // If value1 != value 2, then doEquals should always return false.
        // If value1 == value2, but skipArrayCheck is false, then the only way doEquals can return true is if value1 == null
        boolean expectedToBeEqual = (value1 == value2) && (value1 == null || ! skipArrayCheck);
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
   * @param deepArray - likewise - deep arrays should be detected
   */
  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArrayAsArrayEquals(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(type.getClazz(), extraAnnotations(skipArrayCheck))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // If value1 != value 2, then doEquals should always return false.
        // If value1 == value2, but canBeArray is false, then the only way doEquals can return true is if value1 == null
        // If value1 == value2 != null and canBeArray is true, then without deepArray, they can only be equal if
        //  there are no second level arrays that can be cloned.
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        boolean expectedToBeEqual = value1 == value2;
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
      }
    }
  }

  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArraysAsShallowArraysEqualsAndDiff(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object[].class, extraAnnotations(skipArrayCheck))));
    for (Object value1: type.getSampleValues()) {
      for (Object value2: type.getSampleValues()) {
        // If value1 != value 2, then doEquals should always return false.
        // If value1 == value2, but canBeArray is false, then the only way doEquals can return true is if value1 == null
        // If value1 == value2 != null and canBeArray is true, then without deepArray, they can only be equal if
        //  there are no second level arrays that can be cloned.
        Object pojo1 = pojoFactory.create(value1);
        Object pojo2 = pojoFactory.create(cloneArray(value2, true));
        boolean expectedToBeEqual = value1 == value2;
        checkEqualsAndDiff(expectedToBeEqual, pojoFactory, value1, value2, pojo1, pojo2);
      }
    }
  }

  @Test(dataProvider = "deepArrayTypes", dataProviderClass = TypeProviders.class)
  public void testDeepArraysAsShallowArraysToString(Type type, boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object[].class, extraAnnotations(skipArrayCheck))));
    for (Object value: type.getSampleValues()) {
      checkToString(pojoFactory, value, type.deepToString(value));
    }
  }

  @Test(dataProvider = "annotations", dataProviderClass = TypeProviders.class)
  public void testMixedTypesAsObjectArrayEqualsAndDiff(boolean skipArrayCheck) {
    PojoFactory pojoFactory = new PojoFactory(
      new PojoDescriptor(new PropertyDescriptor(Object[].class, extraAnnotations(skipArrayCheck))));
    Iterable<Type> allTypes =
      Iterables.concat(Arrays.asList(new ArrayType(BaseType.OBJECT)), TypeProviders.doubleArrays());
    List<Object> allValues = new ArrayList<>();
    for (Type type: allTypes) {
      allValues.addAll(type.getSampleValues());
    }
    // Ideally, this would be a data provider. However, as there are 90 different possible values, and we're doing a
    // self-cartesian product, it would add 8100 test cases, and just be a pain. Instead, delegate the real work
    // to a sub method, so that if we have problems, we can do a drop-to-frame in that method to diagnose.
    for (Object value1: allValues) {
      Object pojo1 = pojoFactory.create(value1);
      for (Object value2: allValues) {
        testMixedTypesAsObjectEqualsAndDiffWorker(false, pojoFactory, value1, pojo1, value2);
      }
    }
  }


  @SuppressWarnings("unchecked")
  private Class<? extends Annotation>[] extraAnnotations(boolean skipArrayCheck) {
    List<Class<? extends Annotation>> classes = new ArrayList<>();
    if (skipArrayCheck) {
      classes.add(SkipArrayCheck.class);
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
    if (pojoFactory.pojomator().doEquals(pojo1, pojo2) != expectedToBeEqual)
      assertEquals(pojoFactory.pojomator().doEquals(pojo1, pojo2), expectedToBeEqual, label(value1, value2));
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

  private String label(Object value1, Object value2) {
    return "value1: " + labelString(value1) + ", value2: " + labelString(value2);
  }

  private String label(Object value) {
    return "value: " + labelString(value);
  }

  private String labelString(Object value) {
    if (value == null) {
      return "null";
    }
    else {
      return possibleArrayToList(value) + "(" + value.getClass().getName() + ")";
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
      result.add(possibleArrayToList(Array.get(value, i)));
    }
    return result;
  }

  private Object maybeCloneObject(Object object) {
    if (object == null) {
      return null;
    }
    if (object.getClass().isArray()) {
      return cloneArray(object, true);
    }
    else if (object instanceof String) {
      return new String((String) object);
    }
    else {
      return object;
    }
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
