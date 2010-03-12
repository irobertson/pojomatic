package org.pojomatic.diff;

import java.util.NoSuchElementException;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

/**
 * A {@link Difference} which only has a value on the right.
 */
public final class OnlyOnRight implements Difference {

  private final String propertyName;

  private final Object rightValue;

  public OnlyOnRight(String propertyName, Object rightValue) {
    if (propertyName == null) {
      throw new NullPointerException("Property name cannot be null");
    }
    this.propertyName = propertyName;
    this.rightValue = rightValue;
  }

  @Property
  public String propertyName() {
    return propertyName;
  }

  public Object leftValue() {
    throw new NoSuchElementException("Value only exists on the right");
  }

  public boolean existsOnLeft() {
    return false;
  }

  public boolean existsOnRight() {
    return true;
  }

  @Property
  public Object rightValue() throws NoSuchElementException {
    return rightValue;
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
