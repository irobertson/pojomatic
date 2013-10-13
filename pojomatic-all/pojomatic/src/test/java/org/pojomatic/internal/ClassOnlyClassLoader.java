package org.pojomatic.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.CodeSource;
import java.security.ProtectionDomain;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

/**
 * A {@link ClassLoader} which loads classes, but will not expose their byte code.
 */
class ClassOnlyClassLoader extends ClassLoader {
  private final ClassLoader source;

  public ClassOnlyClassLoader(ClassLoader source) {
    super(null);
    this.source = Preconditions.checkNotNull(source);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (name.startsWith("org.pojomatic.annotations")) {
      return getClass().getClassLoader().loadClass(name);
    }
    try (InputStream classBytesStream = source.getResourceAsStream(name.replace('.', '/') + ".class")) {
      if (classBytesStream == null) {
        throw new ClassNotFoundException(name);
      }
      byte[] bytes = ByteStreams.toByteArray(classBytesStream);
      ProtectionDomain protectionDomain =
          new ProtectionDomain(new CodeSource(new File("/no/such/file").toURI().toURL(), new Certificate[0]), null);
      return defineClass(name, bytes, 0, bytes.length, protectionDomain);
    } catch (IOException e) {
      throw new ClassNotFoundException(name, e);
    }
  }
}
