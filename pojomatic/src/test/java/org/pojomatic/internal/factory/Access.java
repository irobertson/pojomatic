package org.pojomatic.internal.factory;

import static org.kohsuke.asm4.Opcodes.*;

public enum Access {
  PRIVATE(ACC_PRIVATE),
  PACKAGE(0),
  PROTECTED(ACC_PROTECTED),
  PUBLIC(ACC_PUBLIC);

  private final int code;

  private Access(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
