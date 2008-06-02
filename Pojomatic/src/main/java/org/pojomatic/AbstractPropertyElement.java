package org.pojomatic;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;

public abstract class AbstractPropertyElement<T, E extends AccessibleObject>
implements PropertyElement<T> {
  protected final E element;

  protected AbstractPropertyElement(E element) {
    element.setAccessible(true);
    this.element = element;
  }

  /* (non-Javadoc)
   * @see org.pojomatic.PropertyElement#getValue(T)
   */
  public Object getValue(T instance) {
    if (instance == null) {
      throw new NullPointerException("Instance is null: cannot get property value");
    }
    else {
      try {
        return accessValue(instance);
      }
      catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected abstract Object accessValue(T instance) throws IllegalAccessException, IllegalArgumentException;

  /* (non-Javadoc)
   * @see org.pojomatic.PropertyElement#getElement()
   */
  public AnnotatedElement getElement() {
    return this.element;
  }
}
