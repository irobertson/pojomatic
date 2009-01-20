package org.pojomatic.diff;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Difference {
  private final String propertyName;
  private final Object lhs;
  private final Object rhs;

  public Difference(String propertyName, Object lhs, Object rhs) {
    this.propertyName = propertyName;
    this.lhs = lhs;
    this.rhs = rhs;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public Object getLhs() {
    return lhs;
  }

  public Object getRhs() {
    return rhs;
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
    return propertyName + ": " + lhs + " versus " + rhs;
  }

}
