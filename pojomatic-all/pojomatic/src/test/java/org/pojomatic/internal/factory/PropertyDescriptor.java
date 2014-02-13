package org.pojomatic.internal.factory;

public class PropertyDescriptor {
  final String name;
  final Class<?> type;
  final Access access;
  final boolean isMethod;

  public PropertyDescriptor(Class<?> type) {
    this(type, "x");
  }

  public PropertyDescriptor(Class<?> type, String name) {
    this(type, name, Access.PRIVATE);
  }

  public PropertyDescriptor(Class<?> type, String name, Access access) {
    this(type, name, access, false);
  }

  public PropertyDescriptor(Class<?> type, String name, Access access, boolean isMethod) {
    this.type = type;
    this.name = name;
    this.access = access;
    this.isMethod = isMethod;
  }
}
