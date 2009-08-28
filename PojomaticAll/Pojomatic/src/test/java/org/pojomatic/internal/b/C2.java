package org.pojomatic.internal.b;

import org.pojomatic.internal.a.C1;

@SuppressWarnings("all")
public class C2 extends C1 {
  int packagePrivate() { return 1; }
  protected int packagePrivateOverriddenProtected() { return 2; }
  public int packagePrivateOverriddenPublic() { return 2; }
  protected int protectedMethod() { return 2; }
  public int publicMethod() { return 2; }
}
