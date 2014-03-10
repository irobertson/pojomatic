package examples;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PropertyFormat;

@AutoProperty
public class Customer {
  private final String firstName;

  private final String lastName;

  @PropertyFormat(IpAddressFormatter.class)
  private final byte[] ipAddress;

  public Customer(String firstName, String lastName, byte[] ipAddress) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.ipAddress = ipAddress;
  }

  public String getFirstName() { return firstName; }
  public String getLastName() { return lastName; }
  public byte[] getIpAddress() { return ipAddress; }

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
    System.out.println(new Customer("Joe", "Blow", new byte[] {127, 0, 0, 1}));
  }
}
