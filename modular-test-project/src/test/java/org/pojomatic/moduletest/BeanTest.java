package org.pojomatic.moduletest;

import static org.junit.Assert.*;

import org.junit.Test;

public class BeanTest {

    @Test
    public void testToString() throws Exception {
        assertEquals("Bean{s: {hello, world}}", new Bean("hello, world").toString());
    }

}
