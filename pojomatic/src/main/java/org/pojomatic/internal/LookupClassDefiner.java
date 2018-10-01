package org.pojomatic.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

public class LookupClassDefiner implements ClassDefiner {
  private final Lookup lookup = MethodHandles.lookup().dropLookupMode(Lookup.PRIVATE);

  @Override
  public Class<?> defineClass(String className, byte[] classBytes) throws IllegalAccessException {
    return lookup.defineClass(classBytes);
  }

}
