package org.pojomatic;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.Pojomatic;
import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.internal.ClassProperties;

@AutoProperty(autoDetect=AutoDetectPolicy.METHOD)
public class Bean {
  private String string;
  private int i;
  private Integer integer;
  private int[] ints;
  private List<String> strings;

  private static PropertyElement getInteger, getString, getI, getInts, getStrings;
  
  static {
    setUpPojomaticHandrolled();
  }
  
  private static void setUpPojomaticHandrolled() {
    ClassProperties classProperties = new ClassProperties(Bean.class);
    for (PropertyElement prop: classProperties.getHashCodeProperties()) {
      if ("integer".equals(prop.getName())) {
        getInteger = prop;
      }
      else if ("string".equals(prop.getName())) {
        getString = prop;
      }
      else if ("i".equals(prop.getName())) {
        getI = prop;
      }
      else if ("ints".equals(prop.getName())) {
        getInts = prop;
      }
      else if ("strings".equals(prop.getName())) {
        getStrings = prop;
      }
      else {
        throw new RuntimeException("unexpected property: " + prop);
      }
    }
    System.out.println(classProperties.getHashCodeProperties());
  }
  
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
  
  public int handRolledPmHashCode() {
    int hashCode = 1;
    hashCode = 31 * hashCode + hashCodeOfValue(getInteger.getValue(this)); 
    hashCode = 31 * hashCode + hashCodeOfValue(getString.getValue(this)); 
    hashCode = 31 * hashCode + hashCodeOfValue(getI.getValue(this)); 
    hashCode = 31 * hashCode + hashCodeOfValue(getInts.getValue(this)); 
    hashCode = 31 * hashCode + hashCodeOfValue(getStrings.getValue(this)); 
    return hashCode;
  }
  
  public boolean handRolledPmEquals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Bean)) {
      return false;
    }

    return areValuesEqual(getInteger.getValue(this), getInteger.getValue(other))
     && areValuesEqual(getString.getValue(this), getString.getValue(other))
     && areValuesEqual(getI.getValue(this), getI.getValue(other))
     && areValuesEqual(getInts.getValue(this), getInts.getValue(other))
     && areValuesEqual(getStrings.getValue(this), getStrings.getValue(other));
  }
  
private static boolean areValuesEqual(Object instanceValue, Object otherValue) {
  if (instanceValue == null) {
    if (otherValue != null) {
      return false;
    }
  }
  else { // instanceValue is not null
    if (otherValue == null) {
      return false;
    }
    if (!instanceValue.getClass().isArray()) {
      if (!instanceValue.equals(otherValue)) {
        return false;
      }
    }
    else {
      if (!otherValue.getClass().isArray()) {
        return false;
      }
      Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();
      Class<?> otherComponentClass = otherValue.getClass().getComponentType();

      if (!instanceComponentClass.isPrimitive()) {
        if (otherComponentClass.isPrimitive()) {
          return false;
        }
        if (!Arrays.deepEquals((Object[]) instanceValue, (Object[]) otherValue)) {
          return false;
        }
      }
      else { // instanceComponentClass is primative
        if (otherComponentClass != instanceComponentClass) {
          return false;
        }

        if (Boolean.TYPE == instanceComponentClass) {
          if(!Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue)) {
            return false;
          }
        }
        else if (Byte.TYPE == instanceComponentClass) {
          if (! Arrays.equals((byte[]) instanceValue, (byte[]) otherValue)) {
            return false;
          }
        }
        else if (Character.TYPE == instanceComponentClass) {
          if(!Arrays.equals((char[]) instanceValue, (char[]) otherValue)) {
            return false;
          }
        }
        else if (Short.TYPE == instanceComponentClass) {
          if(!Arrays.equals((short[]) instanceValue, (short[]) otherValue)) {
            return false;
          }
        }
        else if (Integer.TYPE == instanceComponentClass) {
          if(!Arrays.equals((int[]) instanceValue, (int[]) otherValue)) {
            return false;
          }
        }
        else if (Long.TYPE == instanceComponentClass) {
          if(!Arrays.equals((long[]) instanceValue, (long[]) otherValue)) {
            return false;
          }
        }
        else if (Float.TYPE == instanceComponentClass) {
          if(!Arrays.equals((float[]) instanceValue, (float[]) otherValue)) {
            return false;
          }
        }
        else if (Double.TYPE == instanceComponentClass) {
          if(!Arrays.equals((double[]) instanceValue, (double[]) otherValue)) {
            return false;
          }
        }
        else {
          // should NEVER happen
          throw new IllegalStateException(
            "unknown primative type " + instanceComponentClass.getName());
        }
      }
    }
  }
  return true;
}

  private static int hashCodeOfValue(Object value) {
    if (value == null) {
      return 0;
    }
    else {
      if (value.getClass().isArray()) {
        Class<?> instanceComponentClass = value.getClass().getComponentType();
        if (! instanceComponentClass.isPrimitive()) {
          return Arrays.hashCode((Object[]) value);
        }
        else {
          if (Boolean.TYPE == instanceComponentClass) {
            return Arrays.hashCode((boolean[]) value);
          }
          else if (Byte.TYPE == instanceComponentClass) {
            return  Arrays.hashCode((byte[]) value);
          }
          else if (Character.TYPE == instanceComponentClass) {
            return Arrays.hashCode((char[]) value);
          }
          else if (Short.TYPE == instanceComponentClass) {
            return Arrays.hashCode((short[]) value);
          }
          else if (Integer.TYPE == instanceComponentClass) {
            return Arrays.hashCode((int[]) value);
          }
          else if (Long.TYPE == instanceComponentClass) {
            return Arrays.hashCode((long[]) value);
          }
          else if (Float.TYPE == instanceComponentClass) {
            return Arrays.hashCode((float[]) value);
          }
          else if (Double.TYPE == instanceComponentClass) {
            return Arrays.hashCode((double[]) value);
          }
          else {
            // should NEVER happen
            throw new IllegalStateException(
              "unknown primative type " + instanceComponentClass.getName());
          }
        }
      }
      else {
        return value.hashCode();
      }
    }
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
