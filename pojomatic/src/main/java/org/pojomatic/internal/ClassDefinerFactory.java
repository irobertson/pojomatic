package org.pojomatic.internal;

public class ClassDefinerFactory {
  private static volatile ClassDefiner CLASS_DEFINER;
  private final static Object MUTEX = new Object();

  // Avoid initializing when the class is first loaded, so that any exceptions don't get masked as NoClassDefFoundErrors
  public static ClassDefiner getDefiner() {
    if (CLASS_DEFINER == null) {
      synchronized (MUTEX) {
        if (CLASS_DEFINER == null) {
          CLASS_DEFINER = makeDefiner();
        }
      }
    }
    return CLASS_DEFINER;
  }

  private static ClassDefiner makeDefiner() {
    try {
      return (ClassDefiner) ClassDefiner.class.getClassLoader().loadClass("org.pojomatic.internal.LookupClassDefiner")
        .getConstructor()
        .newInstance();
    } catch (ReflectiveOperationException e) {
      return new ClassLoaderClassDefiner();
    }
  }
}
