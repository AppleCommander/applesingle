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

import java.io.IOException;
import java.io.OutputStream;

/** An OutputStream that doesn't do output. */
public class NullOutputStream extends OutputStream {
	public static final NullOutputStream INSTANCE = new NullOutputStream();

	private NullOutputStream() { /* Prevent construction */	}
	
	@Override
	public void write(int b) throws IOException {
		// Do Nothing
	}
}
