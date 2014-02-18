package org.pojomatic.internal;

import java.util.List;

public interface Type {

  /**
   * @return the class represented by this type
   */
  Class<?> getClazz();

  /**
   * @return a list of sample values for the type
   */
  List<Object> getSampleValues();

  /**
   * @return the number of array levels in this type. 0 for primitives or non-array classes, 1 for arrays thereof, etc.
   */
  int arrayDepth();

  /**
   * Compute the hashCode for a value of this type. Do not recursively process arrays
   * @param value
   * @return
   */
  int hashCode(Object value);

  /**
   * Compute the hashCode for a value of this type, recursing into arrays as needed.
   * @param value
   * @return
   */
  int deepHashCode(Object value);

  /**
   * Compute the desired toString representation for a value of this type.
   * @param value
   * @return
   */
  String toString(Object value);

  /**
   * Compute the desired toString representation for a value of this type, recursing into arrays as needed.
   * @param value
   * @return
   */
  String deepToString(Object value);
}
