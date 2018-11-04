package org.pojomatic.internal.compile;

import java.io.IOException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
    implements JavaFileManager {
  private final InMemoryClassLoader classLoader;

  public InMemoryJavaFileManager(JavaFileManager javaFileManager, InMemoryClassLoader classLoader) {
    super(javaFileManager);
    this.classLoader = classLoader;
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling)
      throws IOException {
    InMemoryClassFile mbc = new InMemoryClassFile(name);
    classLoader.addClass(name, mbc);
    return mbc;
  }
}
