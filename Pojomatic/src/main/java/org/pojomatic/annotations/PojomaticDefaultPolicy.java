package org.pojomatic.annotations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.internal.PropertyRole;

/**
 * Defines which sets of {@link Pojomatic} operations that properties should be included.
 * This are set class-wide using {@link AutoProperty} and for
 * an individual property using {@link Property}.
 */
public enum PojomaticDefaultPolicy {

  /**
   * Anything included in {@code public int hashCode()} should also be included in
   * {@code public boolean equals(Object)} to preserve the general
   * contract of {@link Object#hashCode()}.
   *
   * @see Object#hashCode()
   * @see Object#equals(Object)
   */
  HASHCODE_EQUALS(PropertyRole.HASH_CODE, PropertyRole.EQUALS),

  /**
   * {@code public boolean equals(Object)}
   *
   * @see Object#equals(Object)
   */
  EQUALS(PropertyRole.EQUALS),

  /**
   * {@code public String toString()}
   *
   * @see Object#toString()
   */
  TO_STRING(PropertyRole.TO_STRING),

  /**
   * Shorthand for all of the above.
   */
  ALL(PropertyRole.EQUALS, PropertyRole.HASH_CODE, PropertyRole.TO_STRING),

  /**
   * Shorthand for none of the above.
   */
  NONE();

  /**
   * @return the roles this specified by this policy.
   */
  public Set<PropertyRole> getRoles() {
    return roles;
  }

  private PojomaticDefaultPolicy(PropertyRole... roles) {
    Set<PropertyRole> roleSet = EnumSet.noneOf(PropertyRole.class);
    for (PropertyRole role: roles) {
      roleSet.add(role);
    }
    this.roles = Collections.unmodifiableSet(roleSet);
  }

  private final Set<PropertyRole> roles;
}
