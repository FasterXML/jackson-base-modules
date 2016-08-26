/*
* Copyright (c) FasterXML, LLC.
* Licensed under the Apache (Software) License, version 2.0.
*/

package com.fasterxml.jackson.module.afterburner.ser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerTestBase;

public class GenericPropertySerializationTest extends AfterburnerTestBase
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

    final private ObjectMapper MAPPER = mapperWithModule();

    public void testGenericIssue4() throws Exception
    {
        MyClass input = new MyClass("foo");
        String json = MAPPER.writeValueAsString(input);
        assertEquals("{\"id\":\"foo\"}", json);
    }
}
