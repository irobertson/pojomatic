package org.pojomatic.annotations;

/**
 * A policy for determining which class members are automatically detected as properties.  This
 * policy is set class-wide using {@link AutoProperty}.
 */
public enum AutoDetectPolicy {
  /**
   *    Auto-detect fields of the class as properties
   */
  FIELD,

  /**
   *    Auto-detect accessor methods of the class as properties using the JavaBean conventions
   *    (i.e. getX and isX).
   */
  METHOD,

  /**
   *    Do not auto-detect properties for the class. This is be useful to specify
   *    a different {@link PojomaticPolicy} in {@link AutoProperty} without enabling
   *    property auto-detection.
   */
  NONE
}
