package org.pojomatic.internal;

import org.pojomatic.PropertyElement;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.PojoFormatter;

@Deprecated
public class EnhancedPojoFormatterWrapper implements EnhancedPojoFormatter {
  private PojoFormatter delegate;

  @Override
  public String getToStringPrefix(Class<?> pojoClass) {
    return delegate.getToStringPrefix(pojoClass);
  }

  @Override
  public String getToStringSuffix(Class<?> pojoClass) {
    return delegate.getToStringSuffix(pojoClass);
  }

  @Override
  public String getPropertyPrefix(PropertyElement property) {
    return delegate.getPropertyPrefix(property);
  }

  @Override
  public String getPropertySuffix(PropertyElement property) {
    return delegate.getPropertySuffix(property);
  }

  @Override
  public void appendToStringPrefix(StringBuilder builder, Class<?> pojoClass) {
    builder.append(getToStringPrefix(pojoClass));
  }

  @Override
  public void appendToStringSuffix(StringBuilder builder, Class<?> pojoClass) {
    builder.append(getToStringSuffix(pojoClass));
  }

  @Override
  public void appendPropertyPrefix(StringBuilder builder, PropertyElement property) {
    builder.append(getPropertyPrefix(property));
  }

  @Override
  public void appendPropertySuffix(StringBuilder builder, PropertyElement property) {
    builder.append(getPropertySuffix(property));
  }


}
