package org.pojomatic.internal;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.AssertJUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;

public class MatrixTest {

  @DataProvider(name = "types")
  public static Object[][] types() {
    Object[][] result = new Object[Type.values().length][1];
    int i = 0;
    for (Type type: Type.values()) {
      result[i++][0] = type;
    }
    return result;
  }

  @DataProvider(name = "typesAndValues")
  public static Object[][] typesAndValues() {
    List<Object[]> result = new ArrayList<>();
    for (Type type: Type.values()) {
      for (Object value: type.sampleValues) {
        result.add(new Object[] { type, value });
      }
    }
    return result.toArray(new Object[0][0]);
  }

  @Test(dataProvider = "types")
  public void testHashCode1(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    for (Object value: type.sampleValues) {
      AssertJUnit.assertEquals(
        "value: " + value,
        31 + Objects.hashCode(value), pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "typesAndValues")
  public void testHashCode2(Type type, Object value) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    AssertJUnit.assertEquals(
      31 + Objects.hashCode(value), pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
  }

  @Test
  public void testHashCode() {
    for (Type type: Type.values()) {
      PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
      for (Object value: type.sampleValues) {
        AssertJUnit.assertEquals(
          "type: " + type.clazz + ", value: " + value,
          31 + Objects.hashCode(value), pojoFactory.pojomator().doHashCode(pojoFactory.create(value)));
      }
    }
  }

  @Test
  public void testToString() {
    for (Type type: Type.values()) {
      PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
      for (Object value: type.sampleValues) {
        AssertJUnit.assertEquals(
          "type: " + type.clazz + ", value: " + value,
          "Pojo{x: {" + Objects.toString(value) + "}}", pojoFactory.pojomator().doToString(pojoFactory.create(value)));
      }
    }
  }

  @Test(dataProvider = "types")
  public void testToString1(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    for (Object value: type.sampleValues) {
      AssertJUnit.assertEquals(
        "value: " + value,
        "Pojo{x: {" + Objects.toString(value) + "}}", pojoFactory.pojomator().doToString(pojoFactory.create(value)));
    }
  }

  @Test(dataProvider = "typesAndValues")
  public void testToString2(Type type, Object value) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    AssertJUnit.assertEquals(
      "Pojo{x: {" + Objects.toString(value) + "}}", pojoFactory.pojomator().doToString(pojoFactory.create(value)));
  }

  @Test
  public void testEquals() {
    for (Type type: Type.values()) {
      PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
      for (Object value1: type.sampleValues) {
        for (Object value2: type.sampleValues) {
          AssertJUnit.assertEquals(
            "type: " + type.clazz + ", value1: " + value1 + ", value2: " + value2,
            value1 == value2,
            pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
        }
        AssertJUnit.assertFalse(
          "type: " + type.clazz + ", value1: " + value1,
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), null));
      }
    }
  }

  @Test(dataProvider = "types")
  public void testEquals1(Type type) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    for (Object value1: type.sampleValues) {
      for (Object value2: type.sampleValues) {
        AssertJUnit.assertEquals(
          "value1: " + value1 + ", value2: " + value2,
          value1 == value2,
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
      }
      AssertJUnit.assertFalse(
        "type: " + type.clazz + ", value1: " + value1,
        pojoFactory.pojomator().doEquals(pojoFactory.create(value1), null));
    }
  }

  @Test(dataProvider = "typesAndValues")
  public void testEquals2(Type type, Object value1) {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
    for (Object value2: type.sampleValues) {
      AssertJUnit.assertEquals(
        "value2: " + value2,
        value1 == value2,
        pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
    }
    AssertJUnit.assertFalse(
      "type: " + type.clazz + ", value1: " + value1,
      pojoFactory.pojomator().doEquals(pojoFactory.create(value1), null));
  }

}
