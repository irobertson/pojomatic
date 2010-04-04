package org.pojomatic;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.annotations.OverridesEquals;
import org.pojomatic.annotations.PojomaticPolicy;
import org.pojomatic.annotations.Property;
import org.pojomatic.annotations.SubclassCannotOverrideEquals;
import org.pojomatic.diff.Differences;
import org.pojomatic.formatter.DefaultPojoFormatter;
import org.pojomatic.formatter.DefaultPropertyFormatter;
import org.pojomatic.formatter.PojoFormatter;
import org.pojomatic.formatter.PropertyFormatter;

/**
 * A provider of the three standard {@code Object} methods,
 * {@link Object#equals(Object)}, {@link Object#hashCode()} and {@link Object#toString()}, as
 * well as a usefull method to aid in debugging, {@link #doDiff(Object, Object)}}.
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
   * {@link PojoFormatter} used for the POJO, and the {@link PropertyFormatter} of each property.
   * <p>
   * For example, suppose a class {@code Person} has properties {@code firstName} and
   * {@code lastName} which are included in its {@code String} representation.
   * No {@code PojoFormatter} or {@code PropertyFormatter} are specified, so the defaults are used.
   * In particular, instances of {@code DefaultPropertyFormatter} will be created for
   * {@code firstName} and {@code lastName} (referred to here as {@code firstNameFormatter} and
   * {@code lastNameFormatter}, respectively).  Let {@code firstNameProperty} and
   * {@code lastNameProperty} refer to the instances of {@link PropertyElement} referring to the
   * properties {@code firstName} and {@code lastName} respectively.
   * </p>
   * <p>
   * For a non-null {@code Person} instance, the {@code String} representation will be created by
   * creating an instance of {@code DefaultPojoFormatter} for the {@code Person} class (referred to
   * here as {@code personFormatter}), and then concatenating the results of following:
   * <ol>
   *   <li>{@link DefaultPojoFormatter#getToStringPrefix(Class) personFormatter.getToStringPrefix(Person.class)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertyPrefix(PropertyElement) personFormatter.getPropertyPrefix(firstNameProperty)}</li>
   *   <li>{@link DefaultPropertyFormatter#format(Object) firstNameFormatter.format(firstName)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertySuffix(PropertyElement) personFormatter.getPropertySuffix(firstNameProperty)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertyPrefix(PropertyElement) personFormatter.getPropertyPrefix(lastNameProperty)}</li>
   *   <li>{@link DefaultPropertyFormatter#format(Object) lastNameFormatter.format(lastName)}</li>
   *   <li>{@link DefaultPojoFormatter#getPropertySuffix(PropertyElement) personFormatter.getPropertySuffix(lasttNameProperty)}</li>
   *   <li>{@link DefaultPojoFormatter#getToStringSuffix(Class) personFormatter.getToStringSuffix(Person.class)}</li>
   * </ol>
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
   * <li>Both are of array type, with matching primitive component types, and the corresponding
   * {@code} equals method of {@link Arrays} returns true, or</li>
   * <li>Both are of array type with non-primitive component types, and
   * {@link Arrays#deepEquals(Object[], Object[])} returns true, or</li>
   * <li>The property {@code p} in {@code instance} is an object not of array type, and {@code
   * instanceP.equals(otherP)} returns true.
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
   * to be comapatible for equals with each other and with {@code T}.  Note that in this case.
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
   * (this behavoir may change in future releases).
   * @throws IllegalArgumentException the type of {@code instance} or of {@code other} is not a
   * class which is compatible for equality with {@code T}
   * (this behavoir may change in future releases).
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
