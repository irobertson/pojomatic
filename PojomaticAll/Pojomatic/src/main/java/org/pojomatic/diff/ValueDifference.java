package org.pojomatic.diff;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class ValueDifference implements Difference {
  private final String propertyName;
  private final Object leftValue;
  private final Object rightValue;

  public ValueDifference(String propertyName, Object lhs, Object rhs) {
    this.propertyName = propertyName;
    this.leftValue = lhs;
    this.rightValue = rhs;
  }

  @Property
  public String propertyName() {
    return propertyName;
  }

  @Property
  public Object leftValue() {
    return leftValue;
  }

  @Property
  public Object rightValue() {
    return rightValue;
  }

  public boolean existsOnLeft() {
    return true;
  }

  public boolean existsOnRight() {
    return true;
  }

  @Override
  public boolean equals(Object obj) {
    return Pojomatic.equals(this, obj);
  }

  @Override
  public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override
  public String toString() {
    //TODO - can we do better here?
    return propertyName + ": {" + leftValue + "} versus {" + rightValue + "}";
  }

}
