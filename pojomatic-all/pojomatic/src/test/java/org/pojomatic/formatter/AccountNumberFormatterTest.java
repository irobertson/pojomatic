package org.pojomatic.formatter;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AccountNumberFormatterTest {

  @Test public void testFormatNull() {
    assertEquals(new AccountNumberFormatter().format(null), "null");
  }

  @Test public void testFormatBlank() {
    assertEquals(new AccountNumberFormatter().format(""), "");
  }

  @Test public void testFormatMin() {
    assertEquals(new AccountNumberFormatter().format(1234), "1234");
  }

  @Test public void testFormat() {
    assertEquals(new AccountNumberFormatter().format("0123456789"), "******6789");
  }

}
