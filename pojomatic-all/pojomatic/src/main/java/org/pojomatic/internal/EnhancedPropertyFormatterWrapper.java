package org.pojomatic.internal;

import java.lang.reflect.AnnotatedElement;

import org.pojomatic.formatter.EnhancedPropertyFormatter;
import org.pojomatic.formatter.PropertyFormatter;

@Deprecated
public class EnhancedPropertyFormatterWrapper implements EnhancedPropertyFormatter {
  private final PropertyFormatter delegate;

  public EnhancedPropertyFormatterWrapper(PropertyFormatter delegate) {
    this.delegate = delegate;
  }

  @Override
  public void initialize(AnnotatedElement element) {
    delegate.initialize(element);
  }

  @Override
  public String format(Object value) {
    return delegate.format(value);
  }

  @Override public void appendFormatted(StringBuilder builder, Object o) { builder.append(delegate.format(o)); }
  @Override public void appendFormatted(StringBuilder builder, boolean b) { builder.append(delegate.format(b)); }
  @Override public void appendFormatted(StringBuilder builder, byte b) { builder.append(delegate.format(b)); }
  @Override public void appendFormatted(StringBuilder builder, short s) { builder.append(delegate.format(s)); }
  @Override public void appendFormatted(StringBuilder builder, char c) { builder.append(delegate.format(c)); }
  @Override public void appendFormatted(StringBuilder builder, int i) { builder.append(delegate.format(i)); }
  @Override public void appendFormatted(StringBuilder builder, long l) { builder.append(delegate.format(l)); }
  @Override public void appendFormatted(StringBuilder builder, float f) { builder.append(delegate.format(f)); }
  @Override public void appendFormatted(StringBuilder builder, double d) { builder.append(delegate.format(d)); }
  @Override public void appendFormatted(StringBuilder builder, boolean[] b) { builder.append(delegate.format(b)); }
  @Override public void appendFormatted(StringBuilder builder, byte[] b) { builder.append(delegate.format(b)); }
  @Override public void appendFormatted(StringBuilder builder, short[] s) { builder.append(delegate.format(s)); }
  @Override public void appendFormatted(StringBuilder builder, char[] c) { builder.append(delegate.format(c)); }
  @Override public void appendFormatted(StringBuilder builder, int[] i) { builder.append(delegate.format(i)); }
  @Override public void appendFormatted(StringBuilder builder, long[] l) { builder.append(delegate.format(l)); }
  @Override public void appendFormatted(StringBuilder builder, float[] f) { builder.append(delegate.format(f)); }
  @Override public void appendFormatted(StringBuilder builder, double[] d) { builder.append(delegate.format(d)); }
}
