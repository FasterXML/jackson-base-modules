package com.fasterxml.jackson.module.blackbird;

import com.fasterxml.jackson.databind.util.NativeImageUtil;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.module.blackbird.deser.BBDeserializerModifier;
import com.fasterxml.jackson.module.blackbird.ser.BBSerializerModifier;

public class BlackbirdModule extends Module
{
    /**
     * <a href="https://github.com/FasterXML/jackson-modules-base/issues/191">[modules-base#191] Native image detection</a>
     * 
     * @since 2.16
     */
    private static final boolean RUNNING_IN_SVM = NativeImageUtil.isRunningInNativeImage();
    
    private Function<Class<?>, Lookup> _lookups;

    public BlackbirdModule() {
        this(MethodHandles::lookup);
    }

    public BlackbirdModule(Function<Class<?>, MethodHandles.Lookup> lookups) {
        _lookups = lookups;
    }

    public BlackbirdModule(Supplier<MethodHandles.Lookup> lookup) {
        this(c -> {
            final String className = c.getName();
            return (className.startsWith("java.")
                    // 23-Apr-2021, tatu: [modules-base#131] "sun.misc" problematic too
                    || className.startsWith("sun.misc."))
                ? null : lookup.get();
        });
    }

    @Override
    public void setupModule(SetupContext context)
    {
        if (RUNNING_IN_SVM)
        {
            return;
        }
        CrossLoaderAccess openSesame = new CrossLoaderAccess();
        context.addBeanDeserializerModifier(new BBDeserializerModifier(_lookups, openSesame));
        context.addBeanSerializerModifier(new BBSerializerModifier(_lookups, openSesame));
    }

    @Override
    public String getModuleName() {
        return getClass().getSimpleName();
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }
}

