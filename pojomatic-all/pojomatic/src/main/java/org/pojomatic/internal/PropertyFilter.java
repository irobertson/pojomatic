package org.pojomatic.internal;

import java.util.Collections;
import java.util.Set;

import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;

public class PropertyFilter {
  private PropertyFilter() {}

  /**
   * Get the roles specified by a property policy and class policy.
   * @param elementPolicy the policy on the property ({@code null} if unspecified)
   * @param classPolicy the policy on the class ({@code null} if unspecified).
   * @return the roles specified by the policies.
   * @throws IllegalArgumentException if both {@code elementPolicy} and {@code classPolicy} are
   * {@code null}.
   */
  public static Set<PropertyRole> getRoles(
    PojomaticPolicy elementPolicy, DefaultPojomaticPolicy classPolicy) {
    if (elementPolicy != null) {
      Set<PropertyRole> roles = elementPolicy.getRoles();
      if (roles == null) { // this will be the case for PojomaticPolicy.DEFAULT
          return classPolicy != null ? classPolicy.getRoles() : PojomaticPolicy.ALL.getRoles();
      }
      else {
        return roles;
      }
    }
    else if(classPolicy != null) {
      return classPolicy.getRoles();
    }
    else {
      return Collections.emptySet();
    }
  }
}
