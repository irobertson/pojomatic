package org.pojomatic;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;

public abstract class AbstractPropertyElement<E extends AccessibleObject>
implements PropertyElement {
  protected final E element;

  protected AbstractPropertyElement(E element) {
    element.setAccessible(true);
    this.element = element;
  }

  /* (non-Javadoc)
   * @see org.pojomatic.PropertyElement#getValue(T)
   */
  public Object getValue(Object instance) {
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

  protected abstract Object accessValue(Object instance)
  throws IllegalAccessException, IllegalArgumentException;

  /* (non-Javadoc)
   * @see org.pojomatic.PropertyElement#getElement()
   */
  public AnnotatedElement getElement() {
    return this.element;
  }

  @Override
  public int hashCode() {
    return element.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final AbstractPropertyElement<?> other = (AbstractPropertyElement<?>) obj;
    return element.equals(other.element);
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
