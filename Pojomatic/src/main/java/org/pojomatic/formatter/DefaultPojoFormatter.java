package org.pojomatic.formatter;

import org.pojomatic.Pojomatic;
import org.pojomatic.PropertyElement;

/**
 * Default formatter for classes that use {@link Pojomatic}.
 * For example,
 * TODO provide an example
 */
public class DefaultPojoFormatter implements PojoFormatter {
  private boolean firstPropertyPrinted = false;

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

  public String getPropertySuffix(PropertyElement property) {
    return "}";
  }

  public String getToStringPrefix(Class<?> pojoClass) {
    return pojoClass.getSimpleName() + "{";
  }

  public String getToStringSuffix(Class<?> pojoClass) {
    return "}";
  }
}
