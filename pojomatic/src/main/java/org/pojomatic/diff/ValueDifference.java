package org.pojomatic.diff;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class ValueDifference implements Difference {
  @Property
  private final String propertyName;

  @Property
  private final Object leftValue;

  @Property
  private final Object rightValue;

  public ValueDifference(String propertyName, Object lhs, Object rhs) {
    this.propertyName = propertyName;
    this.leftValue = lhs;
    this.rightValue = rhs;
  }

  @Override
  public String propertyName() {
    return propertyName;
  }

  @Override
  public Object leftValue() {
    return leftValue;
  }

  @Override
  public Object rightValue() {
    return rightValue;
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
