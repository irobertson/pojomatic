package org.pojomatic;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class PropertyElement<T> {
  private final AnnotatedElement element;

  /**
   * A {@code PropertyElement} can either be a field or a method
   */
  private final boolean isField;

  public PropertyElement(Field propertyField) {
    this(propertyField, true);
  }

  public PropertyElement(Method propertyAccessor) {
    this(propertyAccessor, false);
  }

  private PropertyElement(AnnotatedElement element, boolean isField) {
    this.element = element;
    this.isField = isField;
  }

  public Object getValue(T instance) {
    if (instance == null) {
      throw new NullPointerException("Instance is null: cannot get property value");
    }

    if (isField) {
      Field field = (Field)getElement();
      field.setAccessible(true);
      try {
        return field.get(instance);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      Method method = (Method)getElement();
      try {
        return method.invoke(instance, (Object[])null);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  public AnnotatedElement getElement() {
    return this.element;
  }
}
