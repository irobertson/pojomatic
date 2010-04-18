package org.pojomatic.internal;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.diff.Difference;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.diff.ValueDifference;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.pojomatic.formatter.PropertyFormatter;

public class PojomatorImpl<T> implements Pojomator<T>{
  final static int HASH_CODE_SEED = 1;
  final static int HASH_CODE_MULTIPLIER = 31;

  //TODO: someplace in our docs, we need to describe how we walk the class hierarchy, and how we
  //handle interfaces.

  /**
   * Creates an instance for {@code clazz}.
   *
   * @param clazz the class
   * @throws NoPojomaticPropertiesException if {@code clazz} has no properties annotated for use
   * with Pojomatic
   */
  public PojomatorImpl(Class<T> clazz) throws NoPojomaticPropertiesException {
    this.clazz = clazz;
    classProperties = ClassProperties.forClass(clazz);
    pojoFormatterClass = findPojoFormatterClass(clazz);
    for (PropertyElement prop: classProperties.getToStringProperties()) {
      PropertyFormatter propertyFormatter = findPropertyFormatter(prop.getElement());
      propertyFormatter.initialize(prop.getElement());
      formattablePropertyElements.add(new FormattablePropertyElement(prop, propertyFormatter));
    }
  }

  private static PropertyFormatter findPropertyFormatter(AnnotatedElement element) {
    PropertyFormat format = element.getAnnotation(PropertyFormat.class);
    try {
      return (format == null ? DefaultPropertyFormatter.class : format.value()).newInstance();
    }
    catch (InstantiationException e) {
      //TODO log this?
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      //TODO log this?
      throw new RuntimeException(e);
    }
  }

  private static Class<? extends PojoFormatter> findPojoFormatterClass(Class<?> clazz) {
    PojoFormat format = clazz.getAnnotation(PojoFormat.class);
    return format == null ? DefaultPojoFormatter.class : format.value();
  }

  public boolean doEquals(T instance, Object other) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    if (instance == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    if (!instance.getClass().equals(other.getClass())
        && !isCompatibleForEquality(other.getClass())) {
      return false;
    }

    for (PropertyElement prop: classProperties.getEqualsProperties()) {
      if (!areValuesEqual(prop.getValue(instance), prop.getValue(other))) {
        return false;
      }
    }
    return true;
  }

  public boolean isCompatibleForEquality(Class<?> otherClass) {
    return classProperties.isCompatibleForEquals(otherClass);
  }

  public int doHashCode(T instance) {
    int hashCode = HASH_CODE_SEED;
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    for (PropertyElement prop: classProperties.getHashCodeProperties()) {
      Object value = prop.getValue(instance);
      hashCode = HASH_CODE_MULTIPLIER * hashCode + (hashCodeOfValue(value));
    }
    return hashCode;
  }

  private int hashCodeOfValue(Object value) {
    if (value == null) {
      return 0;
    }
    else {
      if (value.getClass().isArray()) {
        Class<?> instanceComponentClass = value.getClass().getComponentType();
        if (!instanceComponentClass.isPrimitive()) {
          return Arrays.hashCode((Object[]) value);
        }
        else {
          if (Boolean.TYPE == instanceComponentClass) {
            return Arrays.hashCode((boolean[]) value);
          }
          else if (Byte.TYPE == instanceComponentClass) {
            return Arrays.hashCode((byte[]) value);
          }
          else if (Character.TYPE == instanceComponentClass) {
            return Arrays.hashCode((char[]) value);
          }
          else if (Short.TYPE == instanceComponentClass) {
            return Arrays.hashCode((short[]) value);
          }
          else if (Integer.TYPE == instanceComponentClass) {
            return Arrays.hashCode((int[]) value);
          }
          else if (Long.TYPE == instanceComponentClass) {
            return Arrays.hashCode((long[]) value);
          }
          else if (Float.TYPE == instanceComponentClass) {
            return Arrays.hashCode((float[]) value);
          }
          else if (Double.TYPE == instanceComponentClass) {
            return Arrays.hashCode((double[]) value);
          }
          else {
            // should NEVER happen
            throw new IllegalStateException(
              "unknown primative type " + instanceComponentClass.getName());
          }
        }
      }
      else {
        return value.hashCode();
      }
    }
  }

  public String doToString(T instance) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }

    PojoFormatter pojoFormatter;
    try {
      pojoFormatter = pojoFormatterClass.newInstance();
    }
    catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
    catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    StringBuilder result = new StringBuilder();
    result.append(pojoFormatter.getToStringPrefix(clazz));
    for (FormattablePropertyElement formattablePropertyElement: formattablePropertyElements) {
      result.append(pojoFormatter.getPropertyPrefix(formattablePropertyElement.propertyElement));
      result.append(formattablePropertyElement.propertyFormatter.format(
        formattablePropertyElement.propertyElement.getValue(instance)));
      result.append(pojoFormatter.getPropertySuffix(formattablePropertyElement.propertyElement));
    }
    result.append(pojoFormatter.getToStringSuffix(clazz));
    return result.toString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Pojomator for ").append(clazz.getName()).append(" with equals properties ");
    propertiesList(builder, classProperties.getEqualsProperties());
    builder.append(", hashCodeProperties ");
    propertiesList(builder, classProperties.getHashCodeProperties());
    builder.append(", and toStringProperties ");
    propertiesList(builder, classProperties.getToStringProperties());
    return builder.toString();
  }

  private void propertiesList(StringBuilder builder, final Iterable<PropertyElement> properties) {
    builder.append("{");
    boolean firstElement = true;
    for (PropertyElement prop: properties) {
      if (!firstElement) {
        builder.append(",");
      }
      else {
        firstElement = false;
      }
      builder.append(prop.getName());
    }
    builder.append("}");
  }

  private final Class<T> clazz;
  private final ClassProperties classProperties;
  private final List<FormattablePropertyElement> formattablePropertyElements =
    new ArrayList<FormattablePropertyElement>();
  private final Class<? extends PojoFormatter> pojoFormatterClass;

  private static class FormattablePropertyElement {
    private final PropertyElement propertyElement;
    private final PropertyFormatter propertyFormatter;

    public FormattablePropertyElement(
    PropertyElement propertyElement, PropertyFormatter propertyFormatter) {
      this.propertyElement = propertyElement;
      this.propertyFormatter = propertyFormatter;
    }
  }

  public Differences doDiff(T instance, T other) {
    final Collection<PropertyElement> diffProperties = classProperties.getEqualsProperties();
    if (instance == null) {
      throw new NullPointerException("instance is null");
    }
    if (other == null) {
      throw new NullPointerException("other is null");
    }
    if (instance == other) {
      return NoDifferences.getInstance();
    }

    checkClass(instance, "instance");
    checkClass(other, "other");
    final List<Difference> differences = new ArrayList<Difference>();
    for (PropertyElement prop : diffProperties) {
      final Object instanceValue = prop.getValue(instance);
      final Object otherValue = prop.getValue(other);
      if (!areValuesEqual(instanceValue, otherValue)) {
        differences.add(new ValueDifference(prop.getName(), instanceValue, otherValue));
      }
    }

    if (differences.isEmpty()) {
      return NoDifferences.getInstance();
    }
    return new PropertyDifferences(differences);
  }

  private void checkClass(T instance, String label) {
    if (!isCompatibleForEquality(instance.getClass())) {
      throw new IllegalArgumentException(
        label + " has type " + instance.getClass().getName()
        + " which is not compatible for equality with " + clazz.getName());
    }
  }


  /**
   * Compare two values for equality
   * @param instanceValue the first value to compare
   * @param otherValue the second value to compare
   * @return true if {@code instanceValue} and {@code otherValue} are equal to each other.
   */
  private static boolean areValuesEqual(Object instanceValue, Object otherValue) {
    if (instanceValue == null) {
      if (otherValue != null) {
        return false;
      }
    }
    else { // instanceValue is not null
      if (otherValue == null) {
        return false;
      }
      if (!instanceValue.getClass().isArray()) {
        if (!instanceValue.equals(otherValue)) {
          return false;
        }
      }
      else {
        if (!otherValue.getClass().isArray()) {
          return false;
        }
        final Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();
        final Class<?> otherComponentClass = otherValue.getClass().getComponentType();

        if (!instanceComponentClass.isPrimitive()) {
          if (otherComponentClass.isPrimitive()) {
            return false;
          }
          if (!Arrays.deepEquals((Object[]) instanceValue, (Object[]) otherValue)) {
            return false;
          }
        }
        else { // instanceComponentClass is primative
          if (otherComponentClass != instanceComponentClass) {
            return false;
          }

          if (Boolean.TYPE == instanceComponentClass) {
            if (!Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue)) {
              return false;
            }
          }
          else if (Byte.TYPE == instanceComponentClass) {
            if (!Arrays.equals((byte[]) instanceValue, (byte[]) otherValue)) {
              return false;
            }
          }
          else if (Character.TYPE == instanceComponentClass) {
            if (!Arrays.equals((char[]) instanceValue, (char[]) otherValue)) {
              return false;
            }
          }
          else if (Short.TYPE == instanceComponentClass) {
            if (!Arrays.equals((short[]) instanceValue, (short[]) otherValue)) {
              return false;
            }
          }
          else if (Integer.TYPE == instanceComponentClass) {
            if (!Arrays.equals((int[]) instanceValue, (int[]) otherValue)) {
              return false;
            }
          }
          else if (Long.TYPE == instanceComponentClass) {
            if (!Arrays.equals((long[]) instanceValue, (long[]) otherValue)) {
              return false;
            }
          }
          else if (Float.TYPE == instanceComponentClass) {
            if (!Arrays.equals((float[]) instanceValue, (float[]) otherValue)) {
              return false;
            }
          }
          else if (Double.TYPE == instanceComponentClass) {
            if (!Arrays.equals((double[]) instanceValue, (double[]) otherValue)) {
              return false;
            }
          }
          else {
            // should NEVER happen
            throw new IllegalStateException(
              "unknown primative type " + instanceComponentClass.getName());
          }
        }
      }
    }
    return true;
  }
}
