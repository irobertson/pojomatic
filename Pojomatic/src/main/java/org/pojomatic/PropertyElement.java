package org.pojomatic;

import java.lang.reflect.AnnotatedElement;

public interface PropertyElement<T> {

  Object getValue(T instance);

  AnnotatedElement getElement();

}
