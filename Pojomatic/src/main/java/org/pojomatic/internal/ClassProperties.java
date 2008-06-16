package org.pojomatic.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import org.pojomatic.PropertyAccessor;
import org.pojomatic.PropertyElement;
import org.pojomatic.PropertyField;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PojomaticDefaultPolicy;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

public class ClassProperties {
  private final Map<PropertyRole, Collection<PropertyElement>> properties =
    new EnumMap<PropertyRole, Collection<PropertyElement>>(PropertyRole.class);

  public ClassProperties(Class<?> pojoClass) {
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new HashSet<PropertyElement>());
    }

    AutoProperty autoProperty = pojoClass.getAnnotation(AutoProperty.class);
    PojomaticDefaultPolicy classPolicy = null;
    if (autoProperty != null) {
      classPolicy = autoProperty.policy();
    }

    for (Field field : pojoClass.getDeclaredFields()) {
      Property property = field.getAnnotation(Property.class);
      PojomaticPolicy propertyPolicy = null;
      if (property != null) {
        propertyPolicy = property.policy();
      }

      for (PropertyRole role : PropertyFilter.getRoles(propertyPolicy, classPolicy)) {
        properties.get(role).add(new PropertyField(field));
      }
    }

    for (Method method : pojoClass.getDeclaredMethods()) {
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
      for (PropertyRole role : PropertyFilter.getRoles(propertyPolicy, classPolicy)) {
        properties.get(role).add(new PropertyAccessor(method));
      }
    }
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
    //FIXME - compile
    return name.matches("(get|is)\\P{Ll}.*");
  }

  public Collection<PropertyElement> getEqualsProperties() {
    return properties.get(PropertyRole.EQUALS);
  }

  public Collection<PropertyElement> getHashCodeProperties() {
    return properties.get(PropertyRole.HASH_CODE);
  }

  public Collection<PropertyElement> getToStringProperties() {
    return properties.get(PropertyRole.TO_STRING);
  }

  public static <T> ClassProperties createInstance(Class<T> pojoClass) {
    return new ClassProperties(pojoClass);
  }
}
