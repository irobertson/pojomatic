package examples;

import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;

public class IpAddressFormatter extends DefaultEnhancedPropertyFormatter {
  @Override
  public void appendFormatted(StringBuilder builder, byte[] array) {
    if (array == null) {
      super.appendFormatted(builder, array);
    }
    else {
      boolean first = true;
      for (byte b: array) {
        if (first) {
          first = false;
        }
        else {
          builder.append('.');
        }
        builder.append(((int) b) & 0xff);
      }
    }
  }
}
