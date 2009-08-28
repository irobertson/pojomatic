package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;
import org.pojomatic.internal.a.C1;
import org.pojomatic.internal.a.C3;
import org.pojomatic.internal.b.C2;
import org.pojomatic.internal.b.C4;


public class OverridableMethodsTest {

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
  
  private void checkMethod(
    String methodName, boolean c1Add, boolean c2Add, boolean c3Add, boolean c4Add) 
  throws Exception {
    OverridableMethods overridableMethods = new OverridableMethods();
    assertEquals(c1Add, overridableMethods.checkAndMaybeAddMethod(method(C1.class, methodName)));
    assertEquals(c2Add, overridableMethods.checkAndMaybeAddMethod(method(C2.class, methodName)));
    assertEquals(c3Add, overridableMethods.checkAndMaybeAddMethod(method(C3.class, methodName)));
    assertEquals(c4Add, overridableMethods.checkAndMaybeAddMethod(method(C4.class, methodName)));
  }
  
  private static Method method(Class<?> clazz, String name) throws Exception {
    return clazz.getDeclaredMethod(name);
  }
}
