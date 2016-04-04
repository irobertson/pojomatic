package org.pojomatic.internal.factory;

import java.util.Arrays;
import java.util.List;

import org.pojomatic.annotations.AutoDetectPolicy;

public class PojoDescriptor {
  public final String className;
  public final String packageName;
  public final Access access;
  public final PojoDescriptor parent;
  public final List<PropertyDescriptor> properties;
  public AutoDetectPolicy autoDetectPolicy;

  public PojoDescriptor(PropertyDescriptor... properties) {
    this("Pojo", properties);
  }

  public PojoDescriptor(String className, PropertyDescriptor... properties) {
    this("pojos", className, properties);
  }

  public PojoDescriptor(String packageName, String className, PropertyDescriptor... properties) {
    this(packageName, className, Access.PUBLIC, properties);
  }

  public PojoDescriptor(String packageName, String className, Access access, PropertyDescriptor... properties) {
    this(packageName, className, access, null, properties);
  }

  public PojoDescriptor(String packageName, String className, Access access, PojoDescriptor parent, PropertyDescriptor... properties) {
    this.packageName = packageName;
    this.className = className;
    this.access = access;
    this.parent = parent;
    this.properties = Arrays.asList(properties);
  }

  public PojoDescriptor withAutoDetectPolicy(AutoDetectPolicy autoDetectPolicy) {
    this.autoDetectPolicy = autoDetectPolicy;
    return this;
  }

  public String qualifiedName() {
    return packageName + "." + className;
  }

  public String internalName() {
    return packageName + "/" + className;
  }

  public String parentInternalName() {
    return parent == null ? "java/lang/Object" : parent.internalName();
  }
}
