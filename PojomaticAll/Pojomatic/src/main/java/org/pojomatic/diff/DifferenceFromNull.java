package org.pojomatic.diff;

/**
 * The result of comparing {@code null} to a non-null object.
 * @see DifferenceToNull
 */
public final class DifferenceFromNull extends AbstractNullDifference {
  private final Iterable<OnlyOnRight> differences;

  public DifferenceFromNull(Object other, Iterable<OnlyOnRight> differences) {
    super(other);
    if (differences == null) {
      throw new NullPointerException("Differences cannot be null");
    }
    this.differences = differences;
  }

  @Override
  public String toString() {
    return "null is different than the object {" + instance + "}";
  }

  public Iterable<? extends Difference> differences() {
    return differences;
  }

}
