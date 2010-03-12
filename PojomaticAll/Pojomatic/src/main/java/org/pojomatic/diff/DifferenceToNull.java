package org.pojomatic.diff;

/**
 * The result of comparing a non-null object to {@code null}.
 * @see DifferenceFromNull
 */
public final class DifferenceToNull extends AbstractNullDifference {
  private final Iterable<OnlyOnLeft> differences;

  public DifferenceToNull(Object instance, Iterable<OnlyOnLeft> differences) {
    super(instance);
    if (differences == null) {
      throw new NullPointerException("Differences cannot be null");
    }
    this.differences = differences;
  }

  @Override
  public String toString() {
    return "the object {" + instance + "} is different than null";
  }

  public Iterable<? extends Difference> differences() {
    return differences;
  }
}
