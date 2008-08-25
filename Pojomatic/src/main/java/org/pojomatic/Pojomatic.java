package org.pojomatic;

import org.pojomatic.internal.PojomatorImpl;
import org.pojomatic.internal.SelfPopulatingMap;

/**
 * Static convenience methods for working with {@code Pojomator}s.  This class is carefull to create
 * only a single {@code Pojomator} per pojo class.  The overhead for looking up the {@code Pojomator}
 * by pojo class is light, so a a typical use in a pojo class would be
 * <p>
 * <code>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>int&nbsp;</b></font><font color="#000000">hashCode() {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomator.hashCode(</font><font color="#7f0055"><b>this</b></font><font color="#000000">);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#7f0055"><b>boolean&nbsp;</b></font><font color="#000000">equals(Object other) {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomator.equals(</font><font color="#7f0055"><b>this</b></font><font color="#000000">, other);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
 * &nbsp;&nbsp;<font color="#646464">@Override</font>&nbsp;<font color="#7f0055"><b>public&nbsp;</b></font><font color="#000000">String toString() {</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<font color="#7f0055"><b>return&nbsp;</b></font><font color="#000000">Pojomator.toString(</font><font color="#7f0055"><b>this</b></font><font color="#000000">);</font><br />
 * &nbsp;&nbsp;<font color="#000000">}</font><br>
 * <br/>
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
   * Compute the {@code toString} representation for a pojo.
   * @param <T> the type of the pojo
   * @param pojo the pojo - must not be null
   * @return the {@code toString} representation of {@code pojo}.
   * @see Pojomator#doToString(Object)
   */
  public static <T> String toString(T pojo) {
    return pojomator(getClass(pojo)).doToString(pojo);
  }

  /**
   * Compute the {@code hashCode} for a pojo.
   * @param <T> the type of the pojo
   * @param pojo the pojo - must not be null
   * @return the {@code hashCode} for {@code pojo}.
   * @see Pojomator#doHashCode(Object)
   */
  public static <T> int hashCode(T pojo) {
    return pojomator(getClass(pojo)).doHashCode(pojo);
  }

  /**
   * Compute whether {@code pojo} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@code equals} method.
   * @param <T> the type of the pojo
   * @param pojo the pojo - must not be null
   * @param other the object to compare to for equality
   * @return whether {@code pojo} and {@code other} are equal to each other in the sense of
   * {@code Object}'s {@code equals} method.
   * @see Pojomator#doEquals(Object, Object)
   */
  public static <T> boolean equals(T pojo, Object other) {
    return pojomator(getClass(pojo)).doEquals(pojo, other);
  }

  /**
   * Get the {@code Pojomator} for {@code pojoClass}.  The same instance will be returned every time
   * for a given value of {@code pojoClass}.
   * @param <T> the type represented by {@code pojoClass}
   * @param pojoClass the class to create a {@code Pojomator} for.
   * @return a {@code Pojomator<T>}
   */
  @SuppressWarnings("unchecked") // compiler does not know that the type parameter to Pojomator is T
  public static <T> Pojomator<T> pojomator(Class<T> pojoClass) {
    return (Pojomator<T>) POJOMATORS.get(pojoClass);
  }

  @SuppressWarnings("unchecked") // Since Object.getClass returns Class<?>
  private static <T> Class<T> getClass(T pojo) {
    return (Class<T>) pojo.getClass();
  }
}
