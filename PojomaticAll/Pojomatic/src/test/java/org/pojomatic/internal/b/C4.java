package org.pojomatic.internal.b;

import org.pojomatic.annotations.Property;
import org.pojomatic.internal.a.C3;

@SuppressWarnings("all")
public class C4 extends C3 {
  @Property int packagePrivate() { return 4; }
  @Property protected int packagePrivateOverriddenProtected() { return 4; }
  @Property public int packagePrivateOverriddenPublic() { return 4; }
  @Property protected int protectedMethod() { return 4; }
  @Property public int publicMethod() { return 4; }
}
