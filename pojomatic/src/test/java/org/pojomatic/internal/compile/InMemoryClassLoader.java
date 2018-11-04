package org.pojomatic.internal.compile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

class InMemoryClassLoader extends ClassLoader {

  InMemoryClassLoader(ClassLoader parent) {
    super(parent);
  }

  private final Map<String, InMemoryClassFile> m = new HashMap<String, InMemoryClassFile>();

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    InMemoryClassFile mbc = m.get(classResourceName(name));
    return (mbc == null) ? super.findClass(name) : defineClass(name, mbc.getBytes(), 0, mbc.getBytes().length);
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    InMemoryClassFile mbc = m.get(name);
    return (mbc == null) ?  super.getResourceAsStream(name) : new ByteArrayInputStream(mbc.getBytes());

  }

  void addClass(String name, InMemoryClassFile mbc) {
    m.put(classResourceName(name), mbc);
  }

  private String classResourceName(String name) {
    return name.replaceAll("\\.", "/") + ".class";
  }
}

