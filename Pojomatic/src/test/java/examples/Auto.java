package examples;

import static org.pojomatic.annotations.PojomaticPolicy.NONE;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.Property;

@AutoProperty //all fields are included by default
public class Auto {
  private boolean test; //included by default

  @Property(policy=NONE)
  private int exlude;

  @Property //include a method as well, even though it does not follow the getX convention
  public String derived() {
    return String.valueOf(System.currentTimeMillis());
  }


  @Override public int hashCode() {
    return Pojomatic.hashCode(this);
  }

  @Override public String toString() {
    return Pojomatic.toString(this);
  }

  @Override public boolean equals(Object o) {
    return Pojomatic.equals(this, o);
  }

  public boolean isTest() {
    return test;
  }

  public void setTest(boolean test) {
    this.test = test;
  }

  public int getExlude() {
    return exlude;
  }

  public void setExlude(int exlude) {
    this.exlude = exlude;
  }

}
