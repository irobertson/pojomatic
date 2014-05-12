package org.pojomatic.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.testng.annotations.DataProvider;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class TypeProviders {
  @DataProvider(name = "types")
  public static Object[][] types() {
    return FluentIterable.from(Iterables.concat(simpleTypes(), simpleArrays()))
      .transform(ArrayWrap.INSTANCE)
      .toArray(Object[].class);
  }

  @DataProvider(name = "arrayTypes")
  public static Object[][] arrayTypes() {
    return annotatedTypes(simpleArrays(), doubleArrays());
  }

  @DataProvider(name = "deepArrayTypes")
  public static Object[][] deepArrayTypes() {
    return annotatedTypes(doubleArrays());
  }

  @SuppressWarnings("unchecked")
  @DataProvider(name = "annotations")
  public static Object[][] annotationCombinations() {
    return FluentIterable.from(Sets.<Object>cartesianProduct(booleans()))
      .transform(ListToArray.INSTANCE)
      .toArray(Object[].class);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private static Object[][] annotatedTypes(Iterable<Type>... types) {
    return FluentIterable.from(
      Sets.cartesianProduct(Sets.newLinkedHashSet(Iterables.concat(types)), booleans()))
      .transform(ListToArray.INSTANCE)
      .toArray(Object[].class);
  }

  static Iterable<Type> simpleArrays() {
    return Iterables.transform(simpleTypes(), Arrayify.INSTANCE);
  }

  static Iterable<Type> doubleArrays() {
    return Iterables.transform(simpleArrays(), Arrayify.INSTANCE);
  }

  private static enum Arrayify implements Function<Type, Type> {
    INSTANCE { @Override public Type apply(Type type) { return new ArrayType(type); } }
  }

  private static enum ArrayWrap implements Function<Type, Object[]> {
    INSTANCE { @Override public Object[] apply(Type type) { return new Object[] { type }; } }
  }

  private static enum ListToArray implements Function<List<Object>, Object[]> {
    INSTANCE { @Override public Object[] apply(List<Object> o) { return o.toArray(); } }
  }

  private static Set<Boolean> booleans() {
    return Sets.newLinkedHashSet(Arrays.asList(false, true));
  }

  private static Iterable<Type> simpleTypes() {
    return Arrays.<Type>asList(BaseType.values());
  }
}
