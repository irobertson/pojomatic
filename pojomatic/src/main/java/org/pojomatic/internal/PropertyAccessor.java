package org.pojomatic.internal;

import java.lang.reflect.Method;

public class PropertyAccessor extends AbstractPropertyElement<Method> {
  private final static String GET = "get", IS = "is";

  public PropertyAccessor(Method method, String name) {
    super(method, name.length() == 0 ? getName(method) : name);
  }

  private static String getName(Method method) {
    String methodName = method.getName();
    if (isPrefixedWith(methodName, GET)) {
      return decapitalize(methodName.substring(GET.length()));
    }
    else if (isBoolean(method.getReturnType()) && isPrefixedWith(methodName, IS)) {
      return decapitalize(methodName.substring(IS.length()));
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
  public String getElementName() {
    return element.getName();
  }

  @Override
  public String getType() {
    return "method";
  }

  @Override
  public Class<?> getPropertyType() {
    return element.getReturnType();
  }

  private static String decapitalize(String name) {
    if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                    Character.isUpperCase(name.charAt(0))){
        return name;
    }
    char chars[] = name.toCharArray();
    chars[0] = Character.toLowerCase(chars[0]);
    return new String(chars);
  }
}
