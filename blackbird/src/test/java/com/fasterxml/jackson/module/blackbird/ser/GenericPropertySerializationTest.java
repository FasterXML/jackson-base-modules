package com.fasterxml.jackson.module.blackbird.ser;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdTestBase;

import static org.junit.jupiter.api.Assertions.*;

public class GenericPropertySerializationTest extends BlackbirdTestBase
{
    public static abstract class AbstractMyClass<ID> {

        private ID id;

        AbstractMyClass(ID id) {
            setId(id);
        }

        public ID getId() {
            return id;
        }

        public void setId(ID id) {
            this.id = id;
        }
    }

    public static class MyClass extends AbstractMyClass<String> {
        public MyClass(String id) {
            super(id);
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    final private ObjectMapper MAPPER = newObjectMapper();

    @Test
    public void testGenericIssue4() throws Exception
    {
        MyClass input = new MyClass("foo");
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"id\":\"foo\"}", json);
    }
}
