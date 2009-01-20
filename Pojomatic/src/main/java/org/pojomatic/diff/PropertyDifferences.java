package org.pojomatic.diff;

import java.util.Collections;
import java.util.List;

public class PropertyDifferences implements Differences {
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

  public boolean areEqual() {
    return differences.isEmpty();
  }
}
