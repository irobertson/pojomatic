package org.pojomatic.formatter;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.DeepArray;
import org.pojomatic.internal.Type;
import org.pojomatic.internal.TypeProviders;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultEnhancedPropertyFormatterTest {
  private Field unAnnotated;

  @DeepArray
  private Field deepArrayAnnotated;

  @BeforeClass public void setUp() throws Exception {
    unAnnotated = getClass().getDeclaredField("unAnnotated");
    deepArrayAnnotated = getClass().getDeclaredField("deepArrayAnnotated");
  }

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testSimpleAppendFormatted(Type type) throws Exception {
    DefaultEnhancedPropertyFormatter formatter = new DefaultEnhancedPropertyFormatter();
    Method appendFormatted =
      formatter.getClass().getMethod("appendFormatted", new Class[] { StringBuilder.class, type.getClazz() });
    for (Object value: type.getSampleValues()) {
      StringBuilder builder = new StringBuilder();
      appendFormatted.invoke(formatter, builder, value);
      Assert.assertEquals(
        builder.toString(),
        type.toString(value),
        "value: " + possibleArrayToList(value));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectToString(Type type, boolean canBeArray, boolean deepArray) {
    DefaultEnhancedPropertyFormatter simpleFormatter = new DefaultEnhancedPropertyFormatter();
    for (Object value: type.getSampleValues()) {
      StringBuilder builder = new StringBuilder();
      simpleFormatter.initialize(deepArray ? deepArrayAnnotated : unAnnotated);
      if (canBeArray) {
        simpleFormatter.appendFormattedPossibleArray(builder, value);
      }
      else {
        simpleFormatter.appendFormatted(builder, value);
      }
      Assert.assertEquals(
        builder.toString(),
        (canBeArray
          ? ( deepArray ? type.deepToString(value) : type.toString(value) )
          : Objects.toString(value)),
        "value: " + possibleArrayToList(value));
    }
  }


  /**
   * Convert arrays to lists, leaving other types alone.
   * @param value
   * @return {@code value} if value is not an array, or the List equivalent of {@code value} if value is an array
   */
  private Object possibleArrayToList(Object value) {
    if (value == null || ! value.getClass().isArray()) {
      return value;
    }
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < Array.getLength(value); i++) {
      result.add(possibleArrayToList(Array.get(value, i)));
    }
    return result;
  }
}
