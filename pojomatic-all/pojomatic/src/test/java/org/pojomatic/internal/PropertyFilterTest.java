package org.pojomatic.internal;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.EnumSet;

import org.pojomatic.annotations.DefaultPojomaticPolicy;
import org.pojomatic.annotations.PojomaticPolicy;

public class PropertyFilterTest {
  private static final Iterable<PojomaticPolicy> ALL_BUT_DEFAULT =
    EnumSet.complementOf(EnumSet.of(PojomaticPolicy.DEFAULT));

  @Test public void testGetRolesWithNoClassPolicy() {
    for (PojomaticPolicy policy: ALL_BUT_DEFAULT) {
      assertEquals(PropertyFilter.getRoles(policy, null), policy.getRoles());
    }
  }

  @Test public void testGetRolesWithPropertyPolicyAndClassPolicy() {
    for (PojomaticPolicy policy: ALL_BUT_DEFAULT) {
      for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      assertEquals(PropertyFilter.getRoles(policy, defaultPolicy), policy.getRoles());
      }
    }
  }

  @Test public void testGetRolesWithDefaultPropertyPolicyAndNoClassPolicy() {
    assertEquals(PropertyFilter.getRoles(PojomaticPolicy.DEFAULT, null), PojomaticPolicy.ALL.getRoles());
  }

  @Test public void testGetRolesWithDefaultPropertyPolicyAndClassPolicy() {
    for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      assertEquals(PropertyFilter.getRoles(PojomaticPolicy.DEFAULT, defaultPolicy), defaultPolicy.getRoles());
    }
  }

  @Test public void testGetRolesWithOnlyClassPolicy() {
    for (DefaultPojomaticPolicy defaultPolicy: DefaultPojomaticPolicy.values()) {
      assertEquals(PropertyFilter.getRoles(null, defaultPolicy), defaultPolicy.getRoles());
    }
  }

  @Test
  public void testGetRolesWithNoPolicy() {
    assertEquals(PropertyFilter.getRoles(null, null), Collections.EMPTY_SET);
  }

}
