package org.pojomatic.internal;

import java.util.HashMap;
import java.util.Map;

import org.kohsuke.asm5.Opcodes;

class Primitives {
  private final static Map<Class<?>, Class<?>> WRAPPER_CLASSES = new HashMap<>();
  private final static Map<Class<?>, Integer> OPCODES = new HashMap<>();

  static {
    register(void.class, Void.class, Opcodes.NULL);
    register(boolean.class, Boolean.class, Opcodes.INTEGER);
    register(byte.class, Byte.class, Opcodes.INTEGER);
    register(char.class, Character.class, Opcodes.INTEGER);
    register(short.class, Short.class, Opcodes.INTEGER);
    register(int.class, Integer.class, Opcodes.INTEGER);
    register(long.class, Long.class, Opcodes.LONG);
    register(float.class, Float.class, Opcodes.FLOAT);
    register(double.class, Double.class, Opcodes.DOUBLE);
  }

  static Class<?> getWrapperClass(Class<?> primitiveClass) {
    return WRAPPER_CLASSES.get(primitiveClass);
  }

  static Integer getOpcode(Class<?> primitiveClass) {
    Integer retval = OPCODES.get(primitiveClass);
    if (retval == null) {
      System.err.println("null for " + primitiveClass);
      Thread.dumpStack();
    }
    return retval;
  }

  private static <T> void register(Class<T> clazz, Class<? extends T> wrapperClass, Integer opcode) {
    WRAPPER_CLASSES.put(clazz, wrapperClass);
    OPCODES.put(clazz, opcode);
  }
}
