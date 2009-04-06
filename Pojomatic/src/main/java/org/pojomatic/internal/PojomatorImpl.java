package org.pojomatic.internal;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PojoFormat;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.diff.Difference;
import org.pojomatic.diff.DifferenceToNull;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.PropertyDifferences;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.pojomatic.formatter.PropertyFormatter;

public class PojomatorImpl<T> implements Pojomator<T>{
  final static int HASH_CODE_SEED = 1;
  final static int HASH_CODE_MULTIPLIER = 31;


  /**
   * Creates an instance for {@code clazz}.
   *
   * @param clazz the class
   * @throws IllegalArgumentException if {@code clazz} has no properties annotated for use
   * with Pojomatic
   */
  public PojomatorImpl(Class<T> clazz) throws IllegalArgumentException {
    this.clazz = clazz;
    classProperties = ClassProperties.createInstance(clazz);
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

  /**
   * {@inheritDoc}
   *<p>
   * For this to return true, the following must hold:
   * <ul>
   *  <li>{@code other} must be non-null, and an instance of {@code T}</li>
   *  <li>Each property of {@code instance} which has a {@link PojomaticPolicy} other than
   *  {@link PojomaticPolicy#TO_STRING TO_STRING} or {@link PojomaticPolicy#NONE NONE} must be equal
   *  to the corresponding property of {@code other} in the following sense:
   *  <ul>
   *   <li>Both are {@code null}, or</li>
   *   <li>Both are reference-equals (==) to each other, or</li>
   *   <li>Both are primitive of the same type, and equal to each other, or</li>
   *   <li>Both are of array type, with matching primitive component types, and the corresponding
   *   {@code} equals method of {@link Arrays} returns true, or</li>
   *   <li>Both are of array type with non-primitive component types, and
   *   {@link Arrays#deepEquals(Object[], Object[])} returns true, or</li>
   *   <li>The property {@code p} in {@code instance} is an object not of array type, and
   *   {@code instanceP.equals(otherP)} returns true.
   *  </ul>
   * </ul>
   *
   * @throws NullPointerException if {@code instance} is null
   */
  public boolean doEquals(T instance, Object other) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    if (instance == other) {
      return true;
    }
    if (! clazz.isInstance(other)) {
      return false;
    }

    for (PropertyElement prop: classProperties.getEqualsProperties()) {
      if (!areValuesEqual(prop.getValue(instance), prop.getValue(other))) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   *<p>
   * This is done by computing the hashCode of each property which has a {@link PojomaticPolicy} of
   * {@link PojomaticPolicy#HASHCODE_EQUALS HASHCODE_EQUALS} or {@link PojomaticPolicy#ALL ALL}
   * (using 0 when the property is null), and combining them in a fashion similar to that of
   * {@link List#hashCode()}.
   *
   * @throws NullPointerException if {@code instance} is null
   */
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
        if (! instanceComponentClass.isPrimitive()) {
          return Arrays.hashCode((Object[]) value);
        }
        else {
          if (Boolean.TYPE == instanceComponentClass) {
            return Arrays.hashCode((boolean[]) value);
          }
          else if (Byte.TYPE == instanceComponentClass) {
            return  Arrays.hashCode((byte[]) value);
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

  /**
   * {@inheritDoc}
   * <p>
   * The format used depends on the
   * {@link PojoFormatter} used for the POJO, and the {@link PropertyFormatter} of each property.
   * <p>
   * For example, suppose a class {@code Person} has properties {@code firstName} and
   * {@code lastName} which are included in its {@code String} representation.
   * No {@code PojoFormatter} or {@code PropertyFormatter} are specified, so the defaults are used.
   * In particular, instances of {@code DefaultPropertyFormatter} will be created for
   * {@code firstName} and {@code lastName} (referred to here as {@code firstNameFormatter} and
   * {@code lastNameFormatter}, respectively).  Let {@code firstNameProperty} and
   * {@code lastNameProperty} refer to the instances of {@link PropertyElement} referring to the
   * properties {@code firstName} and {@code lastName} respectively.
   * </p>
   * <p>
   * For a non-null {@code Person} instance, the {@code String} representation will be created by
   * creating an instance of {@code DefaultPojoFormatter} for the {@code Person} class (referred to
   * here as {@code personFormatter}), and then concatenating the results of following:
   * <ol>
   *   <li>{@link DefaultPojoFormatter#getToStringPrefix(Class) personFormatter.getToStringPrefix(Person.class)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertyPrefix(PropertyElement) personFormatter.getPropertyPrefix(firstNameProperty)}</li>
   *   <li>{@link DefaultPropertyFormatter#format(Object) firstNameFormatter.format(firstName)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertySuffix(PropertyElement) personFormatter.getPropertySuffix(firstNameProperty)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertyPrefix(PropertyElement) personFormatter.getPropertyPrefix(lastNameProperty)}</li>
   *   <li>{@link DefaultPropertyFormatter#format(Object) lastNameFormatter.format(lastName)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertySuffix(PropertyElement) personFormatter.getPropertySuffix(lasttNameProperty)}</li>
   *   <li>{@link DefaultPojoFormatter#getToStringSuffix(Class) personFormatter.getToStringSuffix(Person.class)}</li>
   * </ol>
   * </p>
   *
   * @throws NullPointerException if {@code instance} is null
   * @see Property#name()
   */
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

  private final Class<T> clazz;
  private final ClassProperties classProperties;
  private final List<FormattablePropertyElement> formattablePropertyElements =
    new ArrayList<FormattablePropertyElement>();
  private final Class<? extends PojoFormatter> pojoFormatterClass;

  private static class FormattablePropertyElement {
    private PropertyElement propertyElement;
    private PropertyFormatter propertyFormatter;

    public FormattablePropertyElement(
    PropertyElement propertyElement, PropertyFormatter propertyFormatter) {
      this.propertyElement = propertyElement;
      this.propertyFormatter = propertyFormatter;
    }
  }

  public Differences doDiff(T instance, T other) {
    if (instance == null) {
      throw new NullPointerException("instance is null");
    }
    if (other == null) {
      return new DifferenceToNull(instance);
    }
    if (instance == other) {
      return new PropertyDifferences(Collections.<Difference>emptyList());
    }
    if (!clazz.isInstance(other)) {
      throw new ClassCastException(
        "other has type " + other.getClass() + " which is not a subtype of " + clazz);
    }
    List<Difference> differences = new ArrayList<Difference>();
    for (PropertyElement prop: classProperties.getEqualsProperties()) {
      final Object instanceValue = prop.getValue(instance);
      final Object otherValue = prop.getValue(other);
      if (!areValuesEqual(instanceValue, otherValue)) {
        differences.add(new Difference(prop.getName(), instanceValue, otherValue));
      }
    }
    return new PropertyDifferences(differences);
  }


  /**
   * @param instance
   * @param other
   * @return true if the values of properties referenced by {@code prop} in {@code instance} and
   * {@code other} are equal to each other.
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
        Class<?> instanceComponentClass = instanceValue.getClass().getComponentType();
        Class<?> otherComponentClass = otherValue.getClass().getComponentType();

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
            if(!Arrays.equals((boolean[]) instanceValue, (boolean[]) otherValue)) {
              return false;
            }
          }
          else if (Byte.TYPE == instanceComponentClass) {
            if (! Arrays.equals((byte[]) instanceValue, (byte[]) otherValue)) {
              return false;
            }
          }
          else if (Character.TYPE == instanceComponentClass) {
            if(!Arrays.equals((char[]) instanceValue, (char[]) otherValue)) {
              return false;
            }
          }
          else if (Short.TYPE == instanceComponentClass) {
            if(!Arrays.equals((short[]) instanceValue, (short[]) otherValue)) {
              return false;
            }
          }
          else if (Integer.TYPE == instanceComponentClass) {
            if(!Arrays.equals((int[]) instanceValue, (int[]) otherValue)) {
              return false;
            }
          }
          else if (Long.TYPE == instanceComponentClass) {
            if(!Arrays.equals((long[]) instanceValue, (long[]) otherValue)) {
              return false;
            }
          }
          else if (Float.TYPE == instanceComponentClass) {
            if(!Arrays.equals((float[]) instanceValue, (float[]) otherValue)) {
              return false;
            }
          }
          else if (Double.TYPE == instanceComponentClass) {
            if(!Arrays.equals((double[]) instanceValue, (double[]) otherValue)) {
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
