package examples;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.AccountNumberFormatter;

@AutoProperty
public class Customer {
  private final String firstName;

  private final String lastName;

  @PropertyFormat(AccountNumberFormatter.class)
  private final String accountNumber;

  public Customer(String accountNumber, String firstName, String lastName) {
    this.accountNumber = accountNumber;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public String getAccountNumber() { return accountNumber; }

  @Override public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override public String toString() {
    return Pojomatic.toString(this);
  }

  @Override public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }

  public static void main(String[] args) {
    System.out.println(new Customer("12345", "Joe", "Blow"));
  }
}
