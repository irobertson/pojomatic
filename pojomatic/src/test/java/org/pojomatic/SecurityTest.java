package org.pojomatic;

import static org.testng.Assert.*;

import java.io.FilePermission;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.ReflectPermission;
import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;
import java.util.HashSet;
import java.util.Set;

import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.internal.PojomatorFactoryTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

public class SecurityTest {
  private SecurityManager originalSecurityManager;
  private Policy originalPolicy;

  private Set<Permission> requestedPermissions = new HashSet<>();

  private void setPolicy() {
    originalSecurityManager = System.getSecurityManager();
    originalPolicy = Policy.getPolicy();
    final ProtectionDomain testProtectionDomain = PojomatorFactoryTest.class.getProtectionDomain();
    final ProtectionDomain mainProtectionDomain = Pojomatic.class.getProtectionDomain();
    Policy.setPolicy(new Policy() {

      @Override
      public boolean implies(ProtectionDomain domain, Permission permission) {
        if (domain == mainProtectionDomain) {
          requestedPermissions.add(permission);
          return true;
        }
        if (permission instanceof SecurityPermission && "setPolicy".equals(permission.getName())) {
          return true;
        }
        if (permission instanceof RuntimePermission && "setSecurityManager".equals(permission.getName())) {
          return true;
        }
        if (testProtectionDomain.equals(domain)) {
          return false;
        }
        return true; // let TestNG do it's thing.
      }
    });

    System.setSecurityManager(new SecurityManager());
  }

  private void restorePolicy() {
    System.setSecurityManager(originalSecurityManager);
    Policy.setPolicy(originalPolicy);
  }

  private static class Inaccessible{}

  private static class SimplePojo {
    @Property
    int x = 0;

    @Property
    int getY() { return 0; }

    @Property
    Inaccessible z;
  }

  @Test
  public void testSecurityModel() {
    requestedPermissions.clear();
    SimplePojo pojo = new SimplePojo();
    String toString = null;
    boolean equals;
    int hashCode;
    try {
      setPolicy();
      equals = Pojomatic.pojomator(SimplePojo.class).doEquals(new SimplePojo(), new SimplePojo());
      hashCode = Pojomatic.pojomator(SimplePojo.class).doHashCode(new SimplePojo());
      toString = Pojomatic.pojomator(SimplePojo.class).doToString(pojo);
    }
    finally {
      restorePolicy();
    }
    assertEquals(toString, "SimplePojo{x: {0}, z: {null}, y: {0}}");
    assertTrue(equals);
    assertEquals(hashCode, 31*31*31);

    String testClassPath = SimplePojo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String simplePojoPath = SimplePojo.class.getName().replace('.', '/') + ".class";

    assertEquals(
      requestedPermissions,
      ImmutableSet.of(
        new FilePermission(testClassPath + simplePojoPath, "read"),
        new RuntimePermission(haveLookupDefineClass() ? "defineClass" : "accessDeclaredMembers"),
        new ReflectPermission("suppressAccessChecks")));
  }

  public static class AttackingConstructorFormatter extends DefaultEnhancedPropertyFormatter {
    {
      System.getSecurityManager().checkListen(80); // should fail
    }
  }

  private static class AttackingConstructorFormattedPojo {
    @Property
    @PropertyFormat(SecurityTest.AttackingConstructorFormatter.class)
    int x = 0;
  }


  /**
   * Verify that constructor code for property formatters is not running with Pojomatic security privileges
   */
  @Test
  public void testPropertyFormatterConstructorSecurity() {
    try {
      setPolicy();
      Pojomatic.pojomator(AttackingConstructorFormattedPojo.class);
      fail("Exception expected");
    }
    catch (AccessControlException e) {
      assertTrue(e.getPermission() instanceof SocketPermission);
    }
    finally {
      restorePolicy();
    }
  }

  public static class AttackingStaticInitializerFormatter extends DefaultEnhancedPropertyFormatter {
    static {
      System.getSecurityManager().checkListen(80); // should fail
    }
  }

  private static class AttackingStaticInitializerFormattedPojo {
    @Property
    @PropertyFormat(SecurityTest.AttackingStaticInitializerFormatter.class)
    int x = 0;
  }

  /**
   * Verify that constructor code for property formatters is not running with Pojomatic security privileges
   */
  @Test
  public void testPropertyFormatterStaticInitializerSecurity() {
    try {
      setPolicy();
      Pojomatic.pojomator(AttackingStaticInitializerFormattedPojo.class);
      fail("Exception expected");
    }
    catch (ExceptionInInitializerError e) {
      assertEquals(e.getCause().getClass(), AccessControlException.class);
      assertTrue(((AccessControlException) e.getCause()).getPermission() instanceof SocketPermission);
    }
    finally {
      restorePolicy();
    }
  }

  private static boolean haveLookupDefineClass() {
    try {
      MethodHandles.Lookup.class.getMethod("defineClass", new Class<?>[] { byte[].class });
      return true;
    }
    catch (Throwable t) {
      return false;
    }
  }
}
