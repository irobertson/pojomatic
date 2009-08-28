package org.pojomatic;

import org.pojomatic.diff.DifferenceFromNull;
import org.pojomatic.diff.Differences;
import org.pojomatic.diff.NoDifferences;
import org.pojomatic.internal.PojomatorImpl;
import org.pojomatic.internal.SelfPopulatingMap;

/**
 * Static methods for implementing the {@link java.lang.Object#equals(Object)},
 * {@link java.lang.Object#hashCode()} and {@link java.lang.Object#toString()} methods on a
 * annotated POJO.  The actual work for a given class is done by a {@link Pojomator} created for
 * that class.  This class is careful to create only a single {@code Pojomator} per POJO class.
 * The overhead for looking up the {@code Pojomator} by POJO class is light, so a typical use in a
 * POJO class would be
 * <p style="background-color:#EEEEFF; margin: 1em">
 * <code>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>int&nbsp;</b></font><font color="#000000">hashCode() {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomatic.hashCode(</font><font color="#7f0055"><b>this</b></font><font color="#000000">);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>boolean&nbsp;</b></font><font color="#000000">equals(Object other) {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomatic.equals(</font><font color="#7f0055"><b>this</b></font><font color="#000000">, other);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#000000">String toString() {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomatic.toString(</font><font color="#7f0055"><b>this</b></font><font color="#000000">);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
 * </code>
 * </p>
 * Under the covers, these methods are referencing a {@link org.pojomatic.Pojomator Pojomator} instance
 * which is created lazily and cached on a per-class basis.  The performance penalty for this is
 * negligible, but if profiling suggests that it is a bottleneck, one can do this by hand:
 * <p style="background-color:#EEEEFF; margin: 1em">
 * <code>
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#7f0055"><b>private&nbsp;final&nbsp;static&nbsp;</b></font><font color="#000000">Pojomator&lt;Manual&gt;&nbsp;POJOMATOR&nbsp;=&nbsp;Pojomatic.pojomator</font><font color="#000000">(</font><font color="#000000">Manual.</font><font color="#7f0055"><b>class</b></font><font color="#000000">)</font><font color="#000000">;</font><br />
 * <font color="#ffffff"></font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#646464">@Override&nbsp;</font><font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>boolean&nbsp;</b></font><font color="#000000">equals</font><font color="#000000">(</font><font color="#000000">Object&nbsp;other</font><font color="#000000">)&nbsp;{</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">POJOMATOR.doEquals</font><font color="#000000">(</font><font color="#000000">this,&nbsp;other</font><font color="#000000">)</font><font color="#000000">;</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">}</font><br />
 * <font color="#ffffff"></font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#646464">@Override&nbsp;</font><font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>int&nbsp;</b></font><font color="#000000">hashCode</font><font color="#000000">()&nbsp;{</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">POJOMATOR.doHashCode</font><font color="#000000">(</font><font color="#7f0055"><b>this</b></font><font color="#000000">)</font><font color="#000000">;</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">}</font><br />
 * <font color="#ffffff"></font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#646464">@Override&nbsp;</font><font color="#7f0055"><b>public&nbsp;</b></font><font color="#000000">String&nbsp;toString</font><font color="#000000">()&nbsp;{</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;&nbsp;&nbsp;</font><font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">POJOMATOR.doToString</font><font color="#000000">(</font><font color="#7f0055"><b>this</b></font><font color="#000000">)</font><font color="#000000">;</font><br />
 * <font color="#ffffff">&nbsp;&nbsp;</font><font color="#000000">}</font>
 * </code>
 * </p>
 *
 * @see Pojomator
 */
public class Pojomatic {

  private static SelfPopulatingMap<Class<?>, Pojomator<?>> POJOMATORS =
    new SelfPopulatingMap<Class<?>, Pojomator<?>>() {
      @Override
      @SuppressWarnings("unchecked")
      // compiler does not know that the type parameter to Pojomator is the same as the type
      // parameter to Class
      protected Pojomator<?> create(Class<?> key) {
        return new PojomatorImpl(key);
      }
  };

  private Pojomatic() {}

  /**
   * Compute the {@code toString} representation for a POJO.
   * @param <T> the type of the POJO
   * @param pojo the POJO - must not be null
   * @return the {@code toString} representation of {@code pojo}.
   * @throws IllegalArgumentException if {@code pojo}'s class has no properties annotated for use
   * with Pojomatic
   * @see Pojomator#doToString(Object)
   */
  public static <T> String toString(T pojo) throws IllegalArgumentException {
    return pojomator(getClass(pojo)).doToString(pojo);
  }

  /**
   * Compute the {@code hashCode} for a POJO.
   * @param <T> the type of the POJO
   * @param pojo the POJO - must not be null
   * @return the {@code hashCode} for {@code pojo}.
   * @throws IllegalArgumentException if {@code pojo}'s class has no properties annotated for use
   * with Pojomatic
   * @see Pojomator#doHashCode(Object)
   */
  public static <T> int hashCode(T pojo) throws IllegalArgumentException {
    return pojomator(getClass(pojo)).doHashCode(pojo);
  }

  /**
   * Compute whether {@code pojo} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@code equals} method.
   * @param <T> the type of the POJO
   * @param pojo the POJO - must not be null
   * @param other the object to compare to for equality
   * @return whether {@code pojo} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@code equals} method.
   * @throws IllegalArgumentException if {@code pojo}'s class has no properties annotated for use
   * with Pojomatic
   * @see Pojomator#doEquals(Object, Object)
   */
  public static <T> boolean equals(T pojo, Object other) throws IllegalArgumentException {
    return pojomator(getClass(pojo)).doEquals(pojo, other);
  }


  /**
   * Compute the differences between {@code pojo} and {@code other} among the properties
   * examined by {@link #equals(Object, Object)} for type {@code T}.
   *
   * @param <T> the static type of the first object to compare
   * @param <S> the static type of the first object to compare
   * @param pojo the instance to diff against
   * @param other the instance to diff
   * @return the list of differences (possibly empty) between {@code instance} and {@code other}
   * among the properties examined by {@link #equals(Object, Object)} for type {@code T}.
   * @throws IllegalArgumentException if {@code pojo}'s class has no properties annotated for use
   * with Pojomatic
   */
  public static <T, S extends T> Differences diff(T pojo, S other)
  throws NullPointerException, IllegalArgumentException {
    if (pojo == null) {
      if (other != null) {
        return new DifferenceFromNull(other);
      }
      else { //both null
        return NoDifferences.getInstance();
      }
    }

    return pojomator(getClass(pojo)).doDiff(pojo, other);
  }

  /**
   * Get the {@code Pojomator} for {@code pojoClass}.  The same instance will be returned every time
   * for a given value of {@code pojoClass}.
   * @param <T> the type represented by {@code pojoClass}
   * @param pojoClass the class to create a {@code Pojomator} for.
   * @return a {@code Pojomator<T>}
   * @throws IllegalArgumentException if {@code pojoClass} has no properties annotated for use
   * with Pojomatic
   */
  @SuppressWarnings("unchecked") // compiler does not know that the type parameter to Pojomator is T
  public static <T> Pojomator<T> pojomator(Class<T> pojoClass) throws IllegalArgumentException {
    return (Pojomator<T>) POJOMATORS.get(pojoClass);
  }

  @SuppressWarnings("unchecked") // Since Object.getClass returns Class<?>
  private static <T> Class<T> getClass(T pojo) {
    return (Class<T>) pojo.getClass();
  }
}
