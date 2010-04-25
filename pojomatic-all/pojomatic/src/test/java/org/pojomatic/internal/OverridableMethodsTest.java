package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.EnumSet;

import org.junit.Test;
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
    assertEquals(
      EQUALS, overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, "publicMethod"), EQUALS));
    assertEquals(
      EnumSet.noneOf(PropertyRole.class), overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EQUALS));
  }

  @Test
  public void testEqualsThenToString() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(
      EQUALS, overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, "publicMethod"), EQUALS));
    assertEquals(
      EnumSet.of(PropertyRole.TO_STRING), overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EnumSet.of(PropertyRole.TO_STRING)));
  }

  @Test
  public void testEqualsThenToEqualsString() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(
      EQUALS, overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, "publicMethod"), EQUALS));
    assertEquals(
      EnumSet.of(PropertyRole.TO_STRING), overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EnumSet.of(PropertyRole.TO_STRING, PropertyRole.EQUALS)));
  }

  @Test
  public void testEqualsHashCodeThenEqualsHashCode() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(
      EQUALS_HASH_CODE, overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, "publicMethod"), EQUALS_HASH_CODE));
    assertEquals(
      EnumSet.noneOf(PropertyRole.class), overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EQUALS_HASH_CODE));
  }

  @Test
  public void testEqualsThenEqualsHashCode() throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(
      EQUALS, overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, "publicMethod"), EQUALS));
    try {
      overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, "publicMethod"), EQUALS_HASH_CODE);
      fail("Exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(
        "Method org.pojomatic.internal.b.C2.publicMethod is requested to be included in hashCode"
          + " computations, but already overrides a method which is requested for"
          + " equals computations, but not hashCode computations.", e.getMessage());
    }
  }

  private void checkMethod(
    String methodName, boolean c1Add, boolean c2Add, boolean c3Add, boolean c4Add)
    throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(
      c1Add, !overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C1.class, methodName), ALL).isEmpty());
    assertEquals(
      c2Add, !overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C2.class, methodName), ALL).isEmpty());
    assertEquals(
      c3Add, !overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C3.class, methodName), ALL).isEmpty());
    assertEquals(
      c4Add, !overridableMethods.checkAndMaybeAddRolesToMethod(
        method(C4.class, methodName), ALL).isEmpty());
  }
  
  private static Method method(Class<?> clazz, String name) throws Exception {
    return clazz.getDeclaredMethod(name);
  }
}
