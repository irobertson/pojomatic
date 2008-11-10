package org.pojomatic.annotations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.internal.PropertyRole;

/**
 * A policy for defining which sets of {@link Pojomatic} operations
 * ({@code equals}, {@code hashCode} and {@code toString}) should use all properties by default.
 * This is set class-wide using {@link AutoProperty}.
 * @see PojomaticPolicy
 */
public enum DefaultPojomaticPolicy {

  /**
   * Use all properties for both {@code hashCode} and {@code equals} by default.
   * Anything included in {@code public int hashCode()} should also be included in
   * {@code public boolean equals(Object)} to preserve the general
   * contract of {@link Object#hashCode()}.
   *
   * @see Object#hashCode()
   * @see Object#equals(Object)
   */
  HASHCODE_EQUALS(PropertyRole.HASH_CODE, PropertyRole.EQUALS),

  /**
   * Use all properties for {@code equals} only by default.
   *
   * @see Object#equals(Object)
   */
  EQUALS(PropertyRole.EQUALS),

  /**
   * Use all properties for both {@code toString} only by default.
   *
   * @see Object#toString()
   */
  TO_STRING(PropertyRole.TO_STRING),

  /**
   * Use all properties for {@code hashCode}, {@code equals} and {@code toString} by default.
   */
  ALL(PropertyRole.EQUALS, PropertyRole.HASH_CODE, PropertyRole.TO_STRING),

  /**
   * Do not use any properties for any of {@code hashCode}, {@code equals} or {@code toString}
   * by default.
   */
  NONE();

  /**
   * @return the roles this specified by this policy.
   */
  public Set<PropertyRole> getRoles() {
    return roles;
  }

  private DefaultPojomaticPolicy(PropertyRole... roles) {
    Set<PropertyRole> roleSet = EnumSet.noneOf(PropertyRole.class);
    for (PropertyRole role: roles) {
      roleSet.add(role);
    }
    this.roles = Collections.unmodifiableSet(roleSet);
  }

  private final Set<PropertyRole> roles;
}
