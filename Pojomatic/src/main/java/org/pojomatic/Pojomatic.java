package org.pojomatic;

import org.pojomatic.formatter.PojomaticFormatter;
import org.pojomatic.formatter.DefaultPojomaticFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PropertyFormatter;

public class Pojomatic<T> {
  private static final PropertyFormatter DEFAULT_PROPERTY_FORMATTER = new DefaultPropertyFormatter();
  private static final PojomaticFormatter DEFAULT_FORMATTER = new DefaultPojomaticFormatter();

  private final Class<T> pojoClass;
  private final PojomaticFormatter classFormatter;
  private final PropertyFormatter propertyFormatter;
  private final Iterable< PropertyElement<T> > propertyElements;

  public Pojomatic(Class<T> pojoClass) {
    this(pojoClass, DEFAULT_FORMATTER, DEFAULT_PROPERTY_FORMATTER);
  }

  public Pojomatic(Class<T> pojoClass, PojomaticFormatter classFormatter, PropertyFormatter propertyFormatter) {
    this.pojoClass = pojoClass;
    this.classFormatter = classFormatter;
    this.propertyFormatter = propertyFormatter;

    //TODO initialize PropertyElements
    this.propertyElements = null;
  }

  public int hashCode(T instance) {
    //TODO implement hashCode
    return 0;
  }

  public String toString(T instance) {
    //TODO implement toString
    return null;
  }

  public boolean equals(T instance, Object other) {
    //TODO implement equals
    return false;
  }

  public Iterable< PropertyElement<T> > getProperties() {
    if (propertyElements == null) {
      //lazily initialize
      synchronized (this) {
        if (propertyElements == null) {
          //TODO initialize properties
        }
      }
    }
    return propertyElements;
  }
}
