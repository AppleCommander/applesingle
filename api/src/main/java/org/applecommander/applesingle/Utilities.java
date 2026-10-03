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
package org.applecommander.applesingle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utilities {
	private Utilities() { /* Prevent construction */ }
	
	/** Utility method to read all bytes from an InputStream. */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (true) {
			byte[] buf = new byte[1024];
			int len = inputStream.read(buf);
			if (len == -1) break;
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		return outputStream.toByteArray();
	}

	/** Convert bytes in an Entry to a 7-bit ASCII string.  Emphasis on 7-bit in case Apple II high bit is along for the ride. */
	public static String entryToAsciiString(Entry entry) {
		byte[] data = entry.getData();
		for (int i=0; i<data.length; i++) {
			data[i] = (byte)(data[i] & 0x7f);
		}
		return new String(data);
	}
}
