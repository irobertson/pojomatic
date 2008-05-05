package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;

public interface PropertyFormatter {
  public void initialize(AnnotatedElement element);

  public void format(Object value, Appendable appendable);
}
