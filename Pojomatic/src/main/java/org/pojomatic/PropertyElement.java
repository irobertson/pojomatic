package org.pojomatic;

import java.lang.reflect.AnnotatedElement;

/**
 * A "property" on a class.  In this context, all a property is is a means of obtaining a value from
 * an instance.
 */
public interface PropertyElement {

  /**
   * Get the value held by this property from the given instance.
   *
   * @param instance the instance to get the value from
   * @return the value held by the instance
   */
  Object getValue(Object instance);

  /**
   * Get the original annotated element that this property is derived from.
   * @return the original annotated element that this property is derived from.
   */
  AnnotatedElement getElement();

}
