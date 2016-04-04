package org.pojomatic.internal.factory;

import java.lang.annotation.Annotation;

import org.kohsuke.asm5.Opcodes;

public class PropertyDescriptor {
  final Class<?> type;
  final Class<? extends Annotation>[] annotations;

  String name = "x";
  Access access = Access.PRIVATE;
  boolean isMethod;
  boolean isSynthetic;

  @SuppressWarnings("unchecked")
  public PropertyDescriptor(Class<?> type) {
    this(type, new Class[0]);
  }

  public PropertyDescriptor(Class<?> type, Class<? extends Annotation>[] annotations) {
    this.type = type;
    this.annotations = annotations;
  }

  public PropertyDescriptor withName(String name) {
    this.name = name;
    return this;
  }

  public PropertyDescriptor withAccess(Access access) {
    this.access = access;
    return this;
  }

  public PropertyDescriptor asMethod() {
    isMethod = true;
    return this;
  }

  public PropertyDescriptor asSynthetic() {
    isSynthetic = true;
    return this;
  }

  public int getFlags() {
    return access.getCode() | (isSynthetic ? Opcodes.ACC_SYNTHETIC : 0);
  }
}
