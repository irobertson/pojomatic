package org.pojomatic;

import org.pojomatic.internal.PropertyAccessor;
import org.pojomatic.internal.PropertyField;

public class TestUtils {

  public static PropertyElement field(Class<?> clazz, String fieldName) throws Exception {
    return new PropertyField(clazz.getDeclaredField(fieldName), "");
  }

  public static PropertyElement method(Class<?> clazz, String methodName) throws Exception {
    return new PropertyAccessor(clazz.getDeclaredMethod(methodName), "");
  }

}
