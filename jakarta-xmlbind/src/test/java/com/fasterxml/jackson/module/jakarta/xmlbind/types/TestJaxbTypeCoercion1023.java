package com.fasterxml.jackson.module.jakarta.xmlbind.types;

import jakarta.xml.bind.annotation.XmlElement;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.ModuleTestBase;

import static org.junit.jupiter.api.Assertions.*;

// Failing temporarily due to [databind#1023]
public class TestJaxbTypeCoercion1023 extends ModuleTestBase
{
    /*
    /**********************************************************
    /* Helper beans
    /**********************************************************
     */

    /**
     * Unit test related to [JACKSON-416]
     */
    static class Jackson416Bean
    {
        @XmlElement(type=Jackson416Base.class)
        public Jackson416Base value = new Jackson416Sub();
    }

    static class Jackson416Base
    {
        public String foo = "foo";
    }

    static class Jackson416Sub extends Jackson416Base
    {
        public String bar = "bar";
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    @Test
    public void testIssue416() throws Exception
    {
        ObjectMapper mapper = getJaxbAndJacksonMapper();
        Jackson416Bean bean = new Jackson416Bean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"value\":{\"foo\":\"foo\"}}", json);
    }
}
