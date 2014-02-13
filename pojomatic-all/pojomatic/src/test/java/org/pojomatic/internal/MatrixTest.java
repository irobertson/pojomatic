package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Test;
import org.pojomatic.internal.factory.PojoDescriptor;
import org.pojomatic.internal.factory.PojoFactory;
import org.pojomatic.internal.factory.PropertyDescriptor;

public class MatrixTest {

  @Test
  public void testHashCode() {
    for (Type type: Type.values()) {
      PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
      for (Object value: type.sampleValues) {
        assertEquals(
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
        assertEquals(
          "type: " + type.clazz + ", value: " + value,
          "Pojo{x: {" + Objects.toString(value) + "}}", pojoFactory.pojomator().doToString(pojoFactory.create(value)));
      }
    }
  }

  @Test
  public void testEquals() {
    for (Type type: Type.values()) {
      PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(type.clazz)));
      for (Object value1: type.sampleValues) {
        for (Object value2: type.sampleValues) {
          assertEquals(
            "type: " + type.clazz + ", value1: " + value1 + ", value2: " + value2,
            value1 == value2,
            pojoFactory.pojomator().doEquals(pojoFactory.create(value1), pojoFactory.create(value2)));
        }
        assertFalse(
          "type: " + type.clazz + ", value1: " + value1,
          pojoFactory.pojomator().doEquals(pojoFactory.create(value1), null));
      }
    }
  }

}
