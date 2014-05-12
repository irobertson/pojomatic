package org.pojomatic.formatter;

import static org.testng.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.pojomatic.annotations.SkipArrayCheck;
import org.pojomatic.internal.Type;
import org.pojomatic.internal.TypeProviders;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultEnhancedPropertyFormatterTest {

  @SuppressWarnings("unused") // used to initialize the formatter.
  private Object unAnnotated;

  @SkipArrayCheck
  private Object skipArrayCheckAnnotated;

  private Field unAnnotatedField, skipArrayCheckAnnotatedField;

  @BeforeClass public void setUp() throws Exception {
    unAnnotatedField = getClass().getDeclaredField("unAnnotated");
    skipArrayCheckAnnotatedField = getClass().getDeclaredField("skipArrayCheckAnnotated");
  }

  @Test(dataProvider = "types", dataProviderClass = TypeProviders.class)
  public void testSimpleAppendFormatted(Type type) throws Exception {
    DefaultEnhancedPropertyFormatter formatter = new DefaultEnhancedPropertyFormatter();
    Method appendFormatted =
      formatter.getClass().getMethod("appendFormatted", new Class[] { StringBuilder.class, type.getClazz() });
    for (Object value: type.getSampleValues()) {
      StringBuilder builder = new StringBuilder();
      appendFormatted.invoke(formatter, builder, value);
      assertEquals(
        builder.toString(),
        type.toString(value),
        "value: " + possibleArrayToList(value));
    }
  }

  @Test(dataProvider = "arrayTypes", dataProviderClass = TypeProviders.class)
  public void testArrayAsObjectToString(Type type, boolean skipArrayCheck) {
    DefaultEnhancedPropertyFormatter simpleFormatter = new DefaultEnhancedPropertyFormatter();
    for (Object value: type.getSampleValues()) {
      testArrayAsObjectToStringForValue(type, skipArrayCheck, simpleFormatter,
        value);
    }
  }

  private void testArrayAsObjectToStringForValue(Type type,
    boolean skipArrayCheck, DefaultEnhancedPropertyFormatter simpleFormatter,
    Object value) {
    StringBuilder builder = new StringBuilder();
    simpleFormatter.initialize(skipArrayCheck ? skipArrayCheckAnnotatedField : unAnnotatedField);
    if (skipArrayCheck) {
      simpleFormatter.appendFormatted(builder, value);
    }
    else {
      simpleFormatter.appendFormattedPossibleArray(builder, value);
    }
    if (! builder.toString().equals(skipArrayCheck ? Objects.toString(value) : type.deepToString(value)))
    assertEquals(
      builder.toString(),
      skipArrayCheck ? Objects.toString(value) : type.deepToString(value),
      "value: " + possibleArrayToList(value));
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
