package org.pojomatic.diff;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

/**
 * A {@link Differences} whose {@link Differences#toString()} is generated exactly once.
 */
@AutoProperty(policy=DefaultPojomaticPolicy.NONE)
abstract class AbstractNullDifference implements Differences {
  @Property(policy=PojomaticPolicy.ALL)
  protected final Object instance;

  /**
   * @param instance the non-null object being compared to {@code null}.
   * @throws NullPointerException if {@code instance} is {@code null}.
   */
  public AbstractNullDifference(Object instance) {
    if (instance == null) {
      throw new NullPointerException("Instance cannot be null");
    }

    this.instance = instance;
  }

  public final boolean areEqual() {
    return false;
  }

  @Override
  public abstract String toString();

  @Override
  public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }

}
