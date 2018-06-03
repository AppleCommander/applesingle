package io.github.applecommander.applesingle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class Entry {
	public static final int LENGTH = 12;
	private int entryId;
	private int offset;
	private int length;
	private byte[] data;
	
	public static Entry create(AppleSingleReader reader) {
		Objects.requireNonNull(reader);
		
		ByteBuffer buffer = reader.read(LENGTH, "Entry header");
		Entry entry = new Entry();
		entry.entryId = buffer.getInt();
		entry.offset = buffer.getInt();
		entry.length = buffer.getInt();
		
		entry.data = reader.readAt(entry.offset, entry.length, EntryType.findNameOrUnknown(entry)).array();
		return entry;
	}
	public static Entry create(EntryType type, byte[] data) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(data);
		Entry entry = new Entry();
		entry.entryId = type.entryId;
		entry.offset = -1;
		entry.length = data.length;
		entry.data = data;
		return entry;
	}
	
	public int getEntryId() {
		return entryId;
	}
	public int getOffset() {
		return offset;
	}
	public int getLength() {
		return length;
	}
	public byte[] getData() {
		return data;
	}
	public ByteBuffer getBuffer() {
		return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer();
	}
}
