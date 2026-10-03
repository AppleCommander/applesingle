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

public enum EntryType {
	DATA_FORK(1, "Data Fork"),
	RESOURCE_FORK(2, "Resource Fork"),
	REAL_NAME(3, "Real Name"),
	COMMENT(4, "Comment"),
	ICON_BW(5, "Icon, B&W"),
	ICON_COLOR(6, "Icon, Color"),
	FILE_INFO(7, "File Info"),
	FILE_DATES_INFO(8, "File Dates Info"),
	FINDER_INFO(9, "Finder Info"),
	MACINTOSH_FILE_INFO(10, "Macintosh File Info"),
	PRODOS_FILE_INFO(11, "ProDOS File Info"),
	MSDOS_FILE_INFO(12, "MS-DOS File Info"),
	SHORT_NAME(13, "Short Name"),
	AFP_FILE_INFO(14, "AFP File Info"),
	DIRECTORY_ID(15, "Directory ID");
	
	public static final String findNameOrUnknown(Entry entry) {
		for (EntryType et : values()) {
			if (et.entryId == entry.getEntryId()) {
				return et.name;
			}
		}
		return "Unknown";
	}
	public static final EntryType find(int entryId) {
		for (EntryType et : values()) {
			if (et.entryId == entryId) {
				return et;
			}
		}
		throw new IllegalArgumentException(String.format("Unable to find EntryType # %d", entryId));
	}
	
	public final int entryId;
	public final String name;
	
	private EntryType(int entryId, String name) {
		this.entryId = entryId;
		this.name= name;
	}
}
