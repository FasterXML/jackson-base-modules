package com.fasterxml.jackson.module.afterburner.deser;

import java.io.IOException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdValueInstantiator;
import tools.jackson.databind.type.TypeFactory;

/**
 * Base class for concrete bytecode-generated value instantiators.
 */
public abstract class OptimizedValueInstantiator
    extends StdValueInstantiator
{
    /**
     * Default constructor which is only used when creating
     * dummy instance to call factory method.
     */
    protected OptimizedValueInstantiator() {
        super(/*DeserializationConfig*/null, TypeFactory.unknownType());
    }

    /**
     * Copy-constructor to use for creating actual optimized instances.
     */
    protected OptimizedValueInstantiator(StdValueInstantiator src) {
        super(src);
    }

    /**
     * Need to override this, now that we have installed default creator.
     */
    @Override
    public boolean canCreateUsingDefault() {
        return true;
    }
    
    protected abstract OptimizedValueInstantiator with(StdValueInstantiator src);

    /* Define as abstract to ensure that it gets reimplemented; or if not,
     * we get a specific error (too easy to break, and get cryptic error)
     */
    @Override
    public abstract Object createUsingDefault(DeserializationContext ctxt)
        throws JacksonException;

    /**
     * Helper method needed to allow handling of instantiation exception in
     * case of optimized no-args creator.
     */
    protected Object _handleInstantiationProblem(DeserializationContext ctxt, Exception e)
        throws IOException
    {
        return ctxt.handleInstantiationProblem(_valueClass, null,
                rewrapCtorProblem(ctxt, e));
    }
}
