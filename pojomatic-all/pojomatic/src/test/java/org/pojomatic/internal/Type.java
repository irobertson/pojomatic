package org.pojomatic.internal;

import java.util.List;

public interface Type {

  public abstract Class<?> getClazz();

  public abstract List<Object> getSampleValues();

}