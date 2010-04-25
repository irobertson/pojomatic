package examples;

import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

@AutoProperty
public class Employee {
  private final String firstName;
  private final String lastName;

  @Property(policy=PojomaticPolicy.EQUALS_TO_STRING)
  private String securityLevel;


  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }


  public String getSecurityLevel() {
    return this.securityLevel;
  }

  public void setSecurityLevel(String securityLevel) {
    this.securityLevel = securityLevel;
  }

  public Employee(String firstName, String lastName, String securityLevel) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.securityLevel = securityLevel;
  }

}
