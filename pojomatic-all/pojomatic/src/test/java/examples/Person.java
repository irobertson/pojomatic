package examples;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Person {
  private final String firstName;
  private final String lastName;
  private final int age;

  public Person(String firstName, String lastName, int age) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }

  public String getLastName() { return this.lastName; }
  public String getFirstName() { return this.firstName; }
  public int getAge() { return this.age; }

  @Override public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override public String toString() {
    return Pojomatic.toString(this);
  }

  @Override public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }
}
