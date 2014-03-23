package org.pojomatic;

import static org.testng.Assert.*;

import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.SecurityPermission;

import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.internal.PojomatorFactoryTest;
import org.testng.annotations.Test;

public class SecurityTest {
  private SecurityManager originalSecurityManager;
  private Policy originalPolicy;

  private void setPolicy() {
    originalSecurityManager = System.getSecurityManager();
    originalPolicy = Policy.getPolicy();
    Policy.setPolicy(new Policy() {
      private final ProtectionDomain testProtectionDomain = PojomatorFactoryTest.class.getProtectionDomain();
      @Override
      public boolean implies(ProtectionDomain domain, Permission permission) {
        if (permission instanceof SecurityPermission && "setPolicy".equals(permission.getName())) {
          return true;
        }
        if (permission instanceof RuntimePermission && "setSecurityManager".equals(permission.getName())) {
          return true;
        }
        if (domain == testProtectionDomain) {
          return false;
        }
        return true;
      }
    });

    System.setSecurityManager(new SecurityManager());
  }

  private void restorePolicy() {
    System.setSecurityManager(originalSecurityManager);
    Policy.setPolicy(originalPolicy);
  }

  @AutoProperty
  private static class SimplePojo {
    @SuppressWarnings("unused")
    int x = 0;
  }

  @Test
  public void testSecurityModel() {
    SimplePojo pojo = new SimplePojo();
    String toString = null;
    try {
      setPolicy();
      toString = Pojomatic.pojomator(SimplePojo.class).doToString(pojo);
    }
    finally {
      restorePolicy();
    }
    assertEquals(toString, "SimplePojo{x: {0}}");
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
      e.printStackTrace();
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
      e.printStackTrace();
    }
    finally {
      restorePolicy();
    }
  }
}
