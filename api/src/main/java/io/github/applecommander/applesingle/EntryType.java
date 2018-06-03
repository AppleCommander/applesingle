package io.github.applecommander.applesingle;

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
