/**
 * Helper code for compiling classes. This is necessary because when we build with maven, we always target Java 7,
 * even if running a later version. This allows us to generate bytecode using the running version of Java.
 */
package org.pojomatic.internal.compile;
