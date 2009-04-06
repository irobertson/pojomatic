package org.pojomatic.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.AutoDetectPolicy;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

/**
 * The properties of a class used for {@link PojomatorImpl#doHashCode(Object)},
 * {@link PojomatorImpl#doEquals(Object, Object)}, and {@link PojomatorImpl#doToString(Object)}.
 */
public class ClassProperties {
  private static final Pattern ACCESSOR_PATTERN = Pattern.compile("(get|is)\\P{Ll}.*");

  private final Map<PropertyRole, Collection<PropertyElement>> properties =
    new EnumMap<PropertyRole, Collection<PropertyElement>>(PropertyRole.class);

  /**
   * Creates an instance for the given {@code pojoClass}.
   *
   * @param pojoClass the class to inspect for properties
   * @throws IllegalArgumentException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic.
   */
  private ClassProperties(Class<?> pojoClass) throws IllegalArgumentException {
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new ArrayList<PropertyElement>());
    }

    for (Class<?> clazz = pojoClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
      AutoProperty autoProperty = clazz.getAnnotation(AutoProperty.class);
      DefaultPojomaticPolicy classPolicy = null;
      if (autoProperty != null) {
        classPolicy = autoProperty.policy();
      }

      for (Field field : clazz.getDeclaredFields()) {
        Property property = field.getAnnotation(Property.class);
        PojomaticPolicy propertyPolicy = null;
        if (property != null) {
          propertyPolicy = property.policy();
        }

        /* add all fields that are explicitly annotated or auto-detected */
        if (propertyPolicy != null ||
            (autoProperty != null && AutoDetectPolicy.FIELD == autoProperty.autoDetect())) {
          PropertyField propertyField = new PropertyField(field, getPropertyName(property));
          for (PropertyRole role : PropertyFilter.getRoles(propertyPolicy, classPolicy)) {
            properties.get(role).add(propertyField);
          }
        }
      }

      for (Method method : clazz.getDeclaredMethods()) {
        Property property = method.getAnnotation(Property.class);
        PojomaticPolicy propertyPolicy = null;
        if (property != null) {
          if (!methodSignatureIsAccessor(method)) {
            throw new IllegalArgumentException(
              "Method " + method +
              " is annotated with @Property but either takes arguments or returns void");
          }
          propertyPolicy = property.policy();
        }
        else if (!methodIsAccessor(method)) {
          continue;
        }

        /* add all methods that are explicitly annotated or auto-detected */
        if (propertyPolicy != null ||
            (autoProperty != null && AutoDetectPolicy.METHOD == autoProperty.autoDetect())) {
          PropertyAccessor propertyAccessor =
            new PropertyAccessor(method, getPropertyName(property));
          for (PropertyRole role : PropertyFilter.getRoles(propertyPolicy, classPolicy)) {
            properties.get(role).add(propertyAccessor);
          }
        }
      }
    }

    for (Collection<PropertyElement> propertyElements : properties.values()) {
      if (!propertyElements.isEmpty()) {
        return;
      }
    }
    throw new IllegalArgumentException("Class " + pojoClass.getName() + " has no Pojomatic properties");
  }

  private String getPropertyName(Property property) {
    return property == null ? "" : property.name();
  }

  private static boolean methodIsAccessor(Method method) {
    return methodSignatureIsAccessor(method)
      && isAccessorName(method.getName());
  }

  private static boolean methodSignatureIsAccessor(Method method) {
    return ! Void.TYPE.equals(method.getReturnType())
      && method.getParameterTypes().length == 0;
  }

  private static boolean isAccessorName(String name) {
    return ACCESSOR_PATTERN.matcher(name).matches();
  }

  /**
   * Gets the properties to use for {@link PojomatorImpl#doEquals(Object, Object)}.
   * @return the properties to use for {@link PojomatorImpl#doEquals(Object, Object)}.
   */
  public Collection<PropertyElement> getEqualsProperties() {
    return properties.get(PropertyRole.EQUALS);
  }

  /**
   * Gets the properties to use for {@link PojomatorImpl#doHashCode(Object)}.
   * @return the properties to use for {@link PojomatorImpl#doHashCode(Object)}.
   */
  public Collection<PropertyElement> getHashCodeProperties() {
    return properties.get(PropertyRole.HASH_CODE);
  }

  /**
   * Gets the properties to use for {@link PojomatorImpl#doToString(Object)}.
   * @return the properties to use for {@link PojomatorImpl#doToString(Object)}.
   */
  public Collection<PropertyElement> getToStringProperties() {
    return properties.get(PropertyRole.TO_STRING);
  }

  /**
   * Creates a new instance.
   *
   * @param <T> the type of {@code pojoClass}
   * @param pojoClass the class to inspect
   * @return a new instance
   * @throws IllegalArgumentException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic.
   */
  public static <T> ClassProperties createInstance(Class<T> pojoClass) throws IllegalArgumentException {
    return new ClassProperties(pojoClass);
  }
}
