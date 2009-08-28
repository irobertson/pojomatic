package org.pojomatic.diff;

/**
 * The result of comparing {@code null} to a non-null object.
 * @see DifferenceToNull
 */
public final class DifferenceFromNull extends AbstractNullDifference {

  public DifferenceFromNull(Object other) {
    super(other);
  }

  @Override
  public String toString() {
    return "null is different than the object {" + instance + "}";
  }

}
