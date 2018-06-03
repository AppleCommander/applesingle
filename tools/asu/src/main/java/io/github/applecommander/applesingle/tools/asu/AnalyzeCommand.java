package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.applesingle.AppleSingleReader;
import io.github.applecommander.applesingle.Entry;
import io.github.applecommander.applesingle.EntryType;
import io.github.applecommander.applesingle.FileDatesInfo;
import io.github.applecommander.applesingle.ProdosFileInfo;
import io.github.applecommander.applesingle.Utilities;
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
		byte[] fileData = stdinFlag ? Utilities.toByteArray(System.in) : Files.readAllBytes(path);
		if (verboseFlag) this.verbose = System.out;
		
		List<IntRange> used = new ArrayList<>();
		HexDumper dumper = HexDumper.standard();
		AppleSingleReader reader = AppleSingleReader.builder()
				.data(fileData)
				.readAtReporter((start,len,b,d) -> used.add(IntRange.of(start, start+len)))
				.readAtReporter((start,len,chunk,desc) -> dumper.dump(start, chunk, desc))
				.versionReporter(this::reportVersion)
				.numberOfEntriesReporter(this::reportNumberOfEntries)
				.entryReporter(this::reportEntry)
				.build();
		AppleSingle.asEntries(reader);
		
		List<IntRange> ranges = IntRange.normalize(used);
		if (ranges.size() == 1 && ranges.get(0).getLow() == 0 && ranges.get(0).getHigh() == fileData.length) {
			verbose.printf("The entirety of the file was used.\n");
		} else {
			verbose.printf("Parts of the file were skipped!\n  - Expected: %s\n  - Actual:   %s\n", 
					Arrays.asList(IntRange.of(0,fileData.length)), ranges);
		}
		return null;
	}

	public void reportVersion(int version) {
		verbose.printf(" .. %s\n", VERSION_TEXT.getOrDefault(version, "Unrecognized version!"));
	}
	public void reportNumberOfEntries(int numberOfEntries) {
		verbose.printf(" .. Number of entries = %d\n", numberOfEntries);
	}
	public void reportEntry(Entry entry) {
		String entryName = EntryType.findNameOrUnknown(entry);
		verbose.printf(" .. Entry: entryId=%d (%s), offset=%d, length=%d\n", entry.getEntryId(), 
				entryName, entry.getOffset(), entry.getLength());
		REPORTERS.getOrDefault(entry.getEntryId(), this::reportDefaultEntry)
		         .accept(entry, entryName);
	}
	private void reportDefaultEntry(Entry entry, String entryName) {
		verbose.printf(" .. No further details for this entry type (%s).\n", entryName);
	}
	private void reportStringEntry(Entry entry, String entryName) {
		verbose.printf(" .. %s: '%s'\n", entryName, Utilities.entryToAsciiString(entry));
	}
	private void reportFileDatesInfoEntry(Entry entry, String entryName) {
		FileDatesInfo info = FileDatesInfo.fromEntry(entry);
		verbose.printf(" .. %s -\n", entryName);
		verbose.printf("           Creation: %s\n", info.getCreationInstant().toString());
		verbose.printf("       Modification: %s\n", info.getModificationInstant().toString());
		verbose.printf("             Backup: %s\n", info.getBackupInstant().toString());
		verbose.printf("             Access: %s\n", info.getAccessInstant().toString());
	}
	private void reportProdosFileInfoEntry(Entry entry, String entryName) {
		ProdosFileInfo info = ProdosFileInfo.fromEntry(entry);
		verbose.printf(" .. %s -\n", entryName);
		verbose.printf("             Access: %02X\n", info.getAccess());
		verbose.printf("          File Type: %04X\n", info.getFileType());
		verbose.printf("          Aux. Type: %04X\n", info.getAuxType());
	}
	
	private static final Map<Integer,String> VERSION_TEXT = new HashMap<Integer,String>() {
		private static final long serialVersionUID = 7142066556402030814L;
		{
			put(AppleSingle.VERSION_NUMBER1, "Version 1");
			put(AppleSingle.VERSION_NUMBER2, "Version 2");
		}
	};
	private final Map<Integer,BiConsumer<Entry,String>> REPORTERS = new HashMap<Integer,BiConsumer<Entry,String>>();
	{
		REPORTERS.put(EntryType.REAL_NAME.entryId, this::reportStringEntry);
		REPORTERS.put(EntryType.COMMENT.entryId, this::reportStringEntry);
		REPORTERS.put(EntryType.SHORT_NAME.entryId, this::reportStringEntry);
		REPORTERS.put(EntryType.FILE_DATES_INFO.entryId, this::reportFileDatesInfoEntry);
		REPORTERS.put(EntryType.PRODOS_FILE_INFO.entryId, this::reportProdosFileInfoEntry);
	}
}
