package io.github.applecommander.applesingle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
	public static final int VERSION_NUMBER = 0x00020000;

	private Map<Integer,Consumer<byte[]>> entryConsumers = new HashMap<>();
	{
		entryConsumers.put(1, this::setDataFork);
		entryConsumers.put(2, this::setResourceFork);
		entryConsumers.put(3, this::setRealName);
		entryConsumers.put(11, this::setProdosFileInfo);
	}
	
	private byte[] dataFork;
	private byte[] resourceFork;
	private String realName;
	private ProdosFileInfo prodosFileInfo = ProdosFileInfo.standardBIN();

	private AppleSingle() {
		// Allow Builder construction
	}
	private AppleSingle(byte[] data) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(data)
				.order(ByteOrder.BIG_ENDIAN)
				.asReadOnlyBuffer();
		required(buffer, MAGIC_NUMBER, "Not an AppleSingle file - magic number does not match.");
		required(buffer, VERSION_NUMBER, "Only AppleSingle version 2 supported.");
		buffer.position(buffer.position() + 16);	// Skip filler
		int entries = buffer.getShort();
		for (int i = 0; i < entries; i++) {
			int entryId = buffer.getInt();
			int offset = buffer.getInt();
			int length = buffer.getInt();
			buffer.mark();
			buffer.position(offset);
			byte[] entryData = new byte[length];
			buffer.get(entryData);
			// Defer to the proper set method or crash if we don't support that type of entry
			Optional.of(entryConsumers.get(entryId))
				.orElseThrow(() -> new IOException(String.format("Unknown entry type of %04X", entryId)))
				.accept(entryData);
			buffer.reset();
		}
	}
	private void required(ByteBuffer buffer, int expected, String message) throws IOException {
		int actual = buffer.getInt();
		if (actual != expected) {
			throw new IOException(String.format("%s  Expected 0x%08x but read 0x%08x.", message, expected, actual));
		}
	}
	private void setDataFork(byte[] entryData) {
		this.dataFork = entryData;
	}
	private void setResourceFork(byte[] entryData) {
		this.resourceFork = entryData;
	}
	private void setRealName(byte[] entryData) {
		for (int i=0; i<entryData.length; i++) {
			entryData[i] = (byte)(entryData[i] & 0x7f);
		}
		this.realName = new String(entryData);
	}
	private void setProdosFileInfo(byte[] entryData) {
		ByteBuffer infoData = ByteBuffer.wrap(entryData)
				.order(ByteOrder.BIG_ENDIAN)
				.asReadOnlyBuffer();
		int access = infoData.getShort();
		int fileType = infoData.getShort();
		int auxType = infoData.getInt();
		this.prodosFileInfo = new ProdosFileInfo(access, fileType, auxType);
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
	
	public void save(OutputStream outputStream) throws IOException {
		// Support real name, prodos file info, resource fork (if present), data fork (assumed)
		final boolean hasResourceFork = resourceFork == null ? false : true;
		final int entries = 3 + (hasResourceFork ? 1 : 0);

		int realNameOffset = 26 + (12 * entries);;
		int prodosFileInfoOffset = realNameOffset + realName.length();
		int resourceForkOffset = prodosFileInfoOffset + 8;
		int dataForkOffset = resourceForkOffset + (hasResourceFork ? resourceFork.length : 0);
		
		writeFileHeader(outputStream);
		writeHeader(outputStream, 3, realNameOffset, realName.length());
		writeHeader(outputStream, 11, prodosFileInfoOffset, 8);
		if (hasResourceFork) writeHeader(outputStream, 2, resourceForkOffset, resourceFork.length);
		writeHeader(outputStream, 1, dataForkOffset, dataFork.length);
		
		writeRealName(outputStream);
		writeProdosFileInfo(outputStream);
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
	
	private void writeFileHeader(OutputStream outputStream) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(24).order(ByteOrder.BIG_ENDIAN);
		buf.putLong(MAGIC_NUMBER);
		buf.putLong(VERSION_NUMBER);
		outputStream.write(buf.array());
	}
	private void writeHeader(OutputStream outputStream, int entryId, int offset, int length) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.BIG_ENDIAN);
		buf.putLong(entryId);
		buf.putLong(offset);
		buf.putLong(length);
		outputStream.write(buf.array());
	}
	private void writeRealName(OutputStream outputStream) throws IOException {
		outputStream.write(realName.getBytes());
	}
	private void writeProdosFileInfo(OutputStream outputStream) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
		buf.putShort((short)prodosFileInfo.access);
		buf.putShort((short)prodosFileInfo.fileType);
		buf.putLong(prodosFileInfo.fileType);
		outputStream.write(buf.array());
	}
	private void writeResourceFork(OutputStream outputStream) throws IOException {
		outputStream.write(resourceFork);
	}
	private void writeDataFork(OutputStream outputStream) throws IOException {
		outputStream.write(dataFork);
	}

	public static AppleSingle read(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (true) {
			byte[] buf = new byte[1024];
			int len = inputStream.read(buf);
			if (len == -1) break;
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		return read(outputStream.toByteArray());
	}
	public static AppleSingle read(File file) throws IOException {
		Objects.requireNonNull(file);
		return read(file.toPath());
	}
	public static AppleSingle read(Path path) throws IOException {
		Objects.requireNonNull(path);
		return new AppleSingle(Files.readAllBytes(path));
	}
	public static AppleSingle read(byte[] data) throws IOException {
		Objects.requireNonNull(data);
		return new AppleSingle(data);
	}
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private AppleSingle as = new AppleSingle();
		public Builder realName(String realName) {
			as.realName = realName;
			return this;
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
		public AppleSingle build() {
			return as;
		}
	}
}
