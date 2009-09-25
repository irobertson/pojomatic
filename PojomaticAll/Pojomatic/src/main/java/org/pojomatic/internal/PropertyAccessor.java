package org.pojomatic.internal;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyAccessor extends AbstractPropertyElement<Method> {
  private final static String GET = "get", IS = "is";

  public PropertyAccessor(Method method, String name) {
    super(method, name.length() == 0 ? getName(method) : name);
  }

  private static String getName(Method method) {
    String methodName = method.getName();
    if (isPrefixedWith(methodName, GET)) {
      return Introspector.decapitalize(methodName.substring(GET.length()));
    }
    else if (isBoolean(method.getReturnType()) && isPrefixedWith(methodName, IS)) {
      return Introspector.decapitalize(methodName.substring(IS.length()));
    }
    else {
      return methodName;
    }
  }

  private static boolean isBoolean(Class<?> clazz) {
    return Boolean.class.equals(clazz) || Boolean.TYPE.equals(clazz);
  }

  private static boolean isPrefixedWith(String name, String prefix) {
    return name.length() > prefix.length()
    && name.startsWith(prefix)
    && Character.isUpperCase(name.charAt(prefix.length()));
  }

  @Override
  protected Object accessValue(Object instance)
  throws IllegalArgumentException, IllegalAccessException {
    try {
      return element.invoke(instance, (Object[])null);
    }
    catch (InvocationTargetException e) {
      throw new RuntimeException(e.getCause());
    }
  }
}
