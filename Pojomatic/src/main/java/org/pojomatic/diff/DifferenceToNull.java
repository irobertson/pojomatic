package org.pojomatic.diff;

/**
 * The result of comparing a non-null object to {@code null}.
 * @see DifferenceFromNull
 */
public final class DifferenceToNull extends AbstractNullDifference {

  public DifferenceToNull(Object instance) {
    super(instance);
  }

  @Override
  public String toString() {
    return "the object {" + instance + "} is different than null";
  }
}
