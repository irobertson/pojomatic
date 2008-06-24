package org.pojomatic.formatter;

import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;

import org.pojomatic.annotations.Property;

/**
 * Shows only the last 4 characters of a {@link Property}. Useful for credit card numbers, social
 * security numbers, etc. This formatter cannot be used with properties of array types.
 * <br><br>
 * For example, a 16 character {@code String} representing a credit card number would
 * be formatted as "************1234".
 */
public class AccountNumberFormatter extends DefaultPropertyFormatter {
  private static final int DEFAULT_PLAINTEXT_CHARS = 4;
  private static final int DEFAULT_FILL_CHAR = '*';

  private int plaintextChars = DEFAULT_PLAINTEXT_CHARS;
  private char fillChar = DEFAULT_FILL_CHAR;

  @Override
  public void initialize(@SuppressWarnings("unused") AnnotatedElement element) {
    //nothing to initialize
  }

  @Override
  public String format(Object value) {
    String rep = super.format(value);
    int repLength = rep.length();
    if (repLength <= getPlaintextChars()) {
      return rep;
    } else {
      char[] repChars = rep.toCharArray();
      Arrays.fill(repChars, 0, repLength - getPlaintextChars(), getFillChar());
      return String.valueOf(repChars);
    }
  }

  protected int getPlaintextChars() {
    return plaintextChars;
  }

  protected void setPlaintextChars(int plaintextChars) {
    this.plaintextChars = plaintextChars;
  }

  protected char getFillChar() {
    return fillChar;
  }

  protected void setFillChar(char fillChar) {
    this.fillChar = fillChar;
  }

}
