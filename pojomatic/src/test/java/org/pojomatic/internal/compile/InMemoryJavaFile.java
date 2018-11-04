package org.pojomatic.internal.compile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

class InMemoryJavaFile extends SimpleJavaFileObject {
  private final String content;

  InMemoryJavaFile(String name, String... lines) {
    super(URI.create("memory:///" + name.replace('.', '/') + ".java"), Kind.SOURCE);
    StringBuilder builder = new StringBuilder();
    for (String line: lines) {
      builder.append(line).append('\n');
    }
    this.content = builder.toString();
  }

  @Override
  public String getCharContent(boolean ignoreEncodingErrors) {
    return content;
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(content.getBytes());
  }
}
