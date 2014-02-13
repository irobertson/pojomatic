package org.pojomatic.internal.factory;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Test;
import org.pojomatic.annotations.Property;

public class PojoFactoryTest {
  @Test
  public void testFactory() throws Exception {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(int.class)));
    Object pojo = pojoFactory.create().with("x", 4).pojo();
    Field field = pojo.getClass().getDeclaredField("x");
    field.setAccessible(true);
    assertEquals(4, field.get(pojo));
    assertTrue(field.isAnnotationPresent(Property.class));

    assertEquals(31 + 4, pojoFactory.pojomator().doHashCode(pojo));
  }

  @Test
  public void testParent() throws Exception {
    PojoDescriptor parent = new PojoDescriptor(new PropertyDescriptor(String.class));
    PojoClassFactory pojoClassFactory = new PojoClassFactory();
    pojoClassFactory.generateClass(parent);
    PojoFactory pojoFactory = new PojoFactory(
      pojoClassFactory,
      new PojoDescriptor(
        "foo",
        "bar",
        Access.PUBLIC,
        parent,
        new PropertyDescriptor(int.class)));
    Object pojo = pojoFactory.create().with("x", 4).withParent("x", "foo").pojo();
    assertEquals("bar{x: {foo}, x: {4}}", pojoFactory.pojomator().doToString(pojo));
  }
}
