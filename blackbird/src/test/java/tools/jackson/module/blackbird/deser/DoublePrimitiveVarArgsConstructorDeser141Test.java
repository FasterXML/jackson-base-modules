package tools.jackson.module.blackbird.deser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.module.blackbird.BlackbirdTestBase;

// [modules-base#141]
public class DoublePrimitiveVarArgsConstructorDeser141Test extends BlackbirdTestBase
{
    // [modules-base#141]
    static class Foo141 {
        @JsonProperty("bar")
        double[] bar;

        @JsonCreator
        public Foo141(@JsonProperty("bar") double... bar) {
          this.bar = bar;
        }
    }

    private final ObjectMapper MAPPER = newObjectMapper();

    // [modules-base#141]
    public void testDoubleVarargsCreator() throws Exception
    {
        Foo141 foo = new Foo141(new double[] { 2.0, 0.25 });
        String serialized = MAPPER.writeValueAsString(foo);
        Foo141 foo2 = MAPPER.readValue(serialized, Foo141.class);

        assertEquals(2, foo2.bar.length);
        assertEquals(0.25, foo2.bar[1]);
    }
}
