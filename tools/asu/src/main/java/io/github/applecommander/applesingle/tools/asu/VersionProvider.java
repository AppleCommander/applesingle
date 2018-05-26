package io.github.applecommander.applesingle.tools.asu;

import picocli.CommandLine.IVersionProvider;

/** Display version information.  Note that this is dependent on the Spring Boot Gradle plugin configuration. */
public class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
    	return new String[] { Main.class.getPackage().getImplementationVersion() };
    }
}