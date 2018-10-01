package org.pojomatic.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;

public class PojomatorFactory {
  public static <T> Pojomator<T> makePojomator(final Class<T> pojoClass) {
    try {
      return AccessController.doPrivileged(new PrivilegedExceptionAction<Pojomator<T>>() {
        @Override
        public Pojomator<T> run() throws Exception {
          return makePojomatorChecked(pojoClass);
        }
      });
    } catch (PrivilegedActionException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private static <T> Pojomator<T> makePojomatorChecked(Class<T> pojoClass)
      throws IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException,
      InvocationTargetException, NoSuchMethodException {
    ClassProperties classProperties = ClassProperties.forClass(pojoClass);
    PojomatorByteCodeGenerator generator = new PojomatorByteCodeGenerator(pojoClass, classProperties);
    Class<?> pojomatorClass = ClassDefinerFactory.getDefiner().defineClass(generator.pojomatorClassName, generator.makeClassBytes());
    @SuppressWarnings("unchecked")
    Pojomator<T> pojomator = (Pojomator<T>) pojomatorClass.getConstructor(Class.class, ClassProperties.class)
      .newInstance(pojoClass, classProperties);
    for (PropertyElement propertyElement: classProperties.getToStringProperties()) {
      setStaticField(
        pojomatorClass,
        PojomatorByteCodeGenerator.propertyFormatterName(propertyElement),
        createPropertyFormatter(propertyElement.getElement()));
    }
    for (PropertyElement propertyElement: classProperties.getAllProperties()) {
      setStaticField(pojomatorClass, PojomatorByteCodeGenerator.propertyElementName(propertyElement), propertyElement);
    }
    return pojomator;
  }

  private static void setStaticField(Class<?> clazz, String fieldName, Object value)
      throws NoSuchFieldException, SecurityException, IllegalAccessException {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static EnhancedPropertyFormatter createPropertyFormatter(AnnotatedElement annotatedElement)
    throws InstantiationException, IllegalAccessException {
    PropertyFormat propertyFormat = annotatedElement.getAnnotation(PropertyFormat.class);
    EnhancedPropertyFormatter propertyFormatter = constructPropertyFormatter(propertyFormat);
    propertyFormatter.initialize(annotatedElement);
    return propertyFormatter;

  }

  private static EnhancedPropertyFormatter constructPropertyFormatter(PropertyFormat propertyFormat)
    throws InstantiationException, IllegalAccessException {
    if (propertyFormat == null) {
      return new DefaultEnhancedPropertyFormatter();
    }
    else {
      if (EnhancedPropertyFormatter.class.isAssignableFrom(propertyFormat.value())) {
        return (EnhancedPropertyFormatter) propertyFormat.value().newInstance();
      }
      else {
        @SuppressWarnings("deprecation")
        EnhancedPropertyFormatterWrapper wrapper = new EnhancedPropertyFormatterWrapper(propertyFormat.value().newInstance());
        return wrapper;
      }
    }
  }
}
