package tools.jackson.module.noctordeser;

import tools.jackson.core.Version;

import tools.jackson.databind.JacksonModule;

public class NoCtorDeserModule extends JacksonModule
{
    @Override
    public String getModuleName() {
        return getClass().getSimpleName();
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addHandler(new MissingInstantiatorHandler());
    }
}
