package org.pojomatic.diff;

import java.util.Collections;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.Property;

public class PropertyDifferences implements Differences {
  @Property
  private final List<Difference> differences;

  public PropertyDifferences(List<Difference> differences) {
    if (differences == null) {
      throw new NullPointerException("list of differences is null");
    }
    this.differences = Collections.unmodifiableList(differences);
  }

  public List<Difference> getDifferences() {
    return differences;
  }

  @Override
  public String toString() {
    return differences.isEmpty() ? "no differences" : differences.toString();
  }

  @Override public boolean equals(Object other) { return Pojomatic.equals(this, other); }
  @Override public int hashCode() { return Pojomatic.hashCode(this); }

  public boolean areEqual() {
    return differences.isEmpty();
  }
}
