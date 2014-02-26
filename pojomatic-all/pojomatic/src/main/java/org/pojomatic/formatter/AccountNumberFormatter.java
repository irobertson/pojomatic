package org.pojomatic.formatter;

import java.util.Arrays;

import org.pojomatic.annotations.Property;

/**
 * A property formatter which shows only the last 4 characters of the string representation of the
 * {@link Property}, with all others replaced by an asterisk ('*').
 * Useful for credit card numbers, social security numbers, etc.
 * <p>
 * For example, a 16 character {@code String} representing a credit card number would
 * be formatted as "************1234".
 *
 * @deprecated While this formatter can prevent the toString representation of an object including a full account
 * number, the full number can still be visible in heap dumps. A more secure approach is to avoid storing plaintext
 * private account numbers in memory at all (or at most, for as long as it takes to encrypt them). This formatter will
 * be removed in a future release of Pojomatic. Clients who feel they still need this functionality should implement it
 * themselves.
 */
@Deprecated
public class AccountNumberFormatter extends DefaultPropertyFormatter {
  private static final int DEFAULT_PLAINTEXT_CHARS = 4;
  private static final int DEFAULT_FILL_CHAR = '*';

  private int plaintextChars = DEFAULT_PLAINTEXT_CHARS;
  private char fillChar = DEFAULT_FILL_CHAR;

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
