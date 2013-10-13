package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pojomatic.PropertyElement;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class PropertyClassVisitorTest {
  static class FieldsAndGetters {
    int field1;
    long getter1() { return 0L; }
    String field2;
    long field3;
    boolean is2() { return false; }
    void m3(@SuppressWarnings("unused") int n) {}
    void m4(@SuppressWarnings("unused") int n) {}
  }

  static class Other {
    int n;
  }

  private static enum NameExtractor implements Function<PropertyElement, String> {
    INSTANCE;
    @Override
    public String apply(PropertyElement p) {
      AnnotatedElement element = p.getElement();
      if (element instanceof Method) {
        return ((Method) element).getName();
      } else {
        return ((Field) element).getName();
      }
    }
  }

  private PropertyElement f1;
  private PropertyElement f2;
  private PropertyElement f3;
  private PropertyElement m1;
  private PropertyElement m2;
  private PropertyElement m3;
  private PropertyElement m4;

  private static List<PropertyElement> NO_PROPERTIES = Collections.emptyList();

  @Before
  public void setup() throws Exception {
    // Note that fields will be visited before methods, regardless of declaration order.
    f1 = new PropertyField(FieldsAndGetters.class.getDeclaredField("field1"), "");
    f2 = new PropertyField(FieldsAndGetters.class.getDeclaredField("field2"), "");
    f3 = new PropertyField(Other.class.getDeclaredField("n"), "");
    m1 = new PropertyAccessor(FieldsAndGetters.class.getDeclaredMethod("getter1"), "");
    m2 = new PropertyAccessor(FieldsAndGetters.class.getDeclaredMethod("is2"), "");
    m3 = new PropertyAccessor(FieldsAndGetters.class.getDeclaredMethod("m3", int.class), "");
    m4 = new PropertyAccessor(FieldsAndGetters.class.getDeclaredMethod("m4", int.class), "");
  }

  @Test
  public void testReflectionOrdering() {
    PropertyClassVisitor visitor = PropertyClassVisitor.visitClass(
        FieldsAndGetters.class,
        makeRoleMaps(Arrays.asList(f1, f2), Arrays.asList(f2, f1), NO_PROPERTIES),
        makeRoleMaps(Arrays.asList(m2, m1), Arrays.asList(m1), Arrays.asList(m2)));
    assertNotNull(visitor);
    assertEquals(
      makeRoleLists(Arrays.asList(f1, f2, m1, m2), Arrays.asList(f1, f2, m1), Arrays.asList(m2)),
      visitor.getSortedProperties());
  }

  @Test
  public void testMissingCodeSource() {
    Map<PropertyRole, Map<String, PropertyElement>> roleMaps =
        makeRoleMaps(NO_PROPERTIES, NO_PROPERTIES, NO_PROPERTIES);
    PropertyClassVisitor visitor = PropertyClassVisitor.visitClass(String.class, roleMaps, roleMaps);
    assertNull(visitor);
  }

  @Test
  public void testMissingClassBytes() throws Exception {
    class Bean {}

    ClassOnlyClassLoader classLoader = new ClassOnlyClassLoader(Bean.class.getClassLoader());
    Class<?> beanClass = classLoader.loadClass(Bean.class.getName());
    assertNotSame(beanClass, Bean.class);
    assertNotNull(beanClass.getProtectionDomain().getCodeSource().getLocation());
    Map<PropertyRole, Map<String, PropertyElement>> roleMaps =
        makeRoleMaps(NO_PROPERTIES, NO_PROPERTIES, NO_PROPERTIES);
    PropertyClassVisitor visitor = PropertyClassVisitor.visitClass(String.class, roleMaps, roleMaps);
    assertNull(visitor);
  }

  @Test
  public void testThrowReflectionMissmatch() throws Exception {
    try {

      PropertyClassVisitor.visitClass(
        FieldsAndGetters.class,
        makeRoleMaps(Arrays.asList(f3), NO_PROPERTIES, NO_PROPERTIES),
        makeRoleMaps(Arrays.asList(m3, m4), NO_PROPERTIES, NO_PROPERTIES));
      fail("Exception expected");
    }
    catch (IllegalStateException e) {
      assertEquals(
        "In class " + FieldsAndGetters.class.getName() + ", properties "
        + f3.getElement().toString() + ", " + m3.getElement().toString() + ", " + m4.getElement().toString()
        + " were found in reflection, but not when visiting the bytecode",
        e.getMessage());
    }
  }

  private static Map<PropertyRole, Map<String, PropertyElement>> makeRoleMaps(
      List<PropertyElement> forEquals, List<PropertyElement> forHashCode, List<PropertyElement> forToString) {
    EnumMap<PropertyRole, Map<String, PropertyElement>> enumMap = new EnumMap<>(PropertyRole.class);
    enumMap.put(PropertyRole.EQUALS, Maps.uniqueIndex(forEquals, NameExtractor.INSTANCE));
    enumMap.put(PropertyRole.HASH_CODE, Maps.uniqueIndex(forHashCode, NameExtractor.INSTANCE));
    enumMap.put(PropertyRole.TO_STRING, Maps.uniqueIndex(forToString, NameExtractor.INSTANCE));
    return enumMap;
  }

  private static Map<PropertyRole, List<PropertyElement>> makeRoleLists(
      List<PropertyElement> forEquals, List<PropertyElement> forHashCode, List<PropertyElement> forToString) {
    EnumMap<PropertyRole, List<PropertyElement>> enumMap = new EnumMap<>(PropertyRole.class);
    enumMap.put(PropertyRole.EQUALS, forEquals);
    enumMap.put(PropertyRole.HASH_CODE, forHashCode);
    enumMap.put(PropertyRole.TO_STRING, forToString);
    return enumMap;
  }
}
