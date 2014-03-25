package org.pojomatic;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.internal.PojomatorFactory;

@AutoProperty(autoDetect=AutoDetectPolicy.METHOD)
public class Bean {
  private String string;
  private int i;
  private Integer integer;
  private int[] ints;
  private List<String> strings;

  private static Pojomator<Bean> asmPojomator = PojomatorFactory.makePojomator(Bean.class);

  public String getString() {
    return string;
  }
  public void setString(String string) {
    this.string = string;
  }
  public int getI() {
    return i;
  }
  public void setI(int i) {
    this.i = i;
  }
  public Integer getInteger() {
    return integer;
  }
  public void setInteger(Integer integer) {
    this.integer = integer;
  }
  public int[] getInts() {
    return ints;
  }
  public void setInts(int[] ints) {
    this.ints = ints;
  }
  public List<String> getStrings() {
    return strings;
  }
  public void setStrings(List<String> strings) {
    this.strings = strings;
  }

  public boolean pmequals(Object other) {
    return Pojomatic.equals(this, other);
  }

  public int pmHashCode() {
    return Pojomatic.hashCode(this);
  }

  private Pojomator<Bean> POJOMATOR = Pojomatic.pojomator(Bean.class);

  public boolean pmFastequals(Object other) {
    return POJOMATOR.doEquals(this, other);
  }

  public int pmFastHashCode() {
    return POJOMATOR.doHashCode(this);
  }

  public int asmHashCode() {
    return asmPojomator.doHashCode(this);
  }

  @Override public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + i;
    result = prime * result + ((integer == null)
        ? 0
        : integer.hashCode());
    result = prime * result + Arrays.hashCode(ints);
    result = prime * result + ((string == null)
        ? 0
        : string.hashCode());
    result = prime * result + ((strings == null)
        ? 0
        : strings.hashCode());
    return result;
  }

  @Override public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final Bean other = (Bean) obj;
    if (i != other.i)
      return false;
    if (integer == null) {
      if (other.integer != null)
        return false;
    }
    else if (!integer.equals(other.integer))
      return false;
    if (!Arrays.equals(ints, other.ints))
      return false;
    if (string == null) {
      if (other.string != null)
        return false;
    }
    else if (!string.equals(other.string))
      return false;
    if (strings == null) {
      if (other.strings != null)
        return false;
    }
    else if (!strings.equals(other.strings))
      return false;
    return true;
  }


}
