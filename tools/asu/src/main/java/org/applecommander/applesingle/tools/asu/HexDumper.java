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

import java.io.PrintStream;
import java.util.Arrays;

/** A slightly-configurable reusable hex dumping mechanism. */
public class HexDumper {
	private PrintStream ps = System.out;
	private int lineWidth = 16;
	private LinePrinter printLine;
	
	public static HexDumper standard() {
		HexDumper hd = new HexDumper();
		hd.printLine = hd::standardLine;
		return hd;
	}
	public static HexDumper alternate(LinePrinter linePrinter) {
		HexDumper hd = new HexDumper();
		hd.printLine = linePrinter;
		return hd;
	}
	
	private HexDumper() {
		// Prevent construction
	}
	
	public void dump(int address, byte[] data, String description) {
		int offset = 0;
		while (offset < data.length) {
			byte[] line = Arrays.copyOfRange(data, offset, Math.min(offset+lineWidth,data.length));
			printLine.print(address+offset, line, description);
			description = "";	// Only on first line!
			offset += line.length;
		}
		if (data.length == 0) {
		    printLine.print(address+offset, data, String.format("%s (empty)", description));
		}
	}
	
	public void standardLine(int address, byte[] data, String description) {
		ps.printf("%04x: ", address);
		for (int i=0; i<lineWidth; i++) {
			if (i < data.length) {
				ps.printf("%02x ", data[i]);
			} else {
				ps.printf(".. ");
			}
		}
		ps.print("| ");
		for (int i=0; i<lineWidth; i++) {
			char ch = ' ';
			if (i < data.length) {
				byte b = data[i];
				ch = (b >= ' ' && Byte.toUnsignedInt(b) < 0x7f) ? (char)b : '.';
			}
			ps.printf("%c", ch);
		}
		ps.printf(" | %s\n", description);
	}
	
	@FunctionalInterface
	public interface LinePrinter {
		public void print(int address, byte[] data, String description);
	}
}
