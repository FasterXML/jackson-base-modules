package com.fasterxml.jackson.module.blackbird.deser.merge;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonMerge;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.module.blackbird.BlackbirdTestBase;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MapMergeTest extends BlackbirdTestBase
{
    static class MergedMap
    {
        @JsonMerge
        public Map<String,String> values = new LinkedHashMap<>();
        {
            values.put("a", "x");
        }
    }

    /*
    /********************************************************
    /* Test methods, Map merging
    /********************************************************
     */

    private final ObjectMapper MAPPER = mapperBuilder()
            // 26-Oct-2016, tatu: Make sure we'll report merge problems by default
            .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE)
            .build()
    ;

    @Test
    public void testMapMerging() throws Exception
    {
        MergedMap v = MAPPER.readValue(aposToQuotes("{'values':{'c':'y'}}"), MergedMap.class);
        assertEquals(2, v.values.size());
        assertEquals("y", v.values.get("c"));
        assertEquals("x", v.values.get("a"));
    }

}
