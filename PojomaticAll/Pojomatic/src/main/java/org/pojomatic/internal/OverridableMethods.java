package org.pojomatic.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A mutable set of methods which can be overridden.  All methods are assumed to take no arguments
 * and either public, protected or package private.
 */
class OverridableMethods {

  /**
   * Check to see if roles should be added to a method, and add them if so.  Only roles not already
   * on the method will be added.  If {@code method} already has an
   * {@link PropertyRole#EQUALS EQUALS} role, and it is requested to add the
   * {@link PropertyRole#HASH_CODE HASH_CODE} role, an {@link IllegalArgumentException} will be
   * thrown.
   *
   * @param method the method to check
   * @param newRoles the roles to add
   * @return the roles which were actually added.
   * @throws IllegalArgumentException if {@code method} already has an
   * {@link PropertyRole#EQUALS EQUALS} role, and it is requested to add the
   * {@link PropertyRole#HASH_CODE HASH_CODE} role
   */
  Set<PropertyRole> checkAndMaybeAddRolesToMethod(Method method, Set<PropertyRole> newRoles) {
    Set<PropertyRole> existingRoles = findExistingRoles(method);
    if (existingRoles.contains(PropertyRole.EQUALS)
      && !existingRoles.contains(PropertyRole.HASH_CODE)
      && newRoles.contains(PropertyRole.HASH_CODE)) {
      throw new IllegalArgumentException(
        "Method " + method.getDeclaringClass().getName() + "." + method.getName()
          + " is requested to be included in hashCode computations, but already overrides a method"
          + " which is requested for equals computations, but not hashCode computations.");
    }
    Set<PropertyRole> addedRoles = EnumSet.noneOf(PropertyRole.class);
    for (PropertyRole role : newRoles) {
      if (!existingRoles.contains(role)) {
        addedRoles.add(role);
        existingRoles.add(role);
      }
    }
    return addedRoles;
  }

  private Set<PropertyRole> findExistingRoles(Method method) {
    Set<PropertyRole> existingRoles;
    if (isPackagePrivate(method)) {
      // This can only override another package private method
      PackageMethod key = new PackageMethod(method);
      existingRoles = packageMethods.get(key);
      if (existingRoles == null) {
        existingRoles = EnumSet.noneOf(PropertyRole.class);
        packageMethods.put(key, existingRoles);
      }
    }
    else {
      // If there is a public method already declared, then this is an override.  Otherwise,
      // we need to track it as a public override going forward, even if it is overriding a
      // superclass method which was declared package private.
      existingRoles = publicOrProtectedMethods.get(method.getName());
      if (existingRoles == null) {
        existingRoles = packageMethods.get(new PackageMethod(method));
      }
      if (existingRoles == null) {
        existingRoles = EnumSet.noneOf(PropertyRole.class);
        publicOrProtectedMethods.put(method.getName(), existingRoles);
      }
    }
    return existingRoles;
  }

  /**
   * A bean to track the package and name of a package-private method
   */
  private static class PackageMethod {
    //TODO: Consider tracking the return type as well, to deal with byte-code-generated overloads
    // that have different return types, as well as return type narrowing in a subclass.
    PackageMethod(Method method) {
      name = method.getName();
      pakage = method.getDeclaringClass().getPackage();
    }

    String name;
    Package pakage;

    @Override
    public int hashCode() {
      return name.hashCode() * 31 + pakage.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj instanceof PackageMethod) {
        PackageMethod other = (PackageMethod) obj;
        return name.equals(other.name) && pakage.equals(other.pakage);
      }
      else {
        return false;
      }
    }
  }

  private final Map<String, Set<PropertyRole>> publicOrProtectedMethods =
    new HashMap<String, Set<PropertyRole>>();
  private final Map<PackageMethod, Set<PropertyRole>> packageMethods =
    new HashMap<PackageMethod, Set<PropertyRole>>();


  private static boolean isPackagePrivate(Method method) {
    return !(Modifier.isPublic(method.getModifiers())
      || Modifier.isProtected(method.getModifiers()));
  }

}
