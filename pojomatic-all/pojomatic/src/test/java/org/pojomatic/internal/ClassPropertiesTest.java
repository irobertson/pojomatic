package org.pojomatic.internal;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import java.util.*;

import org.pojomatic.PropertyElement;
import org.pojomatic.TestUtils;
import org.pojomatic.annotations.*;
import org.pojomatic.internal.a.C1;
import org.pojomatic.internal.b.C2;
import org.pojomatic.internal.b.C4;

public class ClassPropertiesTest {
  @Test public void testForClass() {
    ClassProperties interfaceProperties = ClassProperties.forClass(Interface.class);
    assertSame(ClassProperties.forClass(Interface.class), interfaceProperties);
  }

  @Test
  public void testAnnotatedFields() throws Exception {
    final PropertyElement privateStringField = TestUtils.field(FieldPojo.class, "privateString");
    final PropertyElement publicIntField = TestUtils.field(FieldPojo.class, "publicInt");
    final PropertyElement onlyForStringField = TestUtils.field(FieldPojo.class, "onlyForToString");
    final PropertyElement forEqualsAndToString =
      TestUtils.field(FieldPojo.class, "forEqualsAndToString");

    ClassProperties classProperties = ClassProperties.forClass(FieldPojo.class);

    assertEquals(asSet(classProperties.getEqualsProperties()), asSet(privateStringField, publicIntField, forEqualsAndToString));
    assertEquals(asSet(classProperties.getHashCodeProperties()), asSet(privateStringField, publicIntField));
    assertEquals(asSet(classProperties.getToStringProperties()), asSet(privateStringField, publicIntField, onlyForStringField, forEqualsAndToString));

    assertEquals(classProperties.getAllProperties(), asSet(privateStringField, publicIntField, onlyForStringField, forEqualsAndToString));
  }

  @Test
  public void testAutoFields() throws Exception {

    final PropertyElement stringField = TestUtils.field(AutoFieldPojo.class, "string");
    final PropertyElement allInDoubleField = TestUtils.field(AutoFieldPojo.class, "allInDouble");

    ClassProperties classProperties = ClassProperties.forClass(AutoFieldPojo.class);

    assertEquals(asSet(classProperties.getEqualsProperties()), asSet(allInDoubleField));
    assertEquals(asSet(classProperties.getHashCodeProperties()), asSet(allInDoubleField));
    assertEquals(asSet(classProperties.getToStringProperties()), asSet(stringField, allInDoubleField));
  }

  @Test
  public void testAnnotatedMethods() throws Exception {
    class MethodPojo {
      @Property public int getInt() { return 0; }
      @Property private String privateString() { return null; }
      @Property(policy=PojomaticPolicy.EQUALS) public double onlyForEquals() { return 0.0; }
    }

    final PropertyElement getIntMethod = TestUtils.method(MethodPojo.class, "getInt");
    final PropertyElement privateStringMethod = TestUtils.method(MethodPojo.class, "privateString");
    final PropertyElement onlyForEqualsMethod = TestUtils.method(MethodPojo.class, "onlyForEquals");

    ClassProperties classProperties = ClassProperties.forClass(MethodPojo.class);

    assertEquals(asSet(classProperties.getEqualsProperties()), asSet(getIntMethod, privateStringMethod, onlyForEqualsMethod));
    assertEquals(asSet(classProperties.getHashCodeProperties()), asSet(getIntMethod, privateStringMethod));
    assertEquals(asSet(classProperties.getToStringProperties()), asSet(getIntMethod, privateStringMethod));
  }

  @Test
  public void testAutoMethods() throws Exception {
    final Set<PropertyElement> commonProperties = asSet(
      TestUtils.method(AutoMethodPojo.class, "getInt"),
      TestUtils.method(AutoMethodPojo.class, "isBoolean"),
      TestUtils.method(AutoMethodPojo.class, "is_boolean"),
      TestUtils.method(AutoMethodPojo.class, "get_int"));
    final Set<PropertyElement> equalsHashCodeProperties = new HashSet<>(commonProperties);
    equalsHashCodeProperties.add(TestUtils.method(AutoMethodPojo.class, "getHashCodeAndEquals"));

    ClassProperties classProperties = ClassProperties.forClass(AutoMethodPojo.class);

    assertEquals(asSet(classProperties.getEqualsProperties()), equalsHashCodeProperties);
    assertEquals(asSet(classProperties.getHashCodeProperties()), equalsHashCodeProperties);
    assertEquals(asSet(classProperties.getToStringProperties()), commonProperties);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void testAnnotatedMethodReturningVoid() {
    class MethodReturnsVoidPojo { @Property public void noReturn() {} }
    ClassProperties.forClass(MethodReturnsVoidPojo.class);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void testAnnotatedMethodTakingArgs() {
    class MethodTakesArgsPojo {
      @Property public int takesArgs(String death) {
        return death.length(); }
    }
    ClassProperties.forClass(MethodTakesArgsPojo.class);
  }

  @Test
  public void testAnnotatedInheritance() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.forClass(ParentPojo.class);
    assertEquals(asSet(parentClassProperties.getEqualsProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getHashCodeProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getToStringProperties()), expectedParent);

    ClassProperties childClassProperties = ClassProperties.forClass(ChildPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.method(ParentPojo.class, "getFoo"), TestUtils.field(ChildPojo.class, "other"));
    assertEquals(asSet(childClassProperties.getEqualsProperties()), expectedChild);
    assertEquals(asSet(childClassProperties.getHashCodeProperties()), expectedChild);
    assertEquals(asSet(childClassProperties.getToStringProperties()), expectedChild);
  }

  @Test
  public void testAutoInheritanceBothAuto() throws Exception {
    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentAutoPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.forClass(ParentAutoPojo.class);
    assertEquals(asSet(parentClassProperties.getEqualsProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getHashCodeProperties()), Collections.EMPTY_SET);
    assertEquals(asSet(parentClassProperties.getToStringProperties()), Collections.EMPTY_SET);

    ClassProperties childClassProperties = ClassProperties.forClass(ChildAutoFieldPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.field(ChildAutoFieldPojo.class, "other"));
    assertEquals(asSet(childClassProperties.getEqualsProperties()), expectedParent);
    assertEquals(asSet(childClassProperties.getHashCodeProperties()), Collections.EMPTY_SET);
    assertEquals(asSet(childClassProperties.getToStringProperties()), expectedChild);
  }

  @Test
  public void testAutoInheritanceWithOverride() throws Exception {
    @AutoProperty(autoDetect=AutoDetectPolicy.METHOD)
    class ChildAutoMethodPojo extends ParentPojo {
      @Override public int getFoo() { return 2; }
      @SuppressWarnings("unused") public int getBar() { return 2; }
    }

    ClassProperties childClassProperties = ClassProperties.forClass(ChildAutoMethodPojo.class);
    Set<PropertyElement> expected = asSet(
      TestUtils.method(ParentPojo.class, "getFoo"),
      TestUtils.method(ChildAutoMethodPojo.class, "getBar"));
    assertEquals(asSet(childClassProperties.getEqualsProperties()), expected);
    assertEquals(asSet(childClassProperties.getHashCodeProperties()), expected);
    assertEquals(asSet(childClassProperties.getToStringProperties()), expected);
  }

  @Test
  public void testAutoInheritanceAnnotatedParent() throws Exception {
    @AutoProperty(autoDetect=AutoDetectPolicy.METHOD)
    class ChildExtendsAnnotatedPojo extends ParentPojo {
      @Override public int getFoo() { return 0; }
      @SuppressWarnings("unused") public String getMyString() { return "foo"; }
    }

    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.forClass(ParentPojo.class);
    assertEquals(asSet(parentClassProperties.getEqualsProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getHashCodeProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getToStringProperties()), expectedParent);

    ClassProperties childClassProperties = ClassProperties.forClass(ChildExtendsAnnotatedPojo.class);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.method(ParentPojo.class, "getFoo"),
      TestUtils.method(ChildExtendsAnnotatedPojo.class, "getMyString"));
    assertEquals(asSet(childClassProperties.getEqualsProperties()), expectedChild);
    assertEquals(asSet(childClassProperties.getHashCodeProperties()), expectedChild);
    assertEquals(asSet(childClassProperties.getToStringProperties()), expectedChild);
  }

  @Test
  public void testAutoInheritanceAutoParentAnnotatedChild() throws Exception {
    class ChildExtendsAutoPojo extends ParentAutoPojo {
      @Property public String other;
      @Override public int getFoo() { return 2; }
      @SuppressWarnings("unused") public String getBar() { return ""; }
    }

    Set<PropertyElement> expectedParent = asSet(TestUtils.method(ParentAutoPojo.class, "getFoo"));
    ClassProperties parentClassProperties = ClassProperties.forClass(ParentAutoPojo.class);
    assertEquals(asSet(parentClassProperties.getEqualsProperties()), expectedParent);
    assertEquals(asSet(parentClassProperties.getHashCodeProperties()), Collections.EMPTY_SET);
    assertEquals(asSet(parentClassProperties.getToStringProperties()), Collections.EMPTY_SET);

    ClassProperties childClassProperties = ClassProperties.forClass(ChildExtendsAutoPojo.class);
    Set<PropertyElement> expectedChildEquals = asSet(
      TestUtils.method(ParentAutoPojo.class, "getFoo"),
      TestUtils.field(ChildExtendsAutoPojo.class, "other"));
    assertEquals(asSet(childClassProperties.getEqualsProperties()), expectedChildEquals);
    Set<PropertyElement> expectedChild = asSet(
      TestUtils.field(ChildExtendsAutoPojo.class, "other"));
    assertEquals(asSet(childClassProperties.getHashCodeProperties()), expectedChild);
    assertEquals(asSet(childClassProperties.getToStringProperties()), expectedChild);
  }

  @Test
  public void testOverriddenMethods() throws Exception {
    ClassProperties classProperties = ClassProperties.forClass(C4.class);
    assertEquals(asSet(classProperties.getEqualsProperties()), asSet(
    TestUtils.method(C1.class, "packagePrivate"),
    TestUtils.method(C1.class, "packagePrivateOverriddenProtected"),
    TestUtils.method(C1.class, "packagePrivateOverriddenPublic"),
    TestUtils.method(C1.class, "protectedMethod"),
    TestUtils.method(C1.class, "publicMethod"),
    TestUtils.method(C2.class, "packagePrivate"),
    TestUtils.method(C2.class, "packagePrivateOverriddenProtected"),
    TestUtils.method(C2.class, "packagePrivateOverriddenPublic")));
  }

  @Test
  public void testAnnotatedStaticField() {
    try {
      ClassProperties.forClass(StaticField.class);
      fail("Exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Static field " + StaticField.class.getName() + ".a is annotated with @Property");
    }
  }

  @Test
  public void testSyntheticMethod() throws Exception {
    assertEquals (
      asSet(ClassProperties.forClass(Synthetic.class).getEqualsProperties()),
      asSet(TestUtils.method(Synthetic.class, "getA")));
  }

  @Test
  public void testAnnotatedStaticMethod() {
    try {
      ClassProperties.forClass(StaticMethod.class);
      fail("Exception expected");
    }
    catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Static method " + StaticMethod.class.getName() + ".a() is annotated with @Property");
    }
  }

  @Test public void testInterface() throws Exception {
    ClassProperties classProperties = ClassProperties.forClass(Interface.class);
    PropertyElement getFoo = TestUtils.method(Interface.class, "getFoo");
    PropertyElement baz = TestUtils.method(Interface.class, "baz");
    assertEquals(asSet(classProperties.getHashCodeProperties()), asSet(getFoo));
    assertEquals(asSet(classProperties.getToStringProperties()), asSet(getFoo));
    assertEquals(asSet(classProperties.getEqualsProperties()), asSet(getFoo, baz));
  }

  @Test public void testIsCompatibleForEquals() {
    class Parent { @Property int getX() { return 3; } }
    class NonContributingChild extends Parent {}
    @OverridesEquals class AnnotatedNonContributingChild extends Parent {}
    class ContributingChild extends Parent{ @Property int getY() { return 3; } }
    class ChildOfContributingChild extends ContributingChild{}

    List<List<Class<?>>> partitions = Arrays.asList(
      Arrays.<Class<?>>asList(Parent.class, NonContributingChild.class),
      Arrays.<Class<?>>asList(ContributingChild.class, ChildOfContributingChild.class),
      Arrays.<Class<?>>asList(AnnotatedNonContributingChild.class),
      Arrays.<Class<?>>asList(Interface.class));
    for (List<Class<?>> partition: partitions) {
      for (Class<?> clazz: partition) {
        for(List<Class<?>> otherPartition: partitions) {
          for(Class<?> otherClazz: otherPartition) {
            if (partition == otherPartition) {
              assertTrue(ClassProperties.forClass(clazz).isCompatibleForEquals(otherClazz));
            }
            else {
              assertFalse(ClassProperties.forClass(clazz).isCompatibleForEquals(otherClazz));
            }
          }
        }
      }
    }
  }

  @Test public void testSubclassCannotOverrideEquals() {
    class ChildOfInterface implements Interface {
      @Override
      public int getFoo() { return 0; }
      @Override
      public int bar() { return 0; }
      @Override
      public int baz() { return 0; }
    }

    assertTrue(ClassProperties.forClass(Interface.class).isCompatibleForEquals(ChildOfInterface.class));

    @SubclassCannotOverrideEquals class A { @Property int x; }
    class B extends A { @Property int y; }
    assertTrue(ClassProperties.forClass(A.class).isCompatibleForEquals(B.class));
    assertFalse(ClassProperties.forClass(B.class).isCompatibleForEquals(A.class));
  }

  @Test
  public void testMissingClassBytes() throws Exception {
    class Bean {
      @Property int a, b, c;
    }

    ClassOnlyClassLoader classLoader = new ClassOnlyClassLoader(Bean.class.getClassLoader());
    Class<?> beanClass = classLoader.loadClass(Bean.class.getName());
    ClassProperties classProperties = ClassProperties.forClass(beanClass);
    assertEquals(classProperties.getEqualsProperties().size(), 3);
  }

  //Not all classes can be made internal.  In particular, autodetect=FIELD classes cannot, because of the synthetic
  //$this, and classes requiring static elements cannot.

  public static class FieldPojo {
    @Property
    private String privateString;

    @Property
    public int publicInt;

    @Property(policy=PojomaticPolicy.TO_STRING)
    private int onlyForToString;

    @Property(policy=PojomaticPolicy.EQUALS_TO_STRING)
    private int forEqualsAndToString;
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

    /* static fields are not detected */
    public static String staticField;
  }

  @AutoProperty(autoDetect=AutoDetectPolicy.METHOD, policy=DefaultPojomaticPolicy.ALL)
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
    public int gettyIsAMuseum() { return 1; }
    public String thisIsNotAGetter() { return "really, it's not"; }

    // some methods we should not grab
    public void getHello() {}
    public int getTriple(int arg) { return arg * 3; }


    /* static fields are not detected */
    public static String getStatic() { return null; }
  }

  private static abstract class ParentPojo {
    @Property
    public abstract int getFoo();
  }

  public static class ChildPojo extends ParentPojo {
    @Property
    public String other;

    @Override public int getFoo() { return 2; }
  }

  @AutoProperty(autoDetect=AutoDetectPolicy.METHOD, policy=DefaultPojomaticPolicy.EQUALS)
  private static abstract class ParentAutoPojo {
    public abstract int getFoo();
  }

  @AutoProperty(autoDetect=AutoDetectPolicy.FIELD, policy=DefaultPojomaticPolicy.TO_STRING)
  public static class ChildAutoFieldPojo extends ParentAutoPojo {
    public String other;

    @Override public int getFoo() { return 2; }
  }

  public static class ParentOfSynthetic {
    public Number getA() { return null; }
  }

  public static class Synthetic extends ParentOfSynthetic {
    @Override @Property public Integer getA() { return 3; }
  }
  @AutoProperty(autoDetect=AutoDetectPolicy.METHOD)
  public static interface Interface {
    int getFoo();
    int bar();
    @Property(policy=PojomaticPolicy.EQUALS) int baz();
  }

  public static class StaticField {
    @Property public static int a;
  }

  public static class StaticMethod {
    @Property public static int a() { return 1; }
  }

  private static Set<PropertyElement> asSet(PropertyElement... elements) {
    return new HashSet<>(Arrays.asList(elements));
  }

  private static Set<PropertyElement> asSet(Collection<PropertyElement> elements) {
    return new HashSet<>(elements);
  }
}

