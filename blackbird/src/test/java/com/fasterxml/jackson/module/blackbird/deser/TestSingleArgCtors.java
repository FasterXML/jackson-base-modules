package com.fasterxml.jackson.module.blackbird.deser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdTestBase;

import static org.junit.jupiter.api.Assertions.*;

public class TestSingleArgCtors extends BlackbirdTestBase
{
    static class Node {
        public String name;
        
        public int value;

        public Node() { }

        @JsonCreator
        public Node(String n) {
            name = n;
            value = -1;
        }
    }
    
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = newObjectMapper();
    
    @Test
    public void testSingleStringArgCtor() throws Exception
    {
        Node bean = MAPPER.readValue(quote("Foobar"), Node.class);
        assertNotNull(bean);
        assertEquals(-1, bean.value);
        assertEquals("Foobar", bean.name);
    }

}
