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

import java.util.HashMap;
import java.util.Map;

import picocli.CommandLine.ITypeConverter;

/** Add support for the more common ProDOS file type strings as well as integers. */
public class ProdosFileTypeConverter extends IntegerTypeConverter implements ITypeConverter<Integer> {
	private final Map<String,String> fileTypes = new HashMap<String,String>() {
		private static final long serialVersionUID = 1812781095833750521L;
		{
			put("TXT", "$04");
			put("BIN", "$06");
			put("INT", "$fa");
			put("BAS", "$fc");
			put("REL", "$fe");
			put("SYS", "$ff");
		}
	};

	@Override
	public Integer convert(String value) {
		// If we find the string in our map, swap it for the correct hex string.
		// Else just pass in the original value.
		return super.convert(fileTypes.getOrDefault(value.toUpperCase(), value));
	}
}
