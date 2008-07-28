package org.pojomatic.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.pojomatic.formatter.PropertyFormatter;

public class PojomatorImpl<T> implements Pojomator<T>{
  final static int HASH_CODE_SEED = 1;
  final static int HASH_CODE_MULTIPLIER = 31;


  public PojomatorImpl(Class<T> clazz) {
    this.clazz = clazz;
    classProperties = new ClassProperties(clazz);
    for (PropertyElement prop: classProperties.getToStringProperties()) {
      PropertyFormatter propertyFormatter = new DefaultPropertyFormatter(); //BROKEN
      propertyFormatter.initialize(prop.getElement());
      propertyFormatters.put(prop, propertyFormatter);
    }
  }

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
      Object instanceValue = prop.getValue(instance);
      Object otherValue = prop.getValue(other);
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
          //TODO - decide if we should choose to enter this branch based off of the property's
          // declared or runtime type.  Either way, document the choice.  For now, runtime.
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
          }
        }
      }
    }
    return true;
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
   * Creates the {@code String} representation of the given instance. The format used depends on the
   * {@link PojoFormatter} used for the pojo, and the {@link PropertyFormatter} of each property.
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
   * @param instance the instance for which to create a {@code String} representation. Must not be
   * {@code null}.
   * @return the {@code String} representation of the given instance
   */
  public String doToString(T instance) {
    if (instance == null) {
      throw new NullPointerException("instance must not be null");
    }
    PojoFormatter pojoFormatter = new DefaultPojoFormatter(); //BROKEN
    StringBuilder result = new StringBuilder();
    result.append(pojoFormatter.getToStringPrefix(clazz));
    for (PropertyElement prop: classProperties.getToStringProperties()) {
      result.append(pojoFormatter.getPropertyPrefix(prop));
      result.append(propertyFormatters.get(prop).format(prop.getValue(instance)));
      result.append(pojoFormatter.getPropertySuffix(prop));
    }
    result.append(pojoFormatter.getToStringSuffix(clazz));
    return result.toString();
  }

  private final Class<T> clazz;
  private final ClassProperties classProperties;
  private final Map<PropertyElement, PropertyFormatter> propertyFormatters =
    new HashMap<PropertyElement, PropertyFormatter>();
}
