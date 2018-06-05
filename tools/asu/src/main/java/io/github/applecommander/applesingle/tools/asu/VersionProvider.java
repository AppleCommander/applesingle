package io.github.applecommander.applesingle.tools.asu;

import io.github.applecommander.applesingle.AppleSingle;
import picocli.CommandLine.IVersionProvider;

/** Display version information.  Note that this is dependent on the Spring Boot Gradle plugin configuration. */
public class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
    	return new String[] { 
    		String.format("CLI: %s", Main.class.getPackage().getImplementationVersion()),
    		String.format("API: %s", AppleSingle.VERSION)
		};
    }
}