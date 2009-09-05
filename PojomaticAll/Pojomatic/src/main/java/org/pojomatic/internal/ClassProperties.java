package org.pojomatic.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  
  private static final Pattern ACCESSOR_PATTERN = Pattern.compile("(get|is)\\P{Ll}.*");

  private final Map<PropertyRole, Collection<PropertyElement>> properties = makeProperties();

  /**
   * Creates an instance for the given {@code pojoClass}.
   *
   * @param pojoClass the class to inspect for properties
   * @throws IllegalArgumentException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic.
   */
  private ClassProperties(Class<?> pojoClass) throws IllegalArgumentException {
    if (pojoClass.isInterface()) {
      extractClassProperties(pojoClass, new OverridableMethods());
    }
    else {
      walkHierarchy(pojoClass, new OverridableMethods());
    }
    verifyPropertiesNotEmpty(pojoClass);
  }

  private void walkHierarchy(Class<?> clazz, OverridableMethods overridableMethods) {
    if (clazz != Object.class) {
      walkHierarchy(clazz.getSuperclass(), overridableMethods);
      extractClassProperties(clazz, overridableMethods);
    }
  }

  private void extractClassProperties(Class<?> clazz, OverridableMethods overridableMethods) {
    AutoProperty autoProperty = clazz.getAnnotation(AutoProperty.class);
    final DefaultPojomaticPolicy classPolicy = 
      (autoProperty != null) ? autoProperty.policy() : null; 
    final AutoDetectPolicy autoDetectPolicy =
      (autoProperty != null) ? autoProperty.autoDetect() : null;

    extractFields(clazz, classPolicy, autoDetectPolicy);
    extractMethods(clazz, classPolicy, autoDetectPolicy, overridableMethods);
  }

  private void extractMethods(
    Class<?> clazz,
    final DefaultPojomaticPolicy classPolicy,
    final AutoDetectPolicy autoDetectPolicy,
    final OverridableMethods overridableMethods) {
    for (Method method : clazz.getDeclaredMethods()) {
      Property property = method.getAnnotation(Property.class);
      if (isStatic(method)) {
        if (property != null) {
          throw new IllegalArgumentException(
            "Static method " + clazz.getName() + "." + method.getName()
            + "() is annotated with @Property");
        }
        else {
          continue;
        }
      }
      
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

      /* add all methods that are explicitly annotated or auto-detected, and not overriding already
       * added methods */
      if (propertyPolicy != null || AutoDetectPolicy.METHOD == autoDetectPolicy) {
        for (PropertyRole role : overridableMethods.checkAndMaybeAddRolesToMethod(
          method, PropertyFilter.getRoles(propertyPolicy, classPolicy))) {
          properties.get(role).add(new PropertyAccessor(method, getPropertyName(property)));
        }
      }
    }
  }

  private void extractFields(Class<?> clazz, final DefaultPojomaticPolicy classPolicy,
    final AutoDetectPolicy autoDetectPolicy) {
    for (Field field : clazz.getDeclaredFields()) {
      Property property = field.getAnnotation(Property.class);
      if (isStatic(field)) {
        if (property != null) {
          throw new IllegalArgumentException(
            "Static field " + clazz.getName() + "." + field.getName()
            + " is annotated with @Property");
        }
        else {
          continue;
        }
      }
      
      final PojomaticPolicy propertyPolicy = (property != null) ? property.policy() : null;

      /* add all fields that are explicitly annotated or auto-detected */
      if (propertyPolicy != null || AutoDetectPolicy.FIELD == autoDetectPolicy) {
        for (PropertyRole role : PropertyFilter.getRoles(propertyPolicy, classPolicy)) {
          properties.get(role).add(new PropertyField(field, getPropertyName(property)));
        }
      }
    }
  }

  private void verifyPropertiesNotEmpty(Class<?> pojoClass) {
    for (Collection<PropertyElement> propertyElements : properties.values()) {
      if (!propertyElements.isEmpty()) {
        return;
      }
    }
    throw new IllegalArgumentException(
      "Class " + pojoClass.getName() + " has no Pojomatic properties");
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

  private static boolean isStatic(Member member) {
    return Modifier.isStatic(member.getModifiers());
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

  private static Map<PropertyRole, Collection<PropertyElement>> makeProperties() {
    Map<PropertyRole, Collection<PropertyElement>> properties =
      new EnumMap<PropertyRole, Collection<PropertyElement>>(PropertyRole.class);
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new ArrayList<PropertyElement>());
    }
    return properties;
  }
}
