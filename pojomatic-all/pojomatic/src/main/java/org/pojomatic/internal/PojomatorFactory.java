package org.pojomatic.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.pojomatic.Pojomator;
import org.pojomatic.PropertyElement;
import org.pojomatic.annotations.PropertyFormat;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;

public class PojomatorFactory {

  private static final class DynamicClassLoader extends ClassLoader {
    private DynamicClassLoader(ClassLoader parent) {
      super(parent);
    }

    Class<?> loadClass(String name, byte[] classBytes) {
      return defineClass(name, classBytes, 0, classBytes.length);
    }
  }

  private static volatile DynamicClassLoader CLASS_LOADER = null;
  private final static Object CLASS_LOADER_MUTEX = new Object();

  // We don't initialize CLASS_LOADER at classLoad time because permission issues could cause an exception; we don't
  // want that exception to be masked with a ClassNotFoundException
  private static DynamicClassLoader getClassLoader() {
    if (CLASS_LOADER == null) {
      synchronized (CLASS_LOADER_MUTEX) {
        if (CLASS_LOADER == null) {
          CLASS_LOADER =
            AccessController.doPrivileged(new PrivilegedAction<DynamicClassLoader>() {
              @Override
              public DynamicClassLoader run() {
                return new DynamicClassLoader(PojomatorFactory.class.getClassLoader());
              }
            });
        }
      }
    }
    return CLASS_LOADER;
  }

  public static <T> Pojomator<T> makePojomator(Class<T> pojoClass) {
    try {
      return makePojomatorChecked(pojoClass);
    } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException
        | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  private static <T> Pojomator<T> makePojomatorChecked(Class<T> pojoClass)
      throws IllegalAccessException, NoSuchFieldException, SecurityException, InstantiationException,
      InvocationTargetException, NoSuchMethodException {
    ClassProperties classProperties = ClassProperties.forClass(pojoClass);
    PojomatorByteCodeGenerator generator = new PojomatorByteCodeGenerator(pojoClass, classProperties);
    Class<?> pojomatorClass = getClassLoader().loadClass(generator.pojomatorClassName, generator.makeClassBytes());
    setStaticField(pojomatorClass, PojomatorByteCodeGenerator.POJO_CLASS_FIELD_NAME, pojoClass);
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
