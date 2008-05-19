package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;

import org.pojomatic.annotations.Property;

/**
 * Shows only the last four characters a {@link Property}. Useful for credit card numbers, social
 * security numbers, etc. For example, a 16 character {@link String} representing a credit card number would
 * be formatted as "************1234".
 */
public class AccountNumberFormatter implements PropertyFormatter {

  public void format(Object value, Appendable appendable) {
    // TODO Auto-generated method stub
  }

  public void initialize(AnnotatedElement element) {
    // TODO Auto-generated method stub

  }

}
