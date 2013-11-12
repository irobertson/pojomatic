package org.pojomatic.internal;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;

class Primitives {
  private final static Map<Class<?>, Class<?>> WRAPPER_CLASSES = new HashMap<>();
  private final static Map<Class<?>, String> LABELS = new HashMap<>();
  private final static Map<Class<?>, Integer> OPCODES = new HashMap<>();

  static {
    register(void.class, Void.class, "V", Opcodes.NULL);
    register(boolean.class, Boolean.class, "Z", Opcodes.INTEGER);
    register(byte.class, Byte.class, "B", Opcodes.INTEGER);
    register(char.class, Character.class, "C", Opcodes.INTEGER);
    register(short.class, Short.class, "S", Opcodes.INTEGER);
    register(int.class, Integer.class, "I", Opcodes.INTEGER);
    register(long.class, Long.class, "J", Opcodes.LONG);
    register(float.class, Float.class, "F", Opcodes.FLOAT);
    register(double.class, Double.class, "D", Opcodes.DOUBLE);
  }

  static Class<?> getWrapperClass(Class<?> primitiveClass) {
    return WRAPPER_CLASSES.get(primitiveClass);
  }

  static String getLabel(Class<?> primitiveClass) {
    return LABELS.get(primitiveClass);
  }

  static Integer getOpcode(Class<?> primitiveClass) {
    Integer retval = OPCODES.get(primitiveClass);
    if (retval == null) {
      System.err.println("null for " + primitiveClass);
      Thread.dumpStack();
    }
    return retval;
  }

  private static <T> void register(Class<T> clazz, Class<? extends T> wrapperClass, String label, Integer opcode) {
    WRAPPER_CLASSES.put(clazz, wrapperClass);
    LABELS.put(clazz, label);
    OPCODES.put(clazz, opcode);
  }
}
