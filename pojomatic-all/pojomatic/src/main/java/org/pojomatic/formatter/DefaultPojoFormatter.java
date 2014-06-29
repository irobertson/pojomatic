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
 * @deprecated Since 2.0. Use {@link DefaultEnhancedPojoFormatter} instead.
 */
@Deprecated
public class DefaultPojoFormatter implements PojoFormatter {
  private boolean firstPropertyPrinted = false;

  @Override
  public String getPropertyPrefix(PropertyElement property) {
    StringBuilder result = new StringBuilder();
    if (firstPropertyPrinted) {
      result.append(", ");
    }
    else {
      firstPropertyPrinted = true;
    }
    return result.append(property.getName()).append(": {").toString();
  }

  @Override
  public String getPropertySuffix(PropertyElement property) {
    return "}";
  }

  @Override
  public String getToStringPrefix(Class<?> pojoClass) {
    return pojoClass.getSimpleName() + "{";
  }

  @Override
  public String getToStringSuffix(Class<?> pojoClass) {
    return "}";
  }
}
