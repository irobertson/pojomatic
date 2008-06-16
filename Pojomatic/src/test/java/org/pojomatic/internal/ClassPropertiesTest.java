package org.pojomatic.internal;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
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

  private static PropertyElement field(Class<?> clazz, String fieldName) throws Exception {
    return new PropertyField(clazz.getDeclaredField(fieldName));
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

}
