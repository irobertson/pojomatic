package org.pojomatic.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
  public int deepHashCode(Object value) {
    if (value == null) {
      return 0;
    }
    else {
      int hash = 1;
      for (Object element: arrayToList(value)) {
        hash = hash*31 + componentType.deepHashCode(element);
      }
      return hash;
    }
  }

  @Override
  public String toString(Object value) {
    if (value == null) {
      return "null";
    }
    else {
      ArrayList<String> strings = new ArrayList<>();
      for (Object element: arrayToList(value)) {
        if (componentType instanceof ArrayType) {
          strings.add(Objects.toString(element));
        }
        else {
          strings.add(componentType.toString(element));
        }
      }
      return strings.toString();
    }
  }

  @Override
  public String deepToString(Object value) {
    if (value == null) {
      return "null";
    }
    else {
      ArrayList<String> strings = new ArrayList<>();
      for (Object element: arrayToList(value)) {
        strings.add(componentType.deepToString(element));
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

  @Override
  public int arrayDepth() {
    return componentType.arrayDepth() + 1;
  }
}
