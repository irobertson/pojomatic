package org.pojomatic;

import java.lang.reflect.AnnotatedElement;

public class PropertyElement<T> {
  private AnnotatedElement element;

  public Object getValue(T instance) {
    //TODO get the value on demand
    return null;
  }

  public AnnotatedElement getElement() {
    return this.element;
  }

  public void setElement(AnnotatedElement element) {
    this.element = element;
  }
}
