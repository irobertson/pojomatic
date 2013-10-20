package org.pojomatic.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.pojomatic.PropertyElement;
import org.pojomatic.NoPojomaticPropertiesException;
import org.pojomatic.annotations.*;

/**
 * The properties of a class used for {@link PojomatorImpl#doHashCode(Object)},
 * {@link PojomatorImpl#doEquals(Object, Object)}, and {@link PojomatorImpl#doToString(Object)}.
 */
public class ClassProperties {
  private static final Pattern ACCESSOR_PATTERN = Pattern.compile("(get|is)\\P{Ll}.*");

  private final Map<PropertyRole, List<PropertyElement>> properties = makeProperties();

  private final Class<?> equalsParentClass;

  private final boolean subclassCannotOverrideEquals;

  private final static SelfPopulatingMap<Class<?>, ClassProperties> INSTANCES =
    new SelfPopulatingMap<Class<?>, ClassProperties>() {
      @Override
      protected ClassProperties create(Class<?> key) {
        return new ClassProperties(key);
      }
    };

  private final static class ClassContributionTracker {
    private Class<?> clazz = Object.class;

    public void noteContribution(Class<?> contributingClass) {
      clazz = contributingClass;
    }

    public Class<?> getMostSpecificContributingClass() {
      return clazz;
    }
  }

  /**
   * Get an instance for the given {@code pojoClass}.  Instances are cached, so calling this method
   * repeatedly is not inefficient.
   * @param pojoClass the class to inspect for properties
   * @return The {@code ClassProperties} for {@code pojoClass}.
   * @throws NoPojomaticPropertiesException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic.
   */
  public static ClassProperties forClass(Class<?> pojoClass) throws NoPojomaticPropertiesException {
    return INSTANCES.get(pojoClass);
  }

  /**
   * Creates an instance for the given {@code pojoClass}.
   *
   * @param pojoClass the class to inspect for properties
   * @throws NoPojomaticPropertiesException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic.
   */
  private ClassProperties(Class<?> pojoClass) throws NoPojomaticPropertiesException {
    if (pojoClass.isInterface()) {
      extractClassProperties(pojoClass, new OverridableMethods(), new ClassContributionTracker());
      equalsParentClass = pojoClass;
    }
    else {
      ClassContributionTracker classContributionTracker = new ClassContributionTracker();
      walkHierarchy(pojoClass, new OverridableMethods(), classContributionTracker);
      equalsParentClass = classContributionTracker.getMostSpecificContributingClass();
    }
    verifyPropertiesNotEmpty(pojoClass);
    subclassCannotOverrideEquals = pojoClass.isAnnotationPresent(SubclassCannotOverrideEquals.class)
      || pojoClass.isInterface();
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
   * Get the union of all properties used for any Pojomator methods.
   * @return the union of all properties used for any Pojomator methods.
   */
  public Set<PropertyElement> getAllProperties() {
    LinkedHashSet<PropertyElement> allProperties = new LinkedHashSet<>();
    allProperties.addAll(properties.get(PropertyRole.EQUALS));
    allProperties.addAll(properties.get(PropertyRole.TO_STRING));
    return allProperties;
  }

  /**
   * Whether instances of {@code otherClass} are candidates for being equal to instances of
   * the class this {@code ClassProperties} instance was created for.
   * @param otherClass the class to check for compatibility for equals with.
   * @return {@code true} if instances of {@code otherClass} are candidates for being equal to
   * instances of the class this {@code ClassProperties} instance was created for, or {@code false}
   * otherwise.
   */
  public boolean isCompatibleForEquals(Class<?> otherClass) {
    if (!equalsParentClass.isAssignableFrom(otherClass)) {
      return false;
    }
    else {
      if (subclassCannotOverrideEquals) {
        return true;
      }
      else {
        return equalsParentClass.equals(forClass(otherClass).equalsParentClass);
      }
    }
  }

  private void walkHierarchy(
    Class<?> clazz,
    OverridableMethods overridableMethods,
    ClassContributionTracker classContributionTracker) {
    if (clazz != Object.class) {
      walkHierarchy(clazz.getSuperclass(), overridableMethods, classContributionTracker);
      extractClassProperties(clazz, overridableMethods, classContributionTracker);
      if (clazz.isAnnotationPresent(OverridesEquals.class)) {
        classContributionTracker.noteContribution(clazz);
      }
    }
  }

  private void extractClassProperties(
    Class<?> clazz,
    OverridableMethods overridableMethods,
    ClassContributionTracker classContributionTracker) {
    AutoProperty autoProperty = clazz.getAnnotation(AutoProperty.class);
    final DefaultPojomaticPolicy classPolicy =
      (autoProperty != null) ? autoProperty.policy() : null;
    final AutoDetectPolicy autoDetectPolicy =
      (autoProperty != null) ? autoProperty.autoDetect() : null;

    Map<PropertyRole, Map<String, PropertyElement>> fieldsMap = extractFields(
      clazz, classPolicy, autoDetectPolicy, classContributionTracker);
    Map<PropertyRole, Map<String, PropertyElement>> methodsMap = extractMethods(
      clazz, classPolicy, autoDetectPolicy, overridableMethods, classContributionTracker);
    PropertyClassVisitor propertyClassVisitor = PropertyClassVisitor.visitClass(clazz, fieldsMap, methodsMap);
    if (propertyClassVisitor != null) {
      for (PropertyRole role: PropertyRole.values()) {
        properties.get(role).addAll(propertyClassVisitor.getSortedProperties().get(role));
      }
    }
    else {
      for (PropertyRole role: PropertyRole.values()) {
        properties.get(role).addAll(fieldsMap.get(role).values());
        properties.get(role).addAll(methodsMap.get(role).values());
       }
    }
  }

  private Map<PropertyRole, Map<String, PropertyElement>> extractMethods(
    Class<?> clazz,
    final DefaultPojomaticPolicy classPolicy,
    final AutoDetectPolicy autoDetectPolicy,
    final OverridableMethods overridableMethods,
    final ClassContributionTracker classContributionTracker) {
    Map<PropertyRole, Map<String, PropertyElement>> propertiesMap = makePropertiesMap();
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
        PropertyAccessor propertyAccessor = new PropertyAccessor(method, getPropertyName(property));
        for (PropertyRole role : overridableMethods.checkAndMaybeAddRolesToMethod(
          method, PropertyFilter.getRoles(propertyPolicy, classPolicy))) {
          propertiesMap.get(role).put(method.getName(), propertyAccessor);
          if (PropertyRole.EQUALS == role) {
            classContributionTracker.noteContribution(clazz);
          }
        }
      }
    }
    return propertiesMap;
  }

  private Map<PropertyRole, Map<String, PropertyElement>> extractFields(
    Class<?> clazz,
    final DefaultPojomaticPolicy classPolicy,
    final AutoDetectPolicy autoDetectPolicy,
    final ClassContributionTracker classContributionTracker) {
    Map<PropertyRole, Map<String, PropertyElement>> propertiesMap = makePropertiesMap();
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
          PropertyField propertyField = new PropertyField(field, getPropertyName(property));
          propertiesMap.get(role).put(field.getName(), propertyField);
          if (PropertyRole.EQUALS == role) {
            classContributionTracker.noteContribution(clazz);
          }
        }
      }
    }
    return propertiesMap;
  }

  private void verifyPropertiesNotEmpty(Class<?> pojoClass) {
    for (Collection<PropertyElement> propertyElements : properties.values()) {
      if (!propertyElements.isEmpty()) {
        return;
      }
    }
    throw new NoPojomaticPropertiesException(pojoClass);
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

  private static Map<PropertyRole, List<PropertyElement>> makeProperties() {
    Map<PropertyRole, List<PropertyElement>> properties =
      new EnumMap<>(PropertyRole.class);
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new ArrayList<PropertyElement>());
    }
    return properties;
  }

  private static Map<PropertyRole, Map<String, PropertyElement>> makePropertiesMap() {
    Map<PropertyRole, Map<String, PropertyElement>> properties =
        new EnumMap<>(PropertyRole.class);
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new LinkedHashMap<String, PropertyElement>());
    }
    return properties;
  }
}
