package org.pojomatic;

import java.lang.reflect.Field;

public class PropertyField<T> extends AbstractPropertyElement<T, Field> {
  public PropertyField(Field propertyField) {
    super(propertyField);
  }

  @Override
  protected Object accessValue(T instance) throws IllegalArgumentException, IllegalAccessException {
    return element.get(instance);
  }
}
