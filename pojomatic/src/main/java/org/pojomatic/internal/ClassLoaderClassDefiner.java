package org.pojomatic.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class ClassLoaderClassDefiner implements ClassDefiner {

  private static final class DynamicClassLoader extends ClassLoader {
    private DynamicClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> loadClass(String name, byte[] classBytes) {
      return defineClass(name, classBytes, 0, classBytes.length);
    }
  }

  private DynamicClassLoader classLoader = AccessController.doPrivileged(
    new PrivilegedAction<DynamicClassLoader>() {
      @Override
      public DynamicClassLoader run() {
        return new DynamicClassLoader(PojomatorFactory.class.getClassLoader());
      }
    });


  @Override
  public Class<?> defineClass(String className, byte[] classBytes) {
    return classLoader.loadClass(className, classBytes);
  }
}
