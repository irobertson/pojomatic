package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.pojomatic.PropertyAccessor;
import org.pojomatic.PropertyElement;
import org.pojomatic.PropertyField;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.PojomaticDefaultPolicy;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

public class ClassPropertiesTest {


  @Test
  public void testSimpleFields() throws Exception {
    final PropertyElement privateStringField = field(FieldPojo.class, "privateString");
    final PropertyElement publicIntField = field(FieldPojo.class, "publicInt");
    final PropertyElement onlyForStringField = field(FieldPojo.class, "onlyForToString");

    ClassProperties classProperties = ClassProperties.createInstance(FieldPojo.class);

    Set<PropertyElement> expected = asSet(privateStringField, publicIntField);
    assertEquals(expected, asSet(classProperties.getEqualsProperties()));
    assertEquals(expected, asSet(classProperties.getHashCodeProperties()));
    assertEquals(
      asSet(privateStringField, publicIntField, onlyForStringField),
      asSet(classProperties.getToStringProperties()));
  }

  @Test
  public void testAutoFields() throws Exception {

    final PropertyElement stringField = field(AutoFieldPojo.class, "string");
    final PropertyElement allInDoubleField = field(AutoFieldPojo.class, "allInDouble");

    ClassProperties classProperties = ClassProperties.createInstance(AutoFieldPojo.class);

    assertEquals(asSet(allInDoubleField), asSet(classProperties.getEqualsProperties()));
    assertEquals(asSet(allInDoubleField), asSet(classProperties.getHashCodeProperties()));
    assertEquals(
      asSet(stringField, allInDoubleField),
      asSet(classProperties.getToStringProperties()));
  }

  @Test
  public void testSimpleMethods() throws Exception {
    final PropertyElement getIntMethod = method(MethodPojo.class, "getInt");
    final PropertyElement privateStringMethod = method(MethodPojo.class, "privateString");
    final PropertyElement onlyForEqualsMethod = method(MethodPojo.class, "onlyForEquals");

    ClassProperties classProperties = ClassProperties.createInstance(MethodPojo.class);

    assertEquals(
      asSet(getIntMethod, privateStringMethod, onlyForEqualsMethod),
      asSet(classProperties.getEqualsProperties()));
    assertEquals(
      asSet(getIntMethod, privateStringMethod),
      asSet(classProperties.getHashCodeProperties()));
    assertEquals(
      asSet(getIntMethod, privateStringMethod),
      asSet(classProperties.getToStringProperties()));
  }

  @Test
  public void testAutoMethods() throws Exception {
    final Set<PropertyElement> commonProperties = asSet(
      method(AutoMethodPojo.class, "getInt"),
      method(AutoMethodPojo.class, "isBoolean"),
      method(AutoMethodPojo.class, "is_boolean"),
      method(AutoMethodPojo.class, "get_int"));
    final Set<PropertyElement> equalsHashCodeProperties =
      new HashSet<PropertyElement>(commonProperties);
    equalsHashCodeProperties.add(method(AutoMethodPojo.class, "getHashCodeAndEquals"));

    ClassProperties classProperties = ClassProperties.createInstance(AutoMethodPojo.class);

    assertEquals(equalsHashCodeProperties, asSet(classProperties.getEqualsProperties()));
    assertEquals(equalsHashCodeProperties, asSet(classProperties.getHashCodeProperties()));
    assertEquals(commonProperties, asSet(classProperties.getToStringProperties()));
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAnnotatedMethodReturningVoid() {
      ClassProperties.createInstance(MethodReturnsVoidPojo.class);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testAnnotatedMethodTakingArgs() {
      ClassProperties.createInstance(MethodTakesArgsPojo.class);
  }

  private static PropertyElement field(Class<?> clazz, String fieldName) throws Exception {
    return new PropertyField(clazz.getDeclaredField(fieldName));
  }

  private static PropertyElement method(Class<?> clazz, String methodName) throws Exception {
    return new PropertyAccessor(clazz.getDeclaredMethod(methodName));
  }

  public static class FieldPojo {
    @SuppressWarnings("unused")
    @Property
    private String privateString;

    @Property
    public int publicInt;

    @SuppressWarnings("unused")
    @Property(policy=PojomaticPolicy.TO_STRING)
    private int onlyForToString;
  }

  @AutoProperty(policy=PojomaticDefaultPolicy.TO_STRING)
  public static class AutoFieldPojo {
    public String string;

    @Property(policy=PojomaticPolicy.NONE)
    public int ignoredInt;

    @Property(policy=PojomaticPolicy.ALL)
    public double allInDouble;
  }

  public static class MethodPojo {
    @Property
    public int getInt() { return 0; }

    @Property
    @SuppressWarnings("unused")
    private String privateString() { return null; }

    @Property(policy=PojomaticPolicy.EQUALS)
    public double onlyForEquals() { return 0.0; }
  }

  @AutoProperty(policy=PojomaticDefaultPolicy.ALL)
  public static class AutoMethodPojo {
    @Property(policy=PojomaticPolicy.NONE)
    public String getIgnored() { return null; }

    @Property(policy=PojomaticPolicy.HASHCODE_EQUALS)
    public double getHashCodeAndEquals() { return 0.0; }

    // getter variants
    public int getInt() { return 0; }
    public boolean isBoolean() { return true; }
    public boolean is_boolean() { return true; }
    public int get_int() { return 0; }

    // not getters
    public boolean isaEnabled() { return true; }
    public int gettyIsAMueseum() { return 1; }
    public String thisIsNotAGetter() { return "really, it's not"; }

    // some methods we should not grab
    public void getHello() {}
    public int getTripple(int arg) { return arg * 3; }
  }

  public static class MethodReturnsVoidPojo {
    @Property
    public void noReturn() {}
  }

  public static class MethodTakesArgsPojo {
    @Property
    public int takesArgs(String death) { return death.length(); }
  }

  private static Set<PropertyElement> asSet(PropertyElement... elements) {
    HashSet<PropertyElement> result = new HashSet<PropertyElement>();
    for (PropertyElement element : elements) {
      result.add(element);
    }
    return result;
  }

  private static Set<PropertyElement> asSet(Collection<PropertyElement> elements) {
    return new HashSet<PropertyElement>(elements);
  }
}

