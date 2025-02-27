package com.fasterxml.jackson.module.mrbean;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

public class TestMapStringObjectDeserialization
    extends BaseTest
{

    /**
     * Test simple Map deserialization works.
     */
    @Test
    public void testMapWithMrbean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new MrBeanModule());

        runTest(mapper);
    }

    /**
     * Test simple Map deserialization works.
     */
    @Test
    public void testMapWithoutMrbean() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        runTest(mapper);
    }

    void runTest(ObjectMapper mapper) throws IOException, JsonParseException, JsonMappingException
    {
        Map<String, Object> map = mapper.readValue("{\"test\":3 }", new TypeReference<Map<String, Object>>() {});
        assertEquals(Collections.singletonMap("test", 3), map);
    }
}
