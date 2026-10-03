/*
 * AppleSingle - A Java library and command-line tool for AppleSingle support.
 * Copyright (C) 2026  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.applecommander.applesingle.tools.asu;

import org.applecommander.applesingle.AppleSingle;
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