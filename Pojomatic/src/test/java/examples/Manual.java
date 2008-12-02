package examples;

import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty //all fields are included by default
public class Manual {
  private String firstName, lastName;

  public Manual(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }


  public String getFirstName() { return this.firstName; }
  public String getLastName() { return this.lastName; }

  private final static Pojomator<Manual> POJOMATOR = Pojomatic.pojomator(Manual.class);

  @Override public boolean equals(Object other) {
    return POJOMATOR.doEquals(this, other);
  }

  @Override public int hashCode() {
    return POJOMATOR.doHashCode(this);
  }

  @Override public String toString() {
    return POJOMATOR.doToString(this);
  }

  public static void main(String[] args) {
    System.out.println(new Manual("first", "last"));
  }
}
