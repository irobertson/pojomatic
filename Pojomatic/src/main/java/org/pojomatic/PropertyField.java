package org.pojomatic;

import java.lang.reflect.Field;

public class PropertyField extends AbstractPropertyElement<Field> {
  public PropertyField(Field propertyField) {
    super(propertyField);
  }

  @Override
  protected Object accessValue(Object instance)
  throws IllegalArgumentException, IllegalAccessException {
    return element.get(instance);
  }
}
