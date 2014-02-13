package org.pojomatic.internal.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.pojomatic.Pojomator;
import org.pojomatic.internal.PojomatorFactory;

public class PojoFactory {
  public static class PojoAssembler {
    private final Object pojo;

    public PojoAssembler(Object pojo) {
      this.pojo = pojo;
    }


    public PojoAssembler with(String propertyName, Object value) {
      Field field;
      try {
        field = pojo.getClass().getDeclaredField(propertyName);
      } catch (NoSuchFieldException | SecurityException e) {
        throw new RuntimeException(e);
      }
      field.setAccessible(true);
      try {
        field.set(pojo, value);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public PojoAssembler withParent(String propertyName, Object value) {
      Field field;
      try {
        field = pojo.getClass().getSuperclass().getDeclaredField(propertyName);
      } catch (NoSuchFieldException | SecurityException e) {
        throw new RuntimeException(e);
      }
      field.setAccessible(true);
      try {
        field.set(pojo, value);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      return this;
    }

    public Object pojo() {
      return pojo;
    }
  }

  private final PojoDescriptor pojoDescriptor;
  private final Class<?> pojoClass;
  private final Pojomator<Object> pojomator;

  public PojoFactory(PojoDescriptor pojoDescriptor) {
    this(new PojoClassFactory(), pojoDescriptor);
  }

  @SuppressWarnings("unchecked")
  public PojoFactory(PojoClassFactory classFactory, PojoDescriptor pojoDescriptor) {
    this.pojoDescriptor = pojoDescriptor;
    this.pojoClass = classFactory.generateClass(pojoDescriptor);
    this.pojomator = (Pojomator<Object>) PojomatorFactory.makePojomator(pojoClass);

  }

  public PojoAssembler create() {
    try {
      Constructor<?> constructor = pojoClass.getConstructor();
      constructor.setAccessible(true);
      return new PojoAssembler(constructor.newInstance());
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public Object create(Object value) {
    if (pojoDescriptor.properties.size() != 1) {
      throw new IllegalArgumentException("expected one property, found " + pojoDescriptor.properties.size());
    }
    return create().with(pojoDescriptor.properties.get(0).name, value).pojo();
  }

  public Pojomator<Object> pojomator() {
    return pojomator;
  }

}
