package org.pojomatic.annotations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.pojomatic.Pojomatic;
import org.pojomatic.internal.PropertyRole;

/**
 * A policy for defining which sets of {@link Pojomatic} operations
 * ({@code equals}, {@code hashCode} and {@code toString}) should a property.
 * This is set using {@link Property}.
 * @see DefaultPojomaticPolicy
 */
public enum PojomaticPolicy {

  /**
   * Use the property for both {@code hashCode} and {@code equals}.
   * Anything included in {@code public int hashCode()} should also be included in
   * {@code public boolean equals(Object)} to preserve the general
   * contract of {@link Object#hashCode()}.
   *
   * @see Object#hashCode()
   * @see Object#equals(Object)
   */
  HASHCODE_EQUALS(PropertyRole.HASH_CODE, PropertyRole.EQUALS),

  /**
   * Use the property for both {@code equals} and {@code toString}.
   *
   * @see Object#equals(Object)
   * @see Object#toString()
   */
  EQUALS_TO_STRING(PropertyRole.EQUALS, PropertyRole.TO_STRING),


  /**
   * Use the property for {@code equals} only.
   *
   * @see Object#equals(Object)
   */
  EQUALS(PropertyRole.EQUALS),

  /**
   * Use the property for both {@code toString} only.
   *
   * @see Object#toString()
   */
  TO_STRING(PropertyRole.TO_STRING),

  /**
   * Use the property for {@code hashCode}, {@code equals} and {@code toString}.
   */
  ALL(PropertyRole.EQUALS, PropertyRole.HASH_CODE, PropertyRole.TO_STRING),

  /**
   * Do not use the property for any of {@code hashCode}, {@code equals} or {@code toString}.
   */
  NONE(),

  /**
   * Use the default policy specified via the {@code @AutoProperty} annotation, or
   * {@code ALL} if none was specified.
   */
  DEFAULT {
    @Override public Set<PropertyRole> getRoles() {
      return null;
    }
  };


  /**
   * @return the roles this specified by this policy.  Will be {@code null} for {@code DEFAULT}.
   */
  public Set<PropertyRole> getRoles() {
    return roles;
  }

  private PojomaticPolicy(PropertyRole... roles) {
    Set<PropertyRole> roleSet = EnumSet.noneOf(PropertyRole.class);
    for (PropertyRole role: roles) {
      roleSet.add(role);
    }
    this.roles = Collections.unmodifiableSet(roleSet);
  }

  private final Set<PropertyRole> roles;
}
