package org.pojomatic.internal;

public interface ClassDefiner {

  Class<?> defineClass(String className, byte[] classBytes) throws IllegalAccessException;

}
