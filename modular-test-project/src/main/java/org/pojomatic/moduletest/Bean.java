package org.pojomatic.moduletest;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class Bean {
    private final String s;

    public Bean(String s) {
        this.s = s;
    }

    public String getS() { return s; }

    public static void main(String[] args) {
        System.out.println(new Bean("Hello, world"));
    }

    @Override public String toString() { return Pojomatic.toString(this); }
    @Override public boolean equals(Object other) { return Pojomatic.equals(this, other); }
    @Override public int hashCode() { return Pojomatic.hashCode(this); }

}
