package org.pojomatic.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * A mutable set of methods which can be overridden.  All methods are assumed to take no arguments 
 * and either public, protected or package private.
 */
class OverridableMethods {
  /**
   * Check a method to see if it is not an override; if not, add it to the collection of methods
   * @param method the method to check and maybe add.  It is assumed the method is not private.
   * @return {@code true} if the method is not an override
   */
  boolean checkAndMaybeAddMethod(Method method) {
    if (isPackagePrivate(method)) {
      // This can only override another package private method
      return packageMethods.add(new PackageMethod(method));
    }
    else {
      // If there is a public method already declared, then this is an override.  Otherwise,
      // we need to track it as a public override going forward, even if it is overriding a
      // superclass method which was declared package private.
      return publicOrProtectedMethods.add(method.getName()) 
      && !packageMethods.contains(new PackageMethod(method));
    }
  }
  
  /**
   * A bean to track the package and name of a package-private method
   */
  private class PackageMethod {
    PackageMethod(Method method) {
      name = method.getName();
      pakage = method.getDeclaringClass().getPackage();
    }
    
    String name;
    Package pakage;
    @Override public int hashCode() {
      return name.hashCode() * 31 + pakage.hashCode();
    }
    
    @Override public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj instanceof PackageMethod) {
        PackageMethod other = (PackageMethod)obj;
        return name.equals(other.name) && pakage.equals(other.pakage);
      }
      else {
        return false;
      }
    }
  }
  
  private Set<String> publicOrProtectedMethods = new HashSet<String>();
  private Set<PackageMethod> packageMethods = new HashSet<PackageMethod>();
  
  private static boolean isPackagePrivate(Method method) {
    return !(Modifier.isPublic(method.getModifiers()) 
        || Modifier.isProtected(method.getModifiers()));
  }

}
