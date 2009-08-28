package org.pojomatic.test;

import static org.junit.Assert.*;

import org.junit.Test;

public class AssertUtilsTest extends AssertTest {

  @Override
  protected void performAssertEquals(Object first, Object second) {
    AssertUtils.assertEquals(null, first, second);
  }

  @Test
  public void testEquals() {
    assertTrue(AssertUtils.equal(new Container(""), new Container("")));
  }

  @Test
  public void testEqualsReflexive() {
    Container instance = new Container("");
    assertTrue(AssertUtils.equal(instance, instance));
  }

  @Test
  public void testEqualsBothNull() {
    assertTrue(AssertUtils.equal(null, null));
  }

  @Test
  public void testEqualsNullFirst() {
    assertFalse(AssertUtils.equal(null, new Container(null)));
  }

  @Test
  public void testEqualsNullSecond() {
    assertFalse(AssertUtils.equal(new Container(null), null));
  }

}
