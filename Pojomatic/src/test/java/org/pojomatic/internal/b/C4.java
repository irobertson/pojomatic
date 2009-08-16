package org.pojomatic.internal.b;

import org.pojomatic.internal.a.C3;

@SuppressWarnings("all")
public class C4 extends C3 {
  int packagePrivate() { return 4; }
  protected int packagePrivateOverriddenProtected() { return 4; }
  public int packagePrivateOverriddenPublic() { return 4; }
  protected int protectedMethod() { return 4; }
  public int publicMethod() { return 4; }
}
