package org.pojomatic.internal.factory;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.lang.reflect.Field;

import org.pojomatic.annotations.Property;

public class PojoFactoryTest {
  @Test
  public void testFactory() throws Exception {
    PojoFactory pojoFactory = new PojoFactory(new PojoDescriptor(new PropertyDescriptor(int.class)));
    Object pojo = pojoFactory.create().with("x", 4).pojo();
    Field field = pojo.getClass().getDeclaredField("x");
    field.setAccessible(true);
    assertEquals(field.get(pojo), (Object) 4);
    assertTrue(field.isAnnotationPresent(Property.class));

    assertEquals(pojoFactory.pojomator().doHashCode(pojo), 31 + 4);
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
    assertEquals(pojoFactory.pojomator().doToString(pojo), "bar{x: {foo}, x: {4}}");
  }
}
