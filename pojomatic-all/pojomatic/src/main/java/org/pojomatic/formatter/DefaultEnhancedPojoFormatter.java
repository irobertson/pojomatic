package org.pojomatic.formatter;

import org.pojomatic.Pojomatic;
import org.pojomatic.PropertyElement;

/**
 * Default formatter for classes that use {@link Pojomatic}.  This implementation first presents
 * the class name, and then each property in turn, separated by commas, using braces to indicate
 * nesting.
 * <p>
 * For example, if a class Person has two properties, firstName and LastName, and these properties
 * are using {@link DefaultPropertyFormatter}, then the Person
 * instance representing Joe Blow would be represented as
 * <code>"Person{firstName: {Joe}, lastName: {Blow}}"</code>
 *
 * @since 2.0
 */
public class DefaultEnhancedPojoFormatter implements EnhancedPojoFormatter {
  private boolean firstPropertyPrinted = false;

  @Override
  public final String getPropertyPrefix(PropertyElement property) {
    StringBuilder builder = new StringBuilder();
    appendPropertyPrefix(builder, property);
    return builder.toString();
  }

  @Override
  public final String getPropertySuffix(PropertyElement property) {
    StringBuilder builder = new StringBuilder();
    appendPropertySuffix(builder, property);
    return builder.toString();
  }

  @Override
  public final String getToStringPrefix(Class<?> pojoClass) {
    StringBuilder builder = new StringBuilder();
    appendToStringPrefix(builder, pojoClass);
    return builder.toString();
  }

  @Override
  public final String getToStringSuffix(Class<?> pojoClass) {
    StringBuilder builder = new StringBuilder();
    appendToStringSuffix(builder, pojoClass);
    return builder.toString();
  }

  @Override
  public void appendToStringPrefix(StringBuilder builder, Class<?> pojoClass) {
    builder.append(pojoClass.getSimpleName()).append('{');
  }

  @Override
  public void appendToStringSuffix(StringBuilder builder, Class<?> pojoClass) {
    builder.append('}');
  }

  @Override
  public void appendPropertyPrefix(StringBuilder builder, PropertyElement property) {
    if (firstPropertyPrinted) {
      builder.append(", ");
    }
    else {
      firstPropertyPrinted = true;
    }
    builder.append(property.getName()).append(": {");
  }

  @Override
  public void appendPropertySuffix(StringBuilder builder, PropertyElement property) {
    builder.append('}');
  }
}
