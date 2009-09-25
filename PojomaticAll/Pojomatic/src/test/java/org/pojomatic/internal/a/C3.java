package org.pojomatic.internal.a;

import org.pojomatic.annotations.Property;
import org.pojomatic.internal.b.C2;

@SuppressWarnings("all")
public class C3 extends C2 {
  @Property int packagePrivate() { return 3; }
  @Property protected int packagePrivateOverriddenProtected() { return 3; }
  @Property public int packagePrivateOverriddenPublic() { return 3; }
  @Property protected int protectedMethod() { return 3; }
  @Property public int publicMethod() { return 3; }
}
