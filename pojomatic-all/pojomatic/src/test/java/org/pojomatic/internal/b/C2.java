package org.pojomatic.internal.b;

import org.pojomatic.annotations.Property;
import org.pojomatic.internal.a.C1;

@SuppressWarnings("all")
public class C2 extends C1 {
  @Property int packagePrivate() { return 1; }
  @Property protected int packagePrivateOverriddenProtected() { return 2; }
  @Property public int packagePrivateOverriddenPublic() { return 2; }
  @Property protected int protectedMethod() { return 2; }
  @Property public int publicMethod() { return 2; }
}
