package org.pojomatic.diff;

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

}
