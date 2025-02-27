package com.fasterxml.jackson.module.jakarta.xmlbind.adapters;

import java.util.*;

import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.module.jakarta.xmlbind.ModuleTestBase;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for checking that JAXB type adapters work (to some
 * degree, anyway).
 */
public class TestAdapters extends ModuleTestBase
{
    public static class SillyAdapter extends XmlAdapter<String, Date>
    {
        public SillyAdapter() { }

        @Override
        public Date unmarshal(String date) throws Exception {
            return new Date(29L);
        }

        @Override
        public String marshal(Date date) throws Exception {
            return "XXX";
        }
    }

    static class Bean
    {
        @XmlJavaTypeAdapter(SillyAdapter.class)
        public Date value;

        public Bean() { }
        public Bean(long l) { value = new Date(l); }
    }

    // For [JACKSON-288]
    
    static class Bean288 {
        public List<Person> persons;

        public Bean288() { }
        public Bean288(String str) {
            persons = new ArrayList<Person>();
            persons.add(new Person(str));
        }
    }

    static class Person
    {
        public String name;
        
        @XmlElement(required = true, type = String.class)
        @XmlJavaTypeAdapter(DateAdapter.class)
        protected Calendar date;

        public Person() { }
        public Person(String n) {
            name = n;
            date = Calendar.getInstance();
            date.setTime(new Date(0L));
        }
    }

    public static class DateAdapter
        extends XmlAdapter<String, Calendar>
    {
        public DateAdapter() { }
        
        @Override
        public Calendar unmarshal(String value) {
            return DatatypeConverter.parseDateTime(value);
        }

        @Override
        public String marshal(Calendar value) {
            if (value == null) {
                return null;
            }
            return DatatypeConverter.printDateTime(value);
        }
    }
    
    // [JACKSON-656]

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Paging", propOrder = { "numFound" })
    public static class Bean656 {
            @XmlElement(type = String.class)
            @XmlJavaTypeAdapter(Adapter1.class)
            @XmlSchemaType(name = "long")
            protected Long numFound;

            public Long getNumFound() {
                    return numFound;
            }

            public void setNumFound(Long value) {
                    this.numFound = value;
            }
    }

    public static class Adapter1 extends XmlAdapter<String, Long> {
        @Override
        public Long unmarshal(String value) {
            return (long) DatatypeConverter.parseLong(value);
        }

        @Override
        public String marshal(Long value) {
            if (value == null) {
                return null;
            }
            return DatatypeConverter.printLong((long) value);
        }   
    }    

    // [Issue-10]: Infinite recursion in "self" adapters
    public static class IdentityAdapter extends XmlAdapter<IdentityAdapterBean, IdentityAdapterBean> {
        @Override
        public IdentityAdapterBean unmarshal(IdentityAdapterBean b) {
            b.value += "U";
            return b;
        }

        @Override
        public IdentityAdapterBean marshal(IdentityAdapterBean b) {
            if (b != null) {
                b.value += "M";
            }
            return b;
        }   
    }

    @XmlJavaTypeAdapter(IdentityAdapter.class)
    static class IdentityAdapterBean
    {
        public String value;

        public IdentityAdapterBean() { }
        public IdentityAdapterBean(String s) { value = s; }
    }

    static class IdentityAdapterPropertyBean
    {
        @XmlJavaTypeAdapter(IdentityAdapter.class)
        public String value;

        public IdentityAdapterPropertyBean() { }
        public IdentityAdapterPropertyBean(String s) { value = s; }
    }
    
    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    @Test
    public void testSimpleAdapterSerialization() throws Exception
    {
        Bean bean = new Bean(123L);
        assertEquals("{\"value\":\"XXX\"}", getJaxbMapper().writeValueAsString(bean));
    }

    @Test
    public void testSimpleAdapterDeserialization() throws Exception
    {
        Bean bean = getJaxbMapper().readValue("{\"value\":\"abc\"}", Bean.class);
        assertNotNull(bean.value);
        assertEquals(29L, bean.value.getTime());
    }

    // [JACKSON-288]
    @Test
    public void testDateAdapter() throws Exception
    {
        Bean288 input = new Bean288("test");
        ObjectMapper mapper = getJaxbMapper();
        String json = mapper.writeValueAsString(input);
        Bean288 output = mapper.readValue(json, Bean288.class);
        assertNotNull(output);
    }

    // [JACKSON-656]

    @Test
    public void testJackson656() throws Exception
    {
        Bean656 bean = new Bean656();
        bean.setNumFound(3232l);
        ObjectMapper mapper = getJaxbMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"numFound\":\"3232\"}", json);
    }
}
