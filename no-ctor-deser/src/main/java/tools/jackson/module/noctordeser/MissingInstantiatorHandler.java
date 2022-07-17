package tools.jackson.module.noctordeser;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;

import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.module.noctordeser.util.ReflectionUtil;

public class MissingInstantiatorHandler extends DeserializationProblemHandler
{
    private final ReflectionUtil _util = new ReflectionUtil();

    @Override
    public Object handleMissingInstantiator(DeserializationContext ctxt,
            Class<?> instClass, ValueInstantiator valueInst,
            JsonParser jsonParser, String msg)
        throws JacksonException
    {
        // Overall this is not optimal since we really should only replace what
        // `ValueInstantiator` does -- but that is difficult in 2.x.
        // So instead we'll have to re-fetch deserializer, call with constructed
        // instance, using `deserialize()` method that takes the instance.
        
        // Let's first verify that no default constructor was found
        // (just as a sanity check)
        if (!valueInst.canCreateUsingDefault()) {
            Object instance = _util.newConstructorAndCreateInstance(ctxt, instClass);
            if (instance != null) {
                // Unfortunate that we'll have to re-fetch the assume (shouldn't have to)
                ValueDeserializer<Object> deserializer = ctxt.findRootValueDeserializer(ctxt.constructType(instClass));
                if (deserializer != null) {
                    return deserializer.deserialize(jsonParser, ctxt, instance);
                }
            }
        }
        return NOT_HANDLED;
    }
}
