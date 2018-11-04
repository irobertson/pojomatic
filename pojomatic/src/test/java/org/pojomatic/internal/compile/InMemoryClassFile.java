package org.pojomatic.internal.compile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class InMemoryClassFile extends SimpleJavaFileObject {
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

  public InMemoryClassFile(String name) {
    super(URI.create("memory:///" + name + ".class"), Kind.CLASS);
  }

  @Override
  public OutputStream openOutputStream() {
    return baos;
  }

  public byte[] getBytes() {
    return baos.toByteArray();
  }
}
