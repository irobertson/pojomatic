package org.pojomatic.test;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

/**
 * Class where every instance is equal via {@link Pojomatic#equals(Object, Object)}, but never
 * by {@code this.equals(other)}.
 */
@AutoProperty
public class OnlyPojomaticEqual {
  @SuppressWarnings("unused")
  private final int number = 3;

  @Override
  public boolean equals(Object obj) {
    //cannot assert because only the unit under test should throw AssertionError
    if (!Pojomatic.equals(this, obj)) {
      throw new IllegalStateException("Invariant violated");
    }
    return false;
  }
}
