package examples;

import org.pojomatic.formatter.EnhancedPropertyFormatter;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class IpAddressFormatterTest {
  private final static EnhancedPropertyFormatter FORMATTER = new IpAddressFormatter();

  @Test
  public void appendFormattedNull() {
    assertFormat("null", null);
  }

  @Test
  public void appendFormattedEmpty() {
    assertFormat("");
  }

  @Test
  public void appendFormatSingleByte() {
    assertFormat("4", (byte) 4);
    assertFormat("252", (byte) 252);
  }

  @Test void appendFormatMultiByte() {
    assertFormat("10.254.7.3", (byte) 10, (byte) -2, (byte) 7, (byte)3);
  }

  private void assertFormat(String expected, byte... array) {
    StringBuilder builder = new StringBuilder();
    FORMATTER.appendFormatted(builder, array);
    assertEquals(builder.toString(), expected);
  }
}
