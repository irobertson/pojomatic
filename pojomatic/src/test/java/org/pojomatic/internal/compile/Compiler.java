package org.pojomatic.internal.compile;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;

import org.pojomatic.Pojomatic;

public class Compiler {
  private final JavaCompiler javac;
  private final InMemoryClassLoader classLoader;
  private final JavaFileManager javaFileManager;

  public Compiler(JavaCompiler javac) {
    this.javac = javac;
    classLoader = new InMemoryClassLoader(Pojomatic.class.getClassLoader());
    javaFileManager = new InMemoryJavaFileManager(javac.getStandardFileManager(null, null, null), classLoader);
  }

  /**
   * Compile a java file.
   * @param className the name of top-level class in the file
   * @param javaLines the lines of the java file
   * @throws IOException if something goes wrong
   */
  public void compile(String className, String... javaLines) throws IOException {
    compile(new InMemoryJavaFile(className, javaLines));
  }

  private void compile(InMemoryJavaFile file) throws IOException {
      boolean compilationSuccessful = javac.getTask(
        null,
        javaFileManager,
        null,
        null,
        null,
        asList(file)).call();
      assertTrue(compilationSuccessful);
  }

  /**
   * Get the class loader this compiler compiles to.
   * @return the class loader this compiler compiles to.
   */
  public ClassLoader getClassLoader() {
    return classLoader;
  }
}
