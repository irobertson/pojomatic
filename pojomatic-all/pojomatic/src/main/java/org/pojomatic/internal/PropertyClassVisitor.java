package org.pojomatic.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pojomatic.PropertyElement;

class PropertyClassVisitor extends ClassVisitor {

  private final Map<PropertyRole, Map<String, PropertyElement>> fieldsMap;
  private final Map<PropertyRole, Map<String, PropertyElement>> methodsMap;
  private final Map<PropertyRole, List<PropertyElement>> sortedProperties = makeProperties();

  PropertyClassVisitor(
      Map<PropertyRole, Map<String, PropertyElement>> fieldsMap,
      Map<PropertyRole, Map<String, PropertyElement>> methodsMap) {
    super(Opcodes.ASM4);
    this.fieldsMap = fieldsMap;
    this.methodsMap = methodsMap;

  }

  static PropertyClassVisitor visitClass(
      Class<?> clazz,
      Map<PropertyRole, Map<String, PropertyElement>> fieldsMap,
      Map<PropertyRole, Map<String, PropertyElement>> methodsMap) {
    URL location = null;
    try {
      location = clazz.getProtectionDomain().getCodeSource().getLocation();
    }
    catch (NullPointerException e) {
      return null;
    }
    try {
      URL url = new URL(location, clazz.getName().replace(".", "/") + ".class");
      try (InputStream stream = url.openStream()) {
        ClassReader classReader = new ClassReader(stream);
        PropertyClassVisitor propertyClassVisitor = new PropertyClassVisitor(fieldsMap, methodsMap);
        classReader.accept(propertyClassVisitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        verifyAllPropertiesFound(clazz, fieldsMap, methodsMap, propertyClassVisitor);
        return propertyClassVisitor;
      }
    } catch (IOException e) {
      //FIXME - should we hook into a logger here?
      return null;
    }
  }

  private static void verifyAllPropertiesFound(Class<?> clazz,
      Map<PropertyRole, Map<String, PropertyElement>> fieldsMap,
      Map<PropertyRole, Map<String, PropertyElement>> methodsMap,
      PropertyClassVisitor propertyClassVisitor) {
    for (PropertyRole role: PropertyRole.values()) {
      List<PropertyElement> sortedProperties = propertyClassVisitor.getSortedProperties().get(role);
      Map<String, PropertyElement> fields = fieldsMap.get(role);
      Map<String, PropertyElement> methods = methodsMap.get(role);
      if (fields.size() + methods.size() != sortedProperties.size()) {
        throwReflectionMissmatch(clazz, fields, methods, sortedProperties);
      }
    }
  }

  private static void throwReflectionMissmatch(
      Class<?> clazz,
      Map<String, PropertyElement> fields,
      Map<String, PropertyElement> methods,
      List<PropertyElement> sortedProperties) {
    Set<PropertyElement> expectedProperties = new LinkedHashSet<>();
    expectedProperties.addAll(fields.values());
    expectedProperties.addAll(methods.values());
    for (PropertyElement property: sortedProperties) {
      expectedProperties.remove(property);
    }
    StringBuilder message = new StringBuilder("In class ").append(clazz.getName()).append(", properties ");
    boolean seenOne = false;
    for (PropertyElement property: expectedProperties) {
      if (seenOne) {
        message.append(", ");
      }
      seenOne = true;
      message.append(property);
    }
    message.append(" were found in reflection, but not when visiting the bytecode");
    throw new IllegalStateException(message.toString());
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    if ((access & Opcodes.ACC_STATIC )== 0) {
      for (PropertyRole role: PropertyRole.values()) {
        PropertyElement propertyElement = fieldsMap.get(role).get(name);
        if (propertyElement != null) {
          sortedProperties.get(role).add(propertyElement);
        }
      }
    }
    return null;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    if (desc.startsWith("()") && (access & Opcodes.ACC_STATIC) == 0) {
      for (PropertyRole role: PropertyRole.values()) {
        PropertyElement propertyElement = methodsMap.get(role).get(name);
        if (propertyElement != null) {
          sortedProperties.get(role).add(propertyElement);
        }
      }
    }
    return null;
  }

  public Map<PropertyRole, List<PropertyElement>> getSortedProperties() {
    return sortedProperties;
  };

  private static Map<PropertyRole, List<PropertyElement>> makeProperties() {
    Map<PropertyRole, List<PropertyElement>> properties =
        new EnumMap<>(PropertyRole.class);
    for (PropertyRole role : PropertyRole.values()) {
      properties.put(role, new ArrayList<PropertyElement>());
    }
    return properties;
  }
}
