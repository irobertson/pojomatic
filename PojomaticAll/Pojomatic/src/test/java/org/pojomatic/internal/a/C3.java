package org.pojomatic.internal.a;

import org.pojomatic.internal.b.C2;

@SuppressWarnings("all")
public class C3 extends C2 {
  int packagePrivate() { return 3; }
  protected int packagePrivateOverriddenProtected() { return 3; }
  public int packagePrivateOverriddenPublic() { return 3; }
  protected int protectedMethod() { return 3; }
  public int publicMethod() { return 3; }
}
