package org.pojomatic.diff;

import java.util.Collections;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class PropertyDifferences implements Differences {
  @Property
  private final List<Difference> differences;

  /**
   * @param differences cannot be {@code null} or empty
   * @throws NullPointerException if {@code differences} is {@code null}
   * @throws IllegalArgumentException if {@code differences.isEmpty()} is {@code true}
   */
  public PropertyDifferences(List<Difference> differences) {
    if (differences == null) {
      throw new NullPointerException("list of differences is null");
    }
    if (differences.isEmpty()) {
      throw new IllegalArgumentException("list of differences is empty");
    }
    this.differences = Collections.unmodifiableList(differences);
  }

  public List<Difference> differences() {
    return differences;
  }

  public boolean areEqual() {
    return false;
  }

  @Override
  public String toString() {
    return differences.toString();
  }

  @Override
  public boolean equals(Object other) {
    return Pojomatic.equals(this, other);
  }

  @Override
  public int hashCode() {
    return Pojomatic.hashCode(this);
  }
}
