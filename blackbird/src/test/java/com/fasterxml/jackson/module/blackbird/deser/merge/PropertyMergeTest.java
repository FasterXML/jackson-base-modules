package com.fasterxml.jackson.module.blackbird.deser.merge;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.module.blackbird.BlackbirdTestBase;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to make sure that the new "merging" property of
 * <code>JsonSetter</code> annotation works as expected.
 */
@SuppressWarnings("serial")
public class PropertyMergeTest extends BlackbirdTestBase
{
    static class Config {
        @JsonMerge
        public AB loc = new AB(1, 2);

        protected Config() { }
        public Config(int a, int b) {
            loc = new AB(a, b);
        }
    }

    static class NonMergeConfig {
        public AB loc = new AB(1, 2);
    }

    // another variant where all we got is a getter
    static class NoSetterConfig {
        AB _value = new AB(1, 2);
 
        @JsonMerge
        public AB getValue() { return _value; }
    }

    static class AB {
        public int a;
        public int b;

        protected AB() { }
        public AB(int a0, int b0) {
            a = a0;
            b = b0;
        }
    }

    @JsonPropertyOrder(alphabetic=true)
    @JsonFormat(shape=Shape.ARRAY)
    static class ABAsArray {
        public int a;
        public int b;
    }

    // Custom type that would be deserializable by default
    static class StringReference extends AtomicReference<String> {
        public StringReference(String str) {
            set(str);
        }
    }

    static class MergedReference
    {
        @JsonMerge
        public StringReference value = new StringReference("default");
    }

    static class MergedX<T>
    {
        @JsonMerge
        public T value;

        public MergedX(T v) { value = v; }
        protected MergedX() { }
    }
    
    // // // Classes with invalid merge definition(s)

    static class CantMergeInts {
        @JsonMerge
        public int value;
    }

    /*
    /********************************************************
    /* Test methods, POJO merging
    /********************************************************
     */

    private final ObjectMapper MAPPER = mapperBuilder()
            // 26-Oct-2016, tatu: Make sure we'll report merge problems by default
            .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE)
            .build()
    ;

    @Test
    public void testBeanMergingViaProp() throws Exception
    {
        Config config = MAPPER.readValue(aposToQuotes("{'loc':{'b':3}}"), Config.class);
        assertEquals(1, config.loc.a);
        assertEquals(3, config.loc.b);

        config = MAPPER.readerForUpdating(new Config(5, 7))
                .readValue(aposToQuotes("{'loc':{'b':2}}"));
        assertEquals(5, config.loc.a);
        assertEquals(2, config.loc.b);
    }

    @Test
    public void testBeanMergingViaType() throws Exception
    {
        // by default, no merging
        NonMergeConfig config = MAPPER.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(0, config.loc.b); // not passed, nor merge from original

        // but with type-overrides
        ObjectMapper mapper = newObjectMapper();
        mapper.configOverride(AB.class).setMergeable(true);
        config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); // original, merged
    }

    @Test
    public void testBeanMergingViaGlobal() throws Exception
    {
        // but with type-overrides
        ObjectMapper mapper = newObjectMapper()
                .setDefaultMergeable(true);
        NonMergeConfig config = mapper.readValue(aposToQuotes("{'loc':{'a':3}}"), NonMergeConfig.class);
        assertEquals(3, config.loc.a);
        assertEquals(2, config.loc.b); // original, merged

        // also, test with bigger POJO type; just as smoke test
        FiveMinuteUser user0 = new FiveMinuteUser("Bob", "Bush", true, FiveMinuteUser.Gender.MALE,
                new byte[] { 1, 2, 3, 4, 5 });
        FiveMinuteUser user = mapper.readerFor(FiveMinuteUser.class)
                .withValueToUpdate(user0)
                .readValue(aposToQuotes("{'name':{'last':'Brown'}}"));
        assertEquals("Bob", user.getName().getFirst());
        assertEquals("Brown", user.getName().getLast());
    }

    // should even work with no setter
    @Test
    public void testBeanMergingWithoutSetter() throws Exception
    {
        NoSetterConfig config = MAPPER.readValue(aposToQuotes("{'value':{'b':99}}"),
                NoSetterConfig.class);
        assertEquals(99, config._value.b);
        assertEquals(1, config._value.a);
    }

    /*
    /********************************************************
    /* Test methods, as array
    /********************************************************
     */

    @Test
    public void testBeanAsArrayMerging() throws Exception
    {
        ABAsArray input = new ABAsArray();
        input.a = 4;
        input.b = 6;

        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[1, 3]"));
        assertEquals(1, input.a);
        assertEquals(3, input.b);

        // then with one too few
        assertSame(input, MAPPER.readerForUpdating(input)
                .readValue("[9]"));
        assertEquals(9, input.a);
        assertEquals(3, input.b);

        // and finally with extra, failing
        try {
            MAPPER.readerForUpdating(input)
                .readValue("[9, 8, 14]");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "expected at most 2 properties");
        }

        try {
            MAPPER.readerForUpdating(input)
                .readValue("\"blob\"");
            fail("Should not pass");
        } catch (MismatchedInputException e) {
            verifyException(e, "cannot deserialize");
            verifyException(e, "from non-Array representation");
        }
    }

    /*
    /********************************************************
    /* Test methods, reference types
    /********************************************************
     */

    @Test
    public void testReferenceMerging() throws Exception
    {
        MergedReference result = MAPPER.readValue(aposToQuotes("{'value':'override'}"),
                MergedReference.class);
        assertEquals("override", result.value.get());
    }

    /*
    /********************************************************
    /* Test methods, failure checking
    /********************************************************
     */

    @Test
    public void testInvalidPropertyMerge() throws Exception
    {
        ObjectMapper mapper = mapperBuilder()
                .disable(MapperFeature.IGNORE_MERGE_FOR_UNMERGEABLE)
                .build();
        
        try {
            mapper.readValue("{\"value\":3}", CantMergeInts.class);
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "cannot be merged");
        }
    }
}
