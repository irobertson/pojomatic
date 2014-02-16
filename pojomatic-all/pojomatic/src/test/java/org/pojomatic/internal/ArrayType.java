package org.pojomatic.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayType implements Type {
  private final Type componentType;
  private final List<Object> sampleValues;

  public ArrayType(Type componentType) {
    this.componentType = componentType;
    sampleValues = new ArrayList<>();
    sampleValues.add(null);
    for (int size = 0; size < componentType.getSampleValues().size(); size++) {
      Object array = Array.newInstance(componentType.getClazz(), size);
      for (int i = 0; i < size; i++) {
        Array.set(array, i, componentType.getSampleValues().get(i));
      }
      sampleValues.add(array);
    }
  }

  @Override
  public Class<?> getClazz() {
    return Array.newInstance(componentType.getClazz(), 0).getClass();
  }

  @Override
  public List<Object> getSampleValues() {
    return sampleValues;
  }

  @Override
  public String toString() {
    return componentType.toString() + "[]";
  }

  @Override
  public int hashCode(Object value) {
    return value == null ? 0 : arrayToList(value).hashCode();
  }

  @Override
  public String toString(Object value) {
    if (value == null) {
      return "null";
    }
    else {
      ArrayList<String> strings = new ArrayList<>();
      for (Object element: arrayToList(value)) {
        strings.add(componentType.toString(element));
      }
      return strings.toString();
    }
  }

  private static List<?> arrayToList(Object array) {
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < Array.getLength(array); i++) {
      result.add(Array.get(array, i));
    }
    return result;
  }
}
