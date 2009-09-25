package org.pojomatic.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

import org.pojomatic.PropertyElement;

public abstract class AbstractPropertyElement<E extends AccessibleObject & Member>
implements PropertyElement {
  protected final E element;
  private final String name;

  protected AbstractPropertyElement(E element, String name) {
    element.setAccessible(true);
    this.element = element;
    this.name = name;
  }

  public String getName() {
    return name;
  }

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

  public Class<?> getDeclaringClass() {
    return element.getDeclaringClass();
  }

  @Override
  public int hashCode() {
    return element.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final AbstractPropertyElement<?> other = (AbstractPropertyElement<?>) obj;
    return element.equals(other.element);
  }

  @Override
  public String toString() {
    return element.toString();
  }
}
