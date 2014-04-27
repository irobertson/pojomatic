package org.pojomatic;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.annotations.CanBeArray;
import org.pojomatic.annotations.DeepArray;
import org.pojomatic.annotations.OverridesEquals;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.SubclassCannotOverrideEquals;
import org.pojomatic.diff.Differences;
import org.pojomatic.formatter.DefaultEnhancedPojoFormatter;
import org.pojomatic.formatter.DefaultEnhancedPropertyFormatter;
import org.pojomatic.formatter.EnhancedPojoFormatter;
import org.pojomatic.formatter.EnhancedPropertyFormatter;

/**
 * A provider of the three standard {@code Object} methods,
 * {@link Object#equals(Object)}, {@link Object#hashCode()} and {@link Object#toString()}, as
 * well as a useful method to aid in debugging, {@link #doDiff(Object, Object)}.
 *
 * <h3>Treatment of arrays</h3>
 * When encountering an array, there are three approaches that can be taken:
 * <ul>
 *   <li>
 *     Treat the array as an opaque object reference. Equality and hashCodes will be based on object identity, and
 *     toString will return a relatively unhelpful string like "{@code [Ljava.lang.String;@5195da41}".
 *   </li>
 *   <li>
 *     Treat the array as a a single-dimensional array. Equality, hashCode and toString will also look not at the array
 *     instance itself, but the contents of the array. If elements of the array are arrays themselves, treat those
 *     elements as opaque object references.
 *   </li>
 *   <li>
 *     Treat the array as a potentially multi-dimensional array. If any of the arrays elements are themselves arrays,
 *     look at the elements of those sub-arrays, and so on recursively.
 *   </li>
 * </ul>
 * <p>
 * Which option a Pojomator will choose depends on the declared type of the property and on the presence or absence of a
 * pair of annotations, {@link CanBeArray @CanBeArray} and {@link DeepArray @DeepArray}. Specifically:
 * <ul>
 *   <li>
 *     If the declared type of the property is a multi-dimensional array, then Pojomatic will treat it as such.
 *   </li>
 *   <li>
 *     If the declared type of the property is a single dimensional array, then Pojomatic will treat it as such, unless
 *     the component type of the array is {@link Object} and the property is annotated with
 *     {@link DeepArray @DeepArray}, in which case it will be treated as a potentially multi-dimensional array
 *   </li>
 *   <li>
 *     If the declared type of the property is {@link Object}, then pojomatic will treat values as opaque object
 *     references, unless the property is annotated with {@link CanBeArray @CanBeArray} or
 *     {@link DeepArray @DeepArray}, in which case the Pojomator will treat it as either a single-dimensional or
 *     multiple-dimensional array, depending on whether a {@link DeepArray @DeepArray} annotation is absent or present
 *     on the property.
 *   </li>
 * <ul>
 *
 * @param <T> the class this {@code Pojomator} is generated for.
 */
public interface Pojomator<T> {

  /**
   * Compute the hashCode for a given instance of {@code T}.
   * This is done by computing the hashCode of each property which has a {@link PojomaticPolicy} of
   * {@link PojomaticPolicy#HASHCODE_EQUALS HASHCODE_EQUALS} or {@link PojomaticPolicy#ALL ALL}
   * (using 0 when the property is null), and combining them in a fashion similar to that of
   * {@link List#hashCode()}.
   *
   * @param instance the instance to compute the hashCode for - must not be {@code null}
   * @return the hashCode of {@code instance}
   * @throws NullPointerException if {@code instance} is {@code null}
   * @see Object#hashCode()
   */
  int doHashCode(T instance);

  /**
   * Compute the {@code toString} representation for a given instance of {@code T}.
   * <p>
   * The format used depends on the
   * {@link EnhancedPojoFormatter} used for the POJO, and the {@link EnhancedPropertyFormatter} of each property.
   * <p>
   * For example, suppose a class {@code Person} has properties {@code String name} and
   * {@code int age} which are included in its {@code String} representation.
   * No {@code EnhancedPojoFormatter} or {@code EnhancedPropertyFormatter} are specified, so the defaults are used.
   * In particular, instances of {@code DefaultEnhancedPropertyFormatter} will be created for
   * {@code name} and {@code age} (referred to here as {@code nameFormatter} and
   * {@code ageFormatter}, respectively).  Let {@code nameProperty} and
   * {@code ageProperty} refer to the instances of {@link PropertyElement} referring to the
   * properties {@code name} and {@code age} respectively.
   * </p>
   * <p>
   * For a non-null {@code Person} instance, the {@code String} representation will be created by
   * creating an instance of {@code DefaultEnhancedPojoFormatter} for the {@code Person} class (referred to
   * here as {@code personFormatter}), a {@link StringBuilder} (referred to here as builder), and then invoking the
   * following methods in order:
   * <ul>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendToStringPrefix(StringBuilder, Class) personFormatter.appendToStringPrefix(builder, Person.class)}</li>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendPropertyPrefix(StringBuilder, PropertyElement) personFormatter.appendPropertyPrefix(builder, nameProperty)}</li>
   *   <li>{@link DefaultEnhancedPropertyFormatter#appendFormatted(StringBuilder, Object) nameFormatter.appendFormatted(builder, name)}</li>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendPropertySuffix(StringBuilder, PropertyElement) personFormatter.appendPropertySuffix(builder, nameProperty)}</li>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendPropertyPrefix(StringBuilder, PropertyElement) personFormatter.appendPropertyPrefix(builder, ageProperty)}</li>
   *   <li>{@link DefaultEnhancedPropertyFormatter#appendFormatted(StringBuilder, int) ageFormatter.appendFormatted(age)}</li>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendPropertySuffix(StringBuilder, PropertyElement) personFormatter.appendPropertySuffix(builder, ageProperty)}</li>
   *   <li>{@link DefaultEnhancedPojoFormatter#appendToStringSuffix(StringBuilder, Class) personFormatter.appendToStringSuffix(builder, Person.class)}</li>
   *   <li>builder.toString()</li>
   * </ul>
   * </p>
   *
   * @param instance the instance to compute the {@code toString} representation for - must not be {@code null}
   * @return the {@code toString} representation of {@code instance}
   * @throws NullPointerException if {@code instance} is {@code null}
   * @see Object#toString()
   * @see Property#name()
   */
  String doToString(T instance);

  /**
   * Compute whether {@code instance} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@link Object#equals(Object) equals} method. For two instances to be
   * considered equal, the first requirement is that their classes must be compatible for equality,
   * as described in the documentation for {@link #isCompatibleForEquality(Class)}.
   * </p>
   * <p>
   * More precisely, if {@code other} is null, this method returns {@code false}.  Otherwise, if
   * {@link #isCompatibleForEquality(Class) isCompatibleForEquals(other.getClass())} would return
   * false, then this method will return false.  Otherwise, this method will return true provided
   * that each property of {@code instance} which has a {@code PojomaticPolicy} other than
   * {@code TO_STRING} or {@code NONE} is equal to the corresponding property of {@code other} in
   * the following sense:
   * <ul>
   * <li>Both are {@code null}, or</li>
   * <li>Both are reference-equals (==) to each other, or</li>
   * <li>Both are primitive of the same type, and equal to each other, or</li>
   * <li>The property {@code p} in {@code instance} is an object not of array type, and {@code
   * instanceP.equals(otherP)} returns true.
   * <li>The declared type of the property {@code p} is {@link Object}, the property is not annotated with
   * {@link CanBeArray @CanBeArray},
   * and {@code instanceP.equals(otherP)} returns true.
   * <li>The declared type of the property is either an array type, or is of type {@link Object}, and the property is
   * annotated with {@link CanBeArray @CanBeArray}, and:
   * <ul>
   *   <li>If the property is annotated with {@link DeepArray @DeepArray} or has a declared type of a multi-dimensional array, then
   *     {@link Arrays#deepEquals(Object[], Object[]) Arrays.deepEquals(instanceP, otherP)} returns true</li>
   *   <li>If the property is not annotated with {@link DeepArray @DeepArray}, then the appropriate {@code equals} method of
   *     {@link Arrays} returns true</li>
   * </ul></li>
   * </ul>
   * </p>
   * @param instance the instance to test against - must not be {@code null}
   * @param other the instance to test
   * @return {@code true} if {@code instance} should be considered equal to {@code other}, and
   *         {@code false} otherwise.
   * @throws NullPointerException if {@code instance} is {@code null}
   * @see Object#equals(Object)
   */
  boolean doEquals(T instance, Object other);

  /**
   * Compute whether {@code otherClass} is compatible for equality with {@code T}.
   * Classes {@code A} and {@code B} are compatible for equality if
   * they share a common superclass {@code C}, and for every class {@code D} which
   * is a proper subclass of {@code C} and a superclass of {@code A} or {@code B} (including
   * the classes {@code A} and {@code B} themselves), the following hold:
   * <ul>
   *   <li>{@code D} has not added additional properties for inclusion in the {@code equals} calculation, and</li>
   *   <li>{@code D} has not been annotated with {@link OverridesEquals}</li>
   * </ul>
   * If {@code T} is an interface or is annotated with {@link SubclassCannotOverrideEquals},
   * then all subclasses of {@code T} are automatically assumed by {@code T}'s {@code Pojomator}
   * to be compatible for equals with each other and with {@code T}.  Note that in this case.
   * to add an {@link OverridesEquals} annotation or additional
   * properties for inclusion in {@code equals} to a subclass of {@code T} will
   * result in a violation of the contract for {@link Object#equals(Object)}.
   * @param otherClass the class to check for compatibility for equality with {@code T}
   * @return {@code true} if {@code otherClass} is compatible for equality with {@code T}, and
   * {@code false} otherwise.
   */
  boolean isCompatibleForEquality(Class<?> otherClass);

  /**
   * Compute the differences between {@code instance} and {@code other} among the properties
   * examined by {@link #doEquals(Object, Object)}.  Assuming that {@code instance} and {@code other}
   * are both non-null and have types which are compatible for equals, it is guaranteed that invoking
   * {@link Differences#areEqual()} on the returned object will return true iff
   * {@code instance.equals(other)}.
   *
   * @param instance the instance to diff against
   * @param other the instance to diff
   * @return the differences between {@code instance} and {@code other}
   * among the properties examined by {@link #doEquals(Object, Object)}.
   * @throws NullPointerException if {@code instance} or {@code other} is null
   * (this behavior may change in future releases).
   * @throws IllegalArgumentException the type of {@code instance} or of {@code other} is not a
   * class which is compatible for equality with {@code T}
   * (this behavior may change in future releases).
   * @see #doEquals(Object, Object)
   */
  Differences doDiff(T instance, T other);

  /**
   * Return a simple String representation of this Pojomator. This is meant to aid in debugging
   * which properties are being used for which purposes. The contents and format of this
   * representation are subject to change.
   *
   * @return a simple String representation of this Pojomator.
   */
  @Override
  public String toString();
}
