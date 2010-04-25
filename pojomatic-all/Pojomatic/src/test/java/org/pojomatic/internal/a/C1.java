package org.pojomatic.internal.a;

import org.pojomatic.annotations.Property;

public class C1 {
  @Property int packagePrivate() { return 1; }
  @Property int packagePrivateOverriddenProtected() { return 1; }
  @Property int packagePrivateOverriddenPublic() { return 1; }
  @Property protected int protectedMethod() { return 1; }
  @Property public int publicMethod() { return 1; }
}
