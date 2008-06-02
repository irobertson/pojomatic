package org.pojomatic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyAccessor<T> extends AbstractPropertyElement<T, Method> {
  public PropertyAccessor(Method element) {
    super(element);
  }

  @Override
  protected Object accessValue(T instance)
  throws IllegalArgumentException, IllegalAccessException {
    try {
      return element.invoke(instance, (Object[])null);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
