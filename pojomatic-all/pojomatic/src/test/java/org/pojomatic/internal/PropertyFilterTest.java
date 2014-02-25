package org.pojomatic.internal;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import java.util.Collections;
import java.util.EnumSet;

import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;

public class PropertyFilterTest {
  private static final Iterable<PojomaticPolicy> ALL_BUT_DEFAULT =
    EnumSet.complementOf(EnumSet.of(PojomaticPolicy.DEFAULT));

  @Test public void testGetRolesWithNoClassPolicy() {
    for (PojomaticPolicy policy: ALL_BUT_DEFAULT) {
      AssertJUnit.assertEquals(policy.getRoles(), PropertyFilter.getRoles(policy, null));
    }
  }

  @Test public void testGetRolesWithPropertyPolicyAndClassPolicy() {
    for (PojomaticPolicy policy: ALL_BUT_DEFAULT) {
      for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      AssertJUnit.assertEquals(policy.getRoles(), PropertyFilter.getRoles(policy, defaultPolicy));
      }
    }
  }

  @Test public void testGetRolesWithDefaultPropertyPolicyAndNoClassPolicy() {
    AssertJUnit.assertEquals(
      PojomaticPolicy.ALL.getRoles(), PropertyFilter.getRoles(PojomaticPolicy.DEFAULT, null));
  }

  @Test public void testGetRolesWithDefaultPropertyPolicyAndClassPolicy() {
    for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      AssertJUnit.assertEquals(
        defaultPolicy.getRoles(), PropertyFilter.getRoles(PojomaticPolicy.DEFAULT, defaultPolicy));
    }
  }

  @Test public void testGetRolesWithOnlyClassPolicy() {
    for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      AssertJUnit.assertEquals(defaultPolicy.getRoles(), PropertyFilter.getRoles(null, defaultPolicy));
    }
  }

  @Test
  public void testGetRolesWithNoPolicy() {
    AssertJUnit.assertEquals(Collections.EMPTY_SET, PropertyFilter.getRoles(null, null));
  }

}
