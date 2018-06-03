package io.github.applecommander.applesingle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.function.IntSupplier;

public class FileDatesInfo {
	/** The number of seconds at the begining of the AppleSingle date epoch since the Unix epoch began. */
	public static final Instant EPOCH_INSTANT = Instant.parse("2000-01-01T00:00:00.00Z");
	/** Per the AppleSingle technical notes. */
	public static final int UNKNOWN_DATE = 0x80000000;
	/** Number of bytes a File Dates Info takes per AppleSingle spec. */
	public static final int BYTES = 16;

	// Package scoped so AppleSingle Builder is able to set
	int creation;
	int modification;
	int backup;
	int access;
	
	public static int fromInstant(Instant instant) {
		return (int)(instant.getEpochSecond() - EPOCH_INSTANT.getEpochSecond());
	}
	public static FileDatesInfo fromEntry(Entry entry) {
		ByteBuffer infoData = entry.getBuffer();
		int creation = infoData.getInt();
		int modification = infoData.getInt();
		int backup = infoData.getInt();
		int access = infoData.getInt();
		return new FileDatesInfo(creation, modification, backup, access);
	}
	
	public FileDatesInfo() {
		int current = FileDatesInfo.fromInstant(Instant.now());
		this.creation = current;
		this.modification = current;
		this.backup = current;
		this.access = current;
	}
	public FileDatesInfo(int creation, int modification, int backup, int access) {
		this.creation = creation;
		this.modification = modification;
		this.backup = backup;
		this.access = access;
	}
	
	public Entry toEntry() {
		ByteBuffer buf = ByteBuffer.allocate(BYTES).order(ByteOrder.BIG_ENDIAN);
		buf.putInt(creation);
		buf.putInt(modification);
		buf.putInt(backup);
		buf.putInt(access);
		return Entry.create(EntryType.FILE_DATES_INFO, buf.array());
	}
	
	public Instant getCreationInstant() {
		return toInstant(this::getCreation);
	}
	public Instant getModificationInstant() {
		return toInstant(this::getModification);
	}
	public Instant getBackupInstant() {
		return toInstant(this::getBackup);
	}
	public Instant getAccessInstant() {
		return toInstant(this::getAccess);
	}
	
	/** Utility method to convert the int to a valid Unix epoch and Java Instant. */
	public Instant toInstant(IntSupplier timeSupplier) {
		return Instant.ofEpochSecond(timeSupplier.getAsInt() + EPOCH_INSTANT.getEpochSecond());
	}
	
	public int getCreation() {
		return creation;
	}
	public int getModification() {
		return modification;
	}
	public int getBackup() {
		return backup;
	}
	public int getAccess() {
		return access;
	}
}
