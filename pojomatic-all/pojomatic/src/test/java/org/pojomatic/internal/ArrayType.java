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
}
