package org.pojomatic.test;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

/**
 * Simple one-object container for use in tests.
 */
@AutoProperty
public class DifferentPojo {

  private final Object test;

  public DifferentPojo(Object test) {
    this.test = test;
  }

  public Object getTest() {
    return test;
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
