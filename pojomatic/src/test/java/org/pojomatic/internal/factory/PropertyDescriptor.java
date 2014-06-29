package org.pojomatic.internal.factory;

import java.lang.annotation.Annotation;

public class PropertyDescriptor {
  final String name;
  final Class<?> type;
  final Access access;
  final boolean isMethod;
  final Class<? extends Annotation>[] annotations;

  @SuppressWarnings("unchecked")
  public PropertyDescriptor(Class<?> type) {
    this(type, new Class[0]);
  }

  public PropertyDescriptor(Class<?> type, Class<? extends Annotation>[] annotations) {
    this(type, annotations, "x");
  }

  public PropertyDescriptor(Class<?> type, Class<? extends Annotation>[] annotations, String name) {
    this(type, annotations, name, Access.PRIVATE);
  }

  public PropertyDescriptor(Class<?> type, Class<? extends Annotation>[] annotations, String name, Access access) {
    this(type, annotations, name, access, false);
  }

  public PropertyDescriptor(Class<?> type, Class<? extends Annotation>[] annotations, String name, Access access, boolean isMethod) {
    this.type = type;
    this.name = name;
    this.access = access;
    this.isMethod = isMethod;
    this.annotations = annotations;
  }
}
