package org.pojomatic;

import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.pojomatic.formatter.PropertyFormatter;

public class Pojomatic<T> {
  private static final PropertyFormatter DEFAULT_PROPERTY_FORMATTER = new DefaultPropertyFormatter();
  private static final PojoFormatter DEFAULT_FORMATTER = new DefaultPojoFormatter();

  private final Class<T> pojoClass;
  private final PojoFormatter classFormatter;
  private final PropertyFormatter propertyFormatter;

  public Pojomatic(Class<T> pojoClass) {
    this(pojoClass, DEFAULT_FORMATTER, DEFAULT_PROPERTY_FORMATTER);
  }

  public Pojomatic(Class<T> pojoClass, PojoFormatter classFormatter, PropertyFormatter propertyFormatter) {
    this.pojoClass = pojoClass;
    this.classFormatter = classFormatter;
    this.propertyFormatter = propertyFormatter;

    //TODO initialize PropertyElements
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
}
