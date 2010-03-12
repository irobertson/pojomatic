package org.pojomatic.diff;

import java.util.Collections;

public final class NoDifferences implements Differences {
  private static final NoDifferences INSTANCE = new NoDifferences();

  private NoDifferences() {
  }

  public boolean areEqual() {
    return true;
  }

  @Override
  public String toString() {
    return "no differences";
  }

  public static NoDifferences getInstance() {
    return INSTANCE;
  }

  public Iterable<Difference> differences() {
    return Collections.emptyList();
  }

}
