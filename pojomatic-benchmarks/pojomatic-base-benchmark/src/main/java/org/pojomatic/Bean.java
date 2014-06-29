package org.pojomatic;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty(autoDetect=AutoDetectPolicy.FIELD)
public class Bean {
  private int i;
  private String string;
  private Integer integer;
  private int[] ints;
  private List<String> strings;

  public int getI() {
    return i;
  }
  public void setI(int i) {
    this.i = i;
  }
  public String getString() {
    return string;
  }
  public void setString(String string) {
    this.string = string;
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

  private final static Pojomator<Bean> POJOMATOR = Pojomatic.pojomator(Bean.class);

  public boolean pmFastequals(Object other) {
    return POJOMATOR.doEquals(this, other);
  }

  public int pmFastHashCode() {
    return POJOMATOR.doHashCode(this);
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

  public static boolean doEquals(Bean left, Object right) {
    if (left == right)
      return true;
    if (right == null)
      return false;
    if (left.getClass() != right.getClass())
      return false;
    final Bean other = (Bean) right;
    if (left.i != other.i)
      return false;
    if (left.integer == null) {
      if (other.integer != null)
        return false;
    }
    else if (!left.integer.equals(other.integer))
      return false;
    if (!Arrays.equals(left.ints, other.ints))
      return false;
    if (left.string == null) {
      if (other.string != null)
        return false;
    }
    else if (!left.string.equals(other.string))
      return false;
    if (left.strings == null) {
      if (other.strings != null)
        return false;
    }
    else if (!left.strings.equals(other.strings))
      return false;
    return true;
  }

  public boolean indirectEquals(Object obj) {
    return doEquals(this, obj);
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
