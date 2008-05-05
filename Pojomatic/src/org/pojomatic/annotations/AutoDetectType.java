package org.pojomatic.annotations;

public enum AutoDetectType {
  /**
   *    Auto-detect fields of the class as properties
   */
  FIELD,

  /**
   *    Auto-detect methods of the class as properties using the JavaBean conventions (i.e. getX).
   */
  METHOD,

  /**
   *    Do not auto-detect properties for the class. This is be useful to specify
   *    a different {@link PojomaticPolicy} in {@link AutoProperty} without enabling
   *    property auto-detection.
   */
  NONE;
}
