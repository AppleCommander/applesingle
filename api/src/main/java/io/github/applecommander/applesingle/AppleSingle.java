package io.github.applecommander.applesingle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Support reading of data from and AppleSingle source.
 * Does not implement all components at this time, extend as required.
 * All construction has been deferred to the <code>read(...)</code> or {@link #builder()} methods.
 * <p>
 * Currently supports entries:<br/>
 * 1. Data Fork<br/>
 * 2. Resource Fork<br/>
 * 3. Real Name<br/>
 * 11. ProDOS File Info<br/>
 * 
 * @see <a href="https://github.com/AppleCommander/AppleCommander/issues/20">AppleCommander issue #20</a>
 */
public class AppleSingle {
	public static final int MAGIC_NUMBER = 0x0051600;
	public static final int VERSION_NUMBER1 = 0x00010000;
	public static final int VERSION_NUMBER2 = 0x00020000;
	
	private Map<Integer,Consumer<Entry>> entryConsumers = new HashMap<>();
	{
		entryConsumers.put(1,  entry -> this.dataFork = entry.getData());
		entryConsumers.put(2,  entry -> this.resourceFork = entry.getData());
		entryConsumers.put(3,  entry -> this.realName = Utilities.entryToAsciiString(entry));
		entryConsumers.put(8,  entry -> this.fileDatesInfo = FileDatesInfo.fromEntry(entry));
		entryConsumers.put(11, entry -> this.prodosFileInfo = ProdosFileInfo.fromEntry(entry));
	}

	private byte[] dataFork;
	private byte[] resourceFork;
	private String realName;
	private ProdosFileInfo prodosFileInfo = ProdosFileInfo.standardBIN();
	private FileDatesInfo fileDatesInfo = new FileDatesInfo();

	private AppleSingle() {
		// Allow Builder construction
	}
	private AppleSingle(List<Entry> entries) throws IOException {
		entries.forEach(entry -> {
			Optional.ofNullable(entry)
					.map(Entry::getEntryId)
					.map(entryConsumers::get)
					.ifPresent(c -> c.accept(entry));
		});
	}
	
	public byte[] getDataFork() {
		return dataFork;
	}
	public byte[] getResourceFork() {
		return resourceFork;
	}
	public String getRealName() {
		return realName;
	}
	public ProdosFileInfo getProdosFileInfo() {
		return prodosFileInfo;
	}
	public FileDatesInfo getFileDatesInfo() {
		return fileDatesInfo;
	}
	
	public void save(OutputStream outputStream) throws IOException {
		final boolean hasResourceFork = Objects.nonNull(resourceFork);
		final boolean hasRealName = Objects.nonNull(realName);
		final int entries = 3 + (hasRealName ? 1 : 0) + (hasResourceFork ? 1 : 0);

		int realNameOffset = 26 + (12 * entries);
		int prodosFileInfoOffset = realNameOffset + (hasRealName ? realName.length() : 0);
		int fileDatesInfoOffset = prodosFileInfoOffset + 8;
		int resourceForkOffset = fileDatesInfoOffset + 16;
		int dataForkOffset = resourceForkOffset + (hasResourceFork ? resourceFork.length : 0);
		
		writeFileHeader(outputStream, entries);
		if (hasRealName) writeHeader(outputStream, 3, realNameOffset, realName.length());
		writeHeader(outputStream, 11, prodosFileInfoOffset, 8);
		writeHeader(outputStream, 8, fileDatesInfoOffset, 16);
		if (hasResourceFork) writeHeader(outputStream, 2, resourceForkOffset, resourceFork.length);
		writeHeader(outputStream, 1, dataForkOffset, dataFork.length);
		
		if (hasRealName) writeRealName(outputStream);
		writeProdosFileInfo(outputStream);
		writeFileDatesInfo(outputStream);
		if (hasResourceFork) writeResourceFork(outputStream);
		writeDataFork(outputStream);
	}
	public void save(File file) throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			save(outputStream);
		}
	}
	public void save(Path path) throws IOException {
		try (OutputStream outputStream = Files.newOutputStream(path)) {
			save(outputStream);
		}
	}
	
	private void writeFileHeader(OutputStream outputStream, int numberOfEntries) throws IOException {
		final byte[] filler = new byte[16];
		ByteBuffer buf = ByteBuffer.allocate(26).order(ByteOrder.BIG_ENDIAN);
		buf.putInt(MAGIC_NUMBER);
		buf.putInt(VERSION_NUMBER2);
		buf.put(filler);
		buf.putShort((short)numberOfEntries);
		outputStream.write(buf.array());
	}
	private void writeHeader(OutputStream outputStream, int entryId, int offset, int length) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
		buf.putInt(entryId);
		buf.putInt(offset);
		buf.putInt(length);
		outputStream.write(buf.array());
	}
	private void writeRealName(OutputStream outputStream) throws IOException {
		outputStream.write(realName.getBytes());
	}
	private void writeProdosFileInfo(OutputStream outputStream) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
		buf.putShort((short)prodosFileInfo.access);
		buf.putShort((short)prodosFileInfo.fileType);
		buf.putInt(prodosFileInfo.auxType);
		outputStream.write(buf.array());
	}
	private void writeFileDatesInfo(OutputStream outputStream) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
		buf.putInt(fileDatesInfo.getCreation());
		buf.putInt(fileDatesInfo.getModification());
		buf.putInt(fileDatesInfo.getBackup());
		buf.putInt(fileDatesInfo.getAccess());
		outputStream.write(buf.array());
	}
	private void writeResourceFork(OutputStream outputStream) throws IOException {
		outputStream.write(resourceFork);
	}
	private void writeDataFork(OutputStream outputStream) throws IOException {
		outputStream.write(dataFork);
	}

	public static AppleSingle read(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream, "Please supply an input stream");
		return read(Utilities.toByteArray(inputStream));
	}
	public static AppleSingle read(File file) throws IOException {
		Objects.requireNonNull(file, "Please supply a file");
		return read(file.toPath());
	}
	public static AppleSingle read(Path path) throws IOException {
		Objects.requireNonNull(path, "Please supply a file");
		return read(Files.readAllBytes(path));
	}
	public static AppleSingle read(byte[] data) throws IOException {
		Objects.requireNonNull(data);
		return new AppleSingle(asEntries(data));
	}
	
	public static List<Entry> asEntries(InputStream inputStream) throws IOException {
		Objects.requireNonNull(inputStream);
		return asEntries(Utilities.toByteArray(inputStream));
	}
	public static List<Entry> asEntries(File file) throws IOException {
		Objects.requireNonNull(file);
		return asEntries(file.toPath());
	}
	public static List<Entry> asEntries(Path path) throws IOException {
		Objects.requireNonNull(path);
		return asEntries(Files.readAllBytes(path));
	}
	public static List<Entry> asEntries(byte[] data) throws IOException {
		Objects.requireNonNull(data);
		return asEntries(AppleSingleReader.builder().data(data).build());
	}
	public static List<Entry> asEntries(AppleSingleReader reader) throws IOException {
		Objects.requireNonNull(reader);
		List<Entry> entries = new ArrayList<>();
		required(reader, "Magic number", "Not an AppleSingle file - magic number does not match.", MAGIC_NUMBER);
		int version = required(reader, "Version", "Only AppleSingle version 1 and 2 supported.", VERSION_NUMBER1, VERSION_NUMBER2);
		reader.reportVersion(version);
		reader.read(16, "Filler");
		int numberOfEntries = reader.read(Short.BYTES, "Number of entries").getShort();
		reader.reportNumberOfEntries(numberOfEntries);
		for (int i = 0; i < numberOfEntries; i++) {
			Entry entry = Entry.create(reader);
			entries.add(entry);
			reader.reportEntry(entry);
		}
		return entries;
	}
	private static int required(AppleSingleReader reader, String description, String message, int... expecteds) throws IOException {
		int actual = reader.read(Integer.BYTES, description).getInt();
		for (int expected : expecteds) {
			if (actual == expected) return actual;
		}
		List<String> versions = new ArrayList<>();
		for (int expected : expecteds) versions.add(String.format("0x%08x", expected));
		throw new IOException(String.format("%s  Expected %s but read 0x%08x.", 
				message, String.join(",", versions), actual));
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private AppleSingle as = new AppleSingle();
		public Builder realName(String realName) {
			if (!Character.isAlphabetic(realName.charAt(0))) {
				throw new IllegalArgumentException("ProDOS file names must begin with a letter");
			}
			as.realName = realName.chars()
					.map(this::sanitize)
					.limit(15)
					.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
					.toString();
			return this;
		}
		private int sanitize(int ch) {
			if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
				return Character.toUpperCase(ch);
			}
			return '.';
		}
		public Builder dataFork(byte[] dataFork) {
			as.dataFork = dataFork;
			return this;
		}
		public Builder resourceFork(byte[] resourceFork) {
			as.resourceFork = resourceFork;
			return this;
		}
		public Builder access(int access) {
			as.prodosFileInfo.access = access;
			return this;
		}
		public Builder fileType(int fileType) {
			as.prodosFileInfo.fileType = fileType;
			return this;
		}
		public Builder auxType(int auxType) {
			as.prodosFileInfo.auxType = auxType;
			return this;
		}
		public Builder creationDate(int creation) {
			as.fileDatesInfo.creation = creation;
			return this;
		}
		public Builder creationDate(Instant creation) {
			as.fileDatesInfo.creation = FileDatesInfo.fromInstant(creation);
			return this;
		}
		public Builder modificationDate(int modification) {
			as.fileDatesInfo.modification = modification;
			return this;
		}
		public Builder modificationDate(Instant modification) {
			as.fileDatesInfo.modification = FileDatesInfo.fromInstant(modification);
			return this;
		}
		public Builder backupDate(int backup) {
			as.fileDatesInfo.backup = backup;
			return this;
		}
		public Builder backupDate(Instant backup) {
			as.fileDatesInfo.backup = FileDatesInfo.fromInstant(backup);
			return this;
		}
		public Builder accessDate(int access) {
			as.fileDatesInfo.access = access;
			return this;
		}
		public Builder accessDate(Instant access) {
			as.fileDatesInfo.access = FileDatesInfo.fromInstant(access);
			return this;
		}
		public Builder allDates(Instant instant) {
			return creationDate(instant).modificationDate(instant).backupDate(instant).accessDate(instant);
		}
		public AppleSingle build() {
			return as;
		}
	}
}
