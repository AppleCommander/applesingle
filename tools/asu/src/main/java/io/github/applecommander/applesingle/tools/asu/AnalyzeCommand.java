package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.applesingle.FileDatesInfo;
import io.github.applecommander.applesingle.ProdosFileInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Perform a bit of analysis of an AppleSingle archive to help extend the library, fix bugs,
 * or understand incompatibilities. 
 */
@Command(name = "analyze", description = { "Perform an analysis on an AppleSingle file",
			"Please include a file name or indicate stdin should be read, but not both." },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class AnalyzeCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

	@Option(names = "--stdin", description = "Read AppleSingle from stdin.")
	private boolean stdinFlag;
	
	@Option(names = { "-v", "--verbose" }, description = "Be verbose.")
	private boolean verboseFlag;
	private PrintStream verbose = new PrintStream(NullOutputStream.INSTANCE);
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path path;
	
	@Override
	public Void call() throws IOException {
		byte[] fileData = stdinFlag ? AppleSingle.toByteArray(System.in) : Files.readAllBytes(path);
		if (verboseFlag) this.verbose = System.out;
		
		State state = new State(fileData);
		match(state, "Magic number", "Not an AppleSingle file - magic number does not match.", 
				AppleSingle.MAGIC_NUMBER);
		int version = match(state, "Version", "Only recognize AppleSingle versions 1 and 2.", 
				AppleSingle.VERSION_NUMBER1, AppleSingle.VERSION_NUMBER2);
		verbose.printf(" .. Version 0x%08x\n", version);
		state.read(16, "Filler");
		int numberOfEntries = state.read(Short.BYTES, "Number of entries").getShort();
		verbose.printf(" .. Entries = %d\n", numberOfEntries);
		List<Entry> entries = new ArrayList<>();
		for (int i = 0; i < numberOfEntries; i++) {
			ByteBuffer buffer = state.read(12, String.format("Entry #%d", i+1));
			Entry entry = new Entry(i+1, buffer);
			entry.print(verbose);
			entries.add(entry);
		}
		entries.sort((a,b) -> Integer.compare(a.offset, b.offset));
		for (Entry entry : entries) entryReport(state, entry);
		
		List<IntRange> ranges = IntRange.normalize(state.used);
		if (ranges.size() == 1 && ranges.get(0).getLow() == 0 && ranges.get(0).getHigh() == fileData.length) {
			verbose.printf("The entirety of the file was used.\n");
		} else {
			verbose.printf("Parts of the file were skipped!\n  - Expected: %s\n  - Actual:   %s\n", 
					Arrays.asList(IntRange.of(0,fileData.length)), ranges);
		}
		return null;
	}
	
	public int match(State state, String description, String message, int... expecteds) throws IOException {
		ByteBuffer buffer = state.read(Integer.BYTES, description);
		int actual = buffer.getInt();
		for (int expected : expecteds) {
			if (actual == expected) return actual;
		}
		throw new IOException(String.format("%s  Aborting.", message));
	}
	
	public void entryReport(State state, Entry entry) throws IOException {
		String entryName = AppleSingle.ENTRY_TYPE_NAMES.getOrDefault(entry.entryId, "Unknown");
		ByteBuffer buffer = state.readAt(entry.offset, entry.length, 
				String.format("Entry #%d data (%s)", entry.index, entryName));
		switch (entry.entryId) {
		case 3:
		case 4:
		case 13:
			displayEntryString(buffer, entryName);
			break;
		case 8:
			displayFileDatesInfo(buffer, entryName);
			break;
		case 11:
			displayProdosFileInfo(buffer, entryName);
			break;
		default:
			verbose.printf(" .. No further details for this entry type (%s).\n", entryName);
			break;
		}
	}
	public void displayEntryString(ByteBuffer buffer, String entryName) {
		StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining()) {
			int ch = Byte.toUnsignedInt(buffer.get()) & 0x7f;
			sb.append((char)ch);
		}
		verbose.printf(" .. %s: '%s'\n", entryName, sb.toString());
	}
	public void displayFileDatesInfo(ByteBuffer buffer, String entryName) {
		FileDatesInfo info = new FileDatesInfo(buffer.getInt(), buffer.getInt(), buffer.getInt(), buffer.getInt());
		verbose.printf(" .. %s -\n", entryName);
		verbose.printf("           Creation: %s\n", info.getCreationInstant().toString());
		verbose.printf("       Modification: %s\n", info.getModificationInstant().toString());
		verbose.printf("             Backup: %s\n", info.getBackupInstant().toString());
		verbose.printf("             Access: %s\n", info.getAccessInstant().toString());
	}
	public void displayProdosFileInfo(ByteBuffer buffer, String entryName) {
		ProdosFileInfo info = new ProdosFileInfo(buffer.getShort(), buffer.getShort(), buffer.getInt());
		verbose.printf(" .. %s -\n", entryName);
		verbose.printf("             Access: %02X\n", info.getAccess());
		verbose.printf("          File Type: %04X\n", info.getFileType());
		verbose.printf("          Aux. Type: %04X\n", info.getAuxType());
	}
	
	public static class State {
		private final byte[] data;
		private int pos = 0;
		private List<IntRange> used = new ArrayList<>();
		private HexDumper dumper = HexDumper.standard();
		
		public State(byte[] data) {
			this.data = data;
		}
		public ByteBuffer read(int len, String description) throws IOException {
			return readAt(pos, len, description);
		}
		public ByteBuffer readAt(int start, int len, String description) throws IOException {
			byte[] chunk = new byte[len];
			System.arraycopy(data, start, chunk, 0, len);
			ByteBuffer buffer = ByteBuffer.wrap(chunk)
					.order(ByteOrder.BIG_ENDIAN)
					.asReadOnlyBuffer();
			dumper.dump(start, chunk, description);
			used.add(IntRange.of(start, start+len));
			pos= start+len;
			return buffer;
		}
	}
	public static class Entry {
		private int index;
		private int entryId;
		private int offset;
		private int length;
		public Entry(int index, ByteBuffer buffer) {
			this.index = index;
			this.entryId = buffer.getInt();
			this.offset = buffer.getInt();
			this.length = buffer.getInt();
		}
		public void print(PrintStream ps) {
			ps.printf(" .. Entry #%d, entryId=%d (%s), offset=%d, length=%d\n", index, entryId, 
					AppleSingle.ENTRY_TYPE_NAMES.getOrDefault(entryId, "Unknown"), offset, length);
		}
	}
}
