package examples;

public class Implementation implements Interface {
  private final String name;

  public Implementation(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override public int hashCode() {
    return POJOMATOR.doHashCode(this);
  }

  @Override public boolean equals(Object other) {
    return POJOMATOR.doEquals(this, other);
  }

  @Override public String toString() {
    return POJOMATOR.doToString(this);
  }
}
