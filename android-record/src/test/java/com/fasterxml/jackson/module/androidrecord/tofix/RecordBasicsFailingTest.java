package com.fasterxml.jackson.module.androidrecord.tofix;

import org.junit.jupiter.api.Test;

import com.android.tools.r8.RecordTag;

import com.fasterxml.jackson.annotation.JacksonInject;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.module.androidrecord.BaseMapTest;
import com.fasterxml.jackson.module.androidrecord.RecordBasicsTest;
import com.fasterxml.jackson.module.androidrecord.testutil.failure.JacksonTestFailureExpected;

import static org.junit.jupiter.api.Assertions.fail;

public class RecordBasicsFailingTest extends BaseMapTest
{
  static final class RecordWithHeaderInject extends RecordTag
  {
    private final int id;
    @JacksonInject
    private final String name;

    RecordWithHeaderInject(int id, @JacksonInject String name) {
      this.id = id;
      this.name = name;
    }

    public int id() {
      return id;
    }

    @JacksonInject
    public String name() {
      return name;
    }
  }

  private final ObjectMapper MAPPER = newJsonMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  // Fails by deserializing successfully, even though annotations on header are "propagated" to the field

  /**
   * This test-case is just for documentation purpose:
   * GOTCHA: Annotations on header will be propagated to the field, leading to this failure.
   *
   * @see RecordBasicsTest#testDeserializeConstructorInjectRecord()
   */
  @JacksonTestFailureExpected
  @Test
  public void testDeserializeHeaderInjectRecord_WillFail() throws Exception {
    MAPPER.setInjectableValues(new InjectableValues.Std().addValue(String.class, "Bob"));

    try {
      MAPPER.readValue("{\"id\":123}", RecordWithHeaderInject.class);

      fail("should not pass");
    } catch (IllegalArgumentException e) {
      verifyException(e, "RecordWithHeaderInject#name");
      verifyException(e, "Can not set final java.lang.String field");
    }
  }
}
