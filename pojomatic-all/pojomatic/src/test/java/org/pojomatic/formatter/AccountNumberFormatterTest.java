package org.pojomatic.formatter;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;

public class AccountNumberFormatterTest {

  @Test public void testFormatNull() {
    AssertJUnit.assertEquals("null", new AccountNumberFormatter().format(null));
  }

  @Test public void testFormatBlank() {
    AssertJUnit.assertEquals("", new AccountNumberFormatter().format(""));
  }

  @Test public void testFormatMin() {
    AssertJUnit.assertEquals("1234", new AccountNumberFormatter().format(1234));
  }

  @Test public void testFormat() {
    AssertJUnit.assertEquals("******6789", new AccountNumberFormatter().format("0123456789"));
  }

}
