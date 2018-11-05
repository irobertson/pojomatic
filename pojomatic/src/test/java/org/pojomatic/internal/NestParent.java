package org.pojomatic.internal;

import org.pojomatic.annotations.Property;

public class NestParent {
  @Property
  int i;

  static class NestChild {}
}
