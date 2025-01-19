package com.fasterxml.jackson.module.jaxb;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public abstract class BaseJaxbTest
{
    public static class NoCheckSubTypeValidator
        extends PolymorphicTypeValidator.Base
    {
        private static final long serialVersionUID = 1L;
    
        @Override
        public Validity validateBaseType(MapperConfig<?> config, JavaType baseType) {
            return Validity.ALLOWED;
        }
    }

    protected BaseJaxbTest() { }
    
    /*
    /**********************************************************************
    /* Factory methods
    /**********************************************************************
     */

    // @since 2.9
    protected ObjectMapper newObjectMapper()
    {
        return getJaxbAndJacksonMapper();
    }

    protected ObjectMapper getJaxbMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector intr = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
        mapper.setAnnotationIntrospector(intr);
        return mapper;
    }

    @SuppressWarnings("deprecation")
    protected MapperBuilder<?,?> getJaxbMapperBuilder()
    {
        return JsonMapper.builder()
                .annotationIntrospector(new JaxbAnnotationIntrospector());
    }

    protected ObjectMapper getJaxbAndJacksonMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector intr = new AnnotationIntrospectorPair(new JaxbAnnotationIntrospector(
                mapper.getTypeFactory()), new JacksonAnnotationIntrospector());
        mapper.setAnnotationIntrospector(intr);
        return mapper;
    }

    protected ObjectMapper getJacksonAndJaxbMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector intr = new AnnotationIntrospectorPair(new JacksonAnnotationIntrospector(),
                new JaxbAnnotationIntrospector(mapper.getTypeFactory()) );
        mapper.setAnnotationIntrospector(intr);
        return mapper;
    }
    
    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    @SuppressWarnings("unchecked")
    protected Map<String,Object> writeAndMap(ObjectMapper m, Object value)
        throws IOException
    {
        String str = m.writeValueAsString(value);
        return (Map<String,Object>) m.readValue(str, Map.class);
    }

    protected Map<String,Object> writeAndMap(Object value)
        throws IOException
    {
        return writeAndMap(new ObjectMapper(), value);
    }

    protected String serializeAsString(ObjectMapper m, Object value)
        throws IOException
    {
        return m.writeValueAsString(value);
    }

    protected String serializeAsString(Object value)
        throws IOException
    {
        return serializeAsString(new ObjectMapper(), value);
    }

    /*
    /**********************************************************
    /* Helper methods, other
    /**********************************************************
     */

    public String quote(String str) {
        return '"'+str+'"';
    }

    protected static String a2q(String json) {
        return json.replace("'", "\"");
    }
}
