package org.pojomatic.internal;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.pojomatic.PropertyElement;
import org.pojomatic.PropertyField;
import org.pojomatic.annotations.Property;

public class ClassPropertiesTest {


  @Test
  public void testSimpleFields() throws Exception {
    ClassProperties<FieldPojo> classProperties = ClassProperties.createInstance(FieldPojo.class);
    Set<PropertyElement<?>> expected =
      asSet(field(FieldPojo.class, "privateString"), field(FieldPojo.class, "publicInt"));
    assertEquals(expected, new HashSet<PropertyElement<?>>(classProperties.getEqualsProperties()));
    assertEquals(expected, new HashSet<PropertyElement<?>>(classProperties.getHashCodeProperties()));
    assertEquals(expected, new HashSet<PropertyElement<?>>(classProperties.getToStringProperties()));
  }

  private static Set<PropertyElement<?>> asSet(PropertyElement<?>... elements) {
    HashSet<PropertyElement<?>> result = new HashSet<PropertyElement<?>>();
    for (PropertyElement<?> element : elements) {
      result.add(element);
    }
    return result;
  }

  private static <T> PropertyElement<T> field(Class<T> clazz, String fieldName) throws Exception {
    return new PropertyField<T>(clazz.getDeclaredField(fieldName));
  }

  public static class FieldPojo {
    @SuppressWarnings("unused")
    @Property
    private String privateString;

    @Property
    public int publicInt;
  }


}
