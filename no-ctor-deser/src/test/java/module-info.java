// No-Constructor module (unit) Test Module descriptor
module tools.jackson.module.noctordeser
{
    // Since we are not split from Main artifact, will not
    // need to depend on Main artifact -- but need its dependencies

    requires tools.jackson.core;
    requires transitive tools.jackson.databind;

    requires jdk.unsupported; 

    // Additional test lib/framework dependencies

    requires junit; // JUnit4 To Be Removed in future

    // Further, need to open up some packages for JUnit et al
    opens tools.jackson.module.noctordeser;
}
