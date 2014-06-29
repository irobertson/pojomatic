package org.pojomatic.internal;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import java.lang.reflect.Method;
import java.util.EnumSet;

import org.pojomatic.internal.a.C1;
import org.pojomatic.internal.a.C3;
import org.pojomatic.internal.b.C2;
import org.pojomatic.internal.b.C4;

public class OverridableMethodsTest {
  private static final EnumSet<PropertyRole> EQUALS_HASH_CODE =
    EnumSet.of(PropertyRole.EQUALS, PropertyRole.HASH_CODE);
  private static final EnumSet<PropertyRole> EQUALS = EnumSet.of(PropertyRole.EQUALS);
  private static final EnumSet<PropertyRole> ALL = EnumSet.allOf(PropertyRole.class);

  @Test public void testPackagePrivate() throws Exception {
    checkMethod("packagePrivate", true, true, false, false);
  }

  @Test public void testPackagePrivateToProtected() throws Exception {
    checkMethod("packagePrivateOverriddenProtected", true, true, false, false);
  }

  @Test public void testPackagePrivateToPublic() throws Exception {
    checkMethod("packagePrivateOverriddenPublic", true, true, false, false);
  }

  @Test public void testProtected() throws Exception {
    checkMethod("protectedMethod", true, false, false, false);
  }

  @Test public void testPublic() throws Exception {
    checkMethod("publicMethod", true, false, false, false);
  }

  @Test
  public void testEqualsThenEquals() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, "publicMethod"), EQUALS), EQUALS);
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C2.class, "publicMethod"), EQUALS), EnumSet.noneOf(PropertyRole.class));
  }

  @Test
  public void testEqualsThenToString() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, "publicMethod"), EQUALS), EQUALS);
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C2.class, "publicMethod"), EnumSet.of(PropertyRole.TO_STRING)), EnumSet.of(PropertyRole.TO_STRING));
  }

  @Test
  public void testEqualsThenToEqualsString() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, "publicMethod"), EQUALS), EQUALS);
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C2.class, "publicMethod"), EnumSet.of(PropertyRole.TO_STRING, PropertyRole.EQUALS)), EnumSet.of(PropertyRole.TO_STRING));
  }

  @Test
  public void testEqualsHashCodeThenEqualsHashCode() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, "publicMethod"), EQUALS_HASH_CODE), EQUALS_HASH_CODE);
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C2.class, "publicMethod"), EQUALS_HASH_CODE), EnumSet.noneOf(PropertyRole.class));
  }

  @Test
  public void testEqualsThenEqualsHashCode() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, "publicMethod"), EQUALS), EQUALS);
    try {
      overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EQUALS_HASH_CODE);
      fail("Exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Method org.pojomatic.internal.b.C2.publicMethod is requested to be included in hashCode"
      + " computations, but already overrides a method which is requested for"
      + " equals computations, but not hashCode computations.");
    }
  }

  private void checkMethod(
    String methodName, boolean c1Add, boolean c2Add, boolean c3Add, boolean c4Add)
    throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(!overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C1.class, methodName), ALL).isEmpty(), c1Add);
    assertEquals(!overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C2.class, methodName), ALL).isEmpty(), c2Add);
    assertEquals(!overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C3.class, methodName), ALL).isEmpty(), c3Add);
    assertEquals(!overridableMethods.checkAndMaybeAddRolesToMethod(
    method(C4.class, methodName), ALL).isEmpty(), c4Add);
  }

  private static Method method(Class<?> clazz, String name) throws Exception {
    return clazz.getDeclaredMethod(name);
  }
}
