package org.pojomatic.diff;

import java.util.NoSuchElementException;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

/**
 * A {@link Difference} which only has a value on the left.
 */
public final class OnlyOnLeft implements Difference {

  private final String propertyName;

  private final Object leftValue;

  public OnlyOnLeft(String propertyName, Object leftValue) {
    if (propertyName == null) {
      throw new NullPointerException("Property name cannot be null");
    }
    this.propertyName = propertyName;
    this.leftValue = leftValue;
  }

  @Property
  public String propertyName() {
    return propertyName;
  }

  @Property
  public Object leftValue() {
    return leftValue;
  }

  public boolean existsOnLeft() {
    return true;
  }

  public boolean existsOnRight() {
    return false;
  }

  public Object rightValue() throws NoSuchElementException {
    throw new NoSuchElementException("Value only exists on the left");
  }

  @Override
  public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override
  public String toString() {
    return Pojomatic.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }

}
