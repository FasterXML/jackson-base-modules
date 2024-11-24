package tools.jackson.module.afterburner.deser;

import java.util.*;

import tools.jackson.core.*;

import tools.jackson.databind.*;
import tools.jackson.databind.deser.*;
import tools.jackson.databind.deser.bean.BeanDeserializer;
import tools.jackson.databind.deser.bean.BeanPropertyMap;
import tools.jackson.databind.deser.bean.PropertyBasedCreator;
import tools.jackson.databind.deser.impl.UnwrappedPropertyHandler;
import tools.jackson.databind.util.NameTransformer;

public final class SuperSonicBeanDeserializer
    extends SuperSonicBDBase
{
    /*
    /**********************************************************
    /* Life-cycle, construction, initialization
    /**********************************************************
     */

    public SuperSonicBeanDeserializer(BeanDeserializer src, List<SettableBeanProperty> props)
    {
        super(src, props);
    }

    protected SuperSonicBeanDeserializer(SuperSonicBeanDeserializer src,
            UnwrappedPropertyHandler unwrapHandler, PropertyBasedCreator propertyBasedCreator,
            BeanPropertyMap renamedProperties, boolean ignoreAllUnknown)
    {
        super(src, unwrapHandler, propertyBasedCreator, renamedProperties, ignoreAllUnknown);
    }
    
    @Override
    public ValueDeserializer<Object> unwrappingDeserializer(DeserializationContext ctxt,
            NameTransformer transformer)
    {
        // NOTE: copied verbatim from `BeanDeserializer`

        if (_currentlyTransforming == transformer) { // from [databind#383]
            return this;
        }
        _currentlyTransforming = transformer;
        try {
            UnwrappedPropertyHandler uwHandler = _unwrappedPropertyHandler;
            if (uwHandler != null) {
                uwHandler = uwHandler.renameAll(ctxt, transformer);
            }
            PropertyBasedCreator pbCreator = _propertyBasedCreator;
            if (pbCreator != null) {
                pbCreator = pbCreator.renameAll(ctxt, transformer);
            }
            return new SuperSonicBeanDeserializer(this, uwHandler, pbCreator,
                    _beanProperties.renameAll(ctxt, transformer), true);
        } finally { _currentlyTransforming = null; }
    }

    // // Others, let's just leave as is; will not be optimized?
    
    //public BeanDeserializer withObjectIdReader(ObjectIdReader oir) {

    //public BeanDeserializer withIgnorableProperties(HashSet<String> ignorableProps)
    
    //protected BeanDeserializerBase asArrayDeserializer()
    
    /*
    /**********************************************************
    /* Deserialization method implementations
    /**********************************************************
     */

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        if (!_vanillaProcessing || (_objectIdReader != null)) {
            // should we ever get here? Just in case
            return super.deserialize(p, ctxt);
        }
        // common case first:
        if (!p.isExpectedStartObjectToken()) {
            return _deserializeOther(p, ctxt, p.currentToken());
        }
        if (_nonStandardCreation) {
            p.nextToken();
            if (_unwrappedPropertyHandler != null) {
                return deserializeWithUnwrapped(p, ctxt);
            }
            if (_externalTypeIdHandler != null) {
                return deserializeWithExternalTypeId(p, ctxt);
            }
            Object bean = deserializeFromObjectUsingNonDefault(p, ctxt);
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        final Object bean = _valueInstantiator.createUsingDefault(ctxt);
        // [databind#631]: Assign current value, to be accessible by custom serializers
        p.assignCurrentValue(bean);
        if (p.canReadObjectId()) {
            Object id = p.getObjectId();
            if (id != null) {
                _handleTypedObjectId(p, ctxt, bean, id);
            }
        }
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }

        // We also we have at least 6 properties, so roll out first few
        SettableBeanProperty prop = _orderedProperties[0];
        do {
            try {
                if (p.nextName(_orderedPropertyNames[0])) {
                    p.nextToken();
                    prop.deserializeAndSet(p, ctxt, bean);
                    if (p.nextName(_orderedPropertyNames[1])) {
                        prop = _orderedProperties[1];
                        p.nextToken();
                        prop.deserializeAndSet(p, ctxt, bean);
                        if (p.nextName(_orderedPropertyNames[2])) {
                            prop = _orderedProperties[2];
                            p.nextToken();
                            prop.deserializeAndSet(p, ctxt, bean);
                            if (p.nextName(_orderedPropertyNames[3])) {
                                prop = _orderedProperties[3];
                                p.nextToken();
                                prop.deserializeAndSet(p, ctxt, bean);
                                break; // yay! We did it!
                            }
                        }
                    }
                }
                return _deserializeDisordered(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        } while (false);

        for (int i = 4, len = _orderedProperties.length; i < len; ++i) {
            prop = _orderedProperties[i];
            if (!p.nextName(_orderedPropertyNames[i])) { // miss...
                if (p.currentToken() == JsonToken.END_OBJECT) {
                    return bean;
                }
                // we likely point to FIELD_NAME still; offline
                return _deserializeDisordered(p, ctxt, bean);
            }
            p.nextToken(); // skip field, returns value token
            try {
                prop.deserializeAndSet(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        }
        // also, need to ensure we get closing END_OBJECT...
        if (p.nextToken() != JsonToken.END_OBJECT) {
            return _deserializeDisordered(p, ctxt, bean);
        }
        return bean;
    }

    // much of below is cut'n pasted from BeanSerializer
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt, Object bean)
        throws JacksonException
    {
        // [databind#631]: Assign current value, to be accessible by custom serializers
        p.assignCurrentValue(bean);
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        if (_unwrappedPropertyHandler != null) {
            return deserializeWithUnwrapped(p, ctxt, bean);
        }
        if (_externalTypeIdHandler != null) {
            return deserializeWithExternalTypeId(p, ctxt, bean);
        }
        // We also we have at least 6 properties, so roll out first few
        SettableBeanProperty prop = _orderedProperties[0];
        do {
            try {
                if (p.isExpectedStartObjectToken()) {
                    if (!p.nextName(_orderedPropertyNames[0])) {
                        return _deserializeDisordered(p, ctxt, bean);
                    }
                } else if (!p.hasToken(JsonToken.PROPERTY_NAME)
                        || !prop.getName().equals(p.currentName())) {
                    return _deserializeDisordered(p, ctxt, bean);
                }
                p.nextToken();
                prop.deserializeAndSet(p, ctxt, bean);
                if (p.nextName(_orderedPropertyNames[1])) {
                    prop = _orderedProperties[1];
                    p.nextToken();
                    prop.deserializeAndSet(p, ctxt, bean);
                    if (p.nextName(_orderedPropertyNames[2])) {
                        prop = _orderedProperties[2];
                        p.nextToken();
                        prop.deserializeAndSet(p, ctxt, bean);
                        if (p.nextName(_orderedPropertyNames[3])) {
                            prop = _orderedProperties[3];
                            p.nextToken();
                            prop.deserializeAndSet(p, ctxt, bean);
                            break; // yay! We did it!
                        }
                    }
                }
                return _deserializeDisordered(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        } while (false);

        // then rest of properties
        for (int i = 4, len = _orderedProperties.length; i < len; ++i) {
            if (!p.nextName(_orderedPropertyNames[i])) { // miss...
                if (p.hasToken(JsonToken.END_OBJECT)) {
                    return bean;
                }
                // we likely point to FIELD_NAME, so can just call parent impl
                return super.deserialize(p, ctxt, bean);
            }
            prop = _orderedProperties[i];
            p.nextToken(); // skip field, returns value token
            try {
                prop.deserializeAndSet(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        }
        // also, need to ensure we get closing END_OBJECT...
        if (p.nextToken() != JsonToken.END_OBJECT) {
            return super.deserialize(p, ctxt, bean);
        }
        return bean;
    }

    // much of below is cut'n pasted from BeanSerializer
    @Override
    public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt)
        throws JacksonException
    {
        // See BeanDeserializer.deserializeFromObject [databind#622]
        // Allow Object Id references to come in as JSON Objects as well...
        if ((_objectIdReader != null) && _objectIdReader.maySerializeAsObject()) {
            if (p.hasTokenId(JsonTokenId.ID_PROPERTY_NAME)
                    && _objectIdReader.isValidReferencePropertyName(p.currentName(), p)) {
                return deserializeFromObjectId(p, ctxt);
            }
        }
        if (_nonStandardCreation) {
            if (_unwrappedPropertyHandler != null) {
                return deserializeWithUnwrapped(p, ctxt);
            }
            if (_externalTypeIdHandler != null) {
                return deserializeWithExternalTypeId(p, ctxt);
            }
            Object bean = deserializeFromObjectUsingNonDefault(p, ctxt);
            if (_injectables != null) {
                injectValues(ctxt, bean);
            }
            return bean;
        }
        final Object bean = _valueInstantiator.createUsingDefault(ctxt);
        // [databind#631]: Assign current value, to be accessible by custom serializers
        p.assignCurrentValue(bean);
        if (p.canReadObjectId()) {
            Object id = p.getObjectId();
            if (id != null) {
                _handleTypedObjectId(p, ctxt, bean, id);
            }
        }
        if (_injectables != null) {
            injectValues(ctxt, bean);
        }
        // We also we have at least 6 properties, so roll out first few
        SettableBeanProperty prop = _orderedProperties[0];
        do {
            try {
                if (p.isExpectedStartObjectToken()) {
                    if (!p.nextName(_orderedPropertyNames[0])) {
                        return _deserializeDisordered(p, ctxt, bean);
                    }
                } else if (!p.hasToken(JsonToken.PROPERTY_NAME)
                        || !prop.getName().equals(p.currentName())) {
                    return _deserializeDisordered(p, ctxt, bean);
                }
                p.nextToken();
                prop.deserializeAndSet(p, ctxt, bean);
                if (p.nextName(_orderedPropertyNames[1])) {
                    prop = _orderedProperties[1];
                    p.nextToken();
                    prop.deserializeAndSet(p, ctxt, bean);
                    if (p.nextName(_orderedPropertyNames[2])) {
                        prop = _orderedProperties[2];
                        p.nextToken();
                        prop.deserializeAndSet(p, ctxt, bean);
                        if (p.nextName(_orderedPropertyNames[3])) {
                            prop = _orderedProperties[3];
                            p.nextToken();
                            prop.deserializeAndSet(p, ctxt, bean);
                            break; // yay! We did it!
                        }
                    }
                }
                return _deserializeDisordered(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        } while (false);

        // then rest of properties
        for (int i = 4, len = _orderedProperties.length; i < len; ++i) {
            if (!p.nextName(_orderedPropertyNames[i])) { // miss...
                if (p.hasToken(JsonToken.END_OBJECT)) {
                    return bean;
                }
                // we likely point to FIELD_NAME, so can just call parent impl
                return super.deserialize(p, ctxt, bean);
            }
            prop = _orderedProperties[i];
            p.nextToken(); // skip field, returns value token
            try {
                prop.deserializeAndSet(p, ctxt, bean);
            } catch (Exception e) {
                wrapAndThrow(e, bean, prop.getName(), ctxt);
            }
        }
        // also, need to ensure we get closing END_OBJECT...
        if (p.nextToken() != JsonToken.END_OBJECT) {
            return _handleUnexpectedWithin(p, ctxt, bean);
        }
        return bean;
    }
}
