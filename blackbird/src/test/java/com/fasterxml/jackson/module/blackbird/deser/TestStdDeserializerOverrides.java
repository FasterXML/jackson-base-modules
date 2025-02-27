package com.fasterxml.jackson.module.blackbird.deser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdTestBase;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("serial")
public class TestStdDeserializerOverrides extends BlackbirdTestBase
{
    static class ClassWithPropOverrides
    {
        public String a;
        
        @JsonDeserialize(using=MyStringDeserializer.class)
        public String b;
    }

    static class MyStringDeserializer extends StdDeserializer<String>
    {
        public MyStringDeserializer() { super(String.class); }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            return "Foo:"+p.getText();
        }
    }

    // for [module-afterburner#59]
    static class Issue59Bean {
        public String field;
    }

    static class DeAmpDeserializer extends StdDeserializer<String>
    {
        public DeAmpDeserializer() { super(String.class); }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return p.getText().replaceAll("&amp;", "&");
        }
    }
    
    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    @Test
    public void testFiveMinuteDoc() throws Exception
    {
        ObjectMapper plainMapper = new ObjectMapper();
        ObjectMapper abMapper = newObjectMapper();
        final String JSON = "{\"a\":\"a\",\"b\":\"b\"}";
        
        ClassWithPropOverrides vanilla = plainMapper.readValue(JSON, ClassWithPropOverrides.class);
        ClassWithPropOverrides burnt = abMapper.readValue(JSON, ClassWithPropOverrides.class);
        
        assertEquals("a", vanilla.a);
        assertEquals("Foo:b", vanilla.b);
        
        assertEquals("a", burnt.a);
        assertEquals("Foo:b", burnt.b);
    }

    @Test
    public void testStringDeserOverideNoAfterburner() throws Exception
    {
        final String json = "{\"field\": \"value &amp; value\"}";
        final String EXP = "value & value";
        Issue59Bean resultVanilla = new ObjectMapper()
            .registerModule(new SimpleModule("module", Version.unknownVersion())
                .addDeserializer(String.class, new DeAmpDeserializer()))
            .readValue(json, Issue59Bean.class);
        assertEquals(EXP, resultVanilla.field);
    }

    // for [module-afterburner#59]
    @Test
    public void testStringDeserOverideWithAfterburner() throws Exception
    {
        final String json = "{\"field\": \"value &amp; value\"}";
        final String EXP = "value & value";

        final SimpleModule module = new SimpleModule("module", Version.unknownVersion()) {
            @Override
            public void setupModule(SetupContext context) {
                context.addDeserializers(
                        new Deserializers.Base() {
                            @Override
                            public JsonDeserializer<?> findBeanDeserializer(
                                    JavaType type,
                                    DeserializationConfig config,
                                    BeanDescription beanDesc)
                                    throws JsonMappingException {
                                if (type.hasRawClass(String.class)) {
                                    return new DeAmpDeserializer();
                                }
                                return null;
                            }
                        });
            }
        };
        
        // but then fails with Afterburner
        Issue59Bean resultAB = newObjectMapper()
            .registerModule(module)
            .readValue(json, Issue59Bean.class);
        assertEquals(EXP, resultAB.field);
    }
}
