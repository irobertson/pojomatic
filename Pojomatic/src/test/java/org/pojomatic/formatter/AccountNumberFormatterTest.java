package org.pojomatic.formatter;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccountNumberFormatterTest {

  @Test public void testFormatNull() {
    assertEquals("null", new AccountNumberFormatter().format(null));
  }

  @Test public void testFormatBlank() {
    assertEquals("", new AccountNumberFormatter().format(""));
  }

  @Test public void testFormatMin() {
    assertEquals("1234", new AccountNumberFormatter().format(1234));
  }

  @Test public void testFormat() {
    assertEquals("******6789", new AccountNumberFormatter().format("0123456789"));
  }

}
