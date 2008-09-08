package org.pojomatic.internal;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.pojomatic.PropertyElement;
import org.pojomatic.TestUtils;
import org.pojomatic.annotations.AutoDetectType;
import org.pojomatic.annotations.AutoProperty;
import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;

public class ClassPropertiesTest {


  @Test
  public void testAnnotatedFields() throws Exception {
    final PropertyElement privateStringField = TestUtils.field(FieldPojo.class, "privateString");
    final PropertyElement publicIntField = TestUtils.field(FieldPojo.class, "publicInt");
    final PropertyElement onlyForStringField = TestUtils.field(FieldPojo.class, "onlyForToString");

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

    final PropertyElement stringField = TestUtils.field(AutoFieldPojo.class, "string");
    final PropertyElement allInDoubleField = TestUtils.field(AutoFieldPojo.class, "allInDouble");

    ClassProperties classProperties = ClassProperties.createInstance(AutoFieldPojo.class);

    assertEquals(asSet(allInDoubleField), asSet(classProperties.getEqualsProperties()));
    assertEquals(asSet(allInDoubleField), asSet(classProperties.getHashCodeProperties()));
    assertEquals(
      asSet(stringField, allInDoubleField),
      asSet(classProperties.getToStringProperties()));
  }

  @Test
  public void testAnnotatedMethods() throws Exception {
    final PropertyElement getIntMethod = TestUtils.method(MethodPojo.class, "getInt");
    final PropertyElement privateStringMethod = TestUtils.method(MethodPojo.class, "privateString");
    final PropertyElement onlyForEqualsMethod = TestUtils.method(MethodPojo.class, "onlyForEquals");

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
      TestUtils.method(AutoMethodPojo.class, "getInt"),
      TestUtils.method(AutoMethodPojo.class, "isBoolean"),
      TestUtils.method(AutoMethodPojo.class, "is_boolean"),
      TestUtils.method(AutoMethodPojo.class, "get_int"));
    final Set<PropertyElement> equalsHashCodeProperties =
      new HashSet<PropertyElement>(commonProperties);
    equalsHashCodeProperties.add(TestUtils.method(AutoMethodPojo.class, "getHashCodeAndEquals"));

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

  @Test
  public void testAnnotatedInheritance() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.createInstance(ParentPojo.class);
    assertEquals(expectedParent, asSet(parentClassProperties.getEqualsProperties()));
    assertEquals(expectedParent, asSet(parentClassProperties.getHashCodeProperties()));
    assertEquals(expectedParent, asSet(parentClassProperties.getToStringProperties()));

    ClassProperties childClassProperties = ClassProperties.createInstance(ChildPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.method(ParentPojo.class, "getFoo"), TestUtils.field(ChildPojo.class, "other"));
    assertEquals(expectedChild, asSet(childClassProperties.getEqualsProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getHashCodeProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getToStringProperties()));
  }

  @Test
  public void testAutoInheritanceBothAuto() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentAutoPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.createInstance(ParentAutoPojo.class);
    assertEquals(expectedParent, asSet(parentClassProperties.getEqualsProperties()));
    assertEquals(Collections.EMPTY_SET, asSet(parentClassProperties.getHashCodeProperties()));
    assertEquals(Collections.EMPTY_SET, asSet(parentClassProperties.getToStringProperties()));

    ClassProperties childClassProperties = ClassProperties.createInstance(ChildAutoPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.field(ChildAutoPojo.class, "other"));
    assertEquals(expectedParent, asSet(childClassProperties.getEqualsProperties()));
    assertEquals(Collections.EMPTY_SET, asSet(childClassProperties.getHashCodeProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getToStringProperties()));
  }

  @Test
  public void testAutoInheritanceAnnotatedParent() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.createInstance(ParentPojo.class);
    assertEquals(expectedParent, asSet(parentClassProperties.getEqualsProperties()));
    assertEquals(expectedParent, asSet(parentClassProperties.getHashCodeProperties()));
    assertEquals(expectedParent, asSet(parentClassProperties.getToStringProperties()));

    ClassProperties childClassProperties = ClassProperties.createInstance(ChildExtendsAnnotatedPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.method(ParentPojo.class, "getFoo"),
      TestUtils.method(ChildExtendsAnnotatedPojo.class, "getFoo"),
      TestUtils.method(ChildExtendsAnnotatedPojo.class, "getMyString"));
    assertEquals(expectedChild, asSet(childClassProperties.getEqualsProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getHashCodeProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getToStringProperties()));
  }

  @Test
  public void testAutoInheritanceAutoParentAnnotatedChild() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentAutoPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.createInstance(ParentAutoPojo.class);
    assertEquals(expectedParent, asSet(parentClassProperties.getEqualsProperties()));
    assertEquals(Collections.EMPTY_SET, asSet(parentClassProperties.getHashCodeProperties()));
    assertEquals(Collections.EMPTY_SET, asSet(parentClassProperties.getToStringProperties()));

    ClassProperties childClassProperties = ClassProperties.createInstance(ChildExtendsAutoPojo.class);
    Set<PropertyElement> expectedChildEquals = asSet(
      TestUtils.method(ParentAutoPojo.class, "getFoo"),
      TestUtils.field(ChildExtendsAutoPojo.class, "other"));
    assertEquals(expectedChildEquals, asSet(childClassProperties.getEqualsProperties()));
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.field(ChildExtendsAutoPojo.class, "other"));
    assertEquals(expectedChild, asSet(childClassProperties.getHashCodeProperties()));
    assertEquals(expectedChild, asSet(childClassProperties.getToStringProperties()));
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

  @AutoProperty(policy=DefaultPojomaticPolicy.TO_STRING)
  public static class AutoFieldPojo {
    public String string;

    @Property(policy=PojomaticPolicy.NONE)
    public int ignoredInt;

    @Property(policy=PojomaticPolicy.ALL)
    public double allInDouble;

    /* Methods are not auto-detected */
    public float getNotDetected() { return 1f; }
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

  @AutoProperty(autoDetect=AutoDetectType.METHOD, policy=DefaultPojomaticPolicy.ALL)
  public static class AutoMethodPojo {
    /* Fields are not auto-detected */
    int notDetected;

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

  private static abstract class ParentPojo {
    @Property
    public abstract int getFoo();
  }

  private static class ChildPojo extends ParentPojo {
    @Property
    public String other;

    @Override public int getFoo() { return 2; }
  }

  @AutoProperty(autoDetect=AutoDetectType.METHOD)
  private static class ChildExtendsAnnotatedPojo extends ParentPojo {
    @Override
    public int getFoo() { return 0; }

    public String getMyString() { return "foo"; }
  }

  @AutoProperty(autoDetect=AutoDetectType.METHOD, policy=DefaultPojomaticPolicy.EQUALS)
  private static abstract class ParentAutoPojo {
    public abstract int getFoo();
  }

  @AutoProperty(autoDetect=AutoDetectType.FIELD, policy=DefaultPojomaticPolicy.TO_STRING)
  private static class ChildAutoPojo extends ParentAutoPojo {
    public String other;

    @Override public int getFoo() { return 2; }
  }

  private static class ChildExtendsAutoPojo extends ParentAutoPojo {
    @Property
    public String other;

    @Override public int getFoo() { return 2; }

    public String getBar() { return ""; }
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

