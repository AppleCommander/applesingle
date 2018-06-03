package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.applesingle.Entry;
import io.github.applecommander.applesingle.EntryType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Allow filtering of an an AppleSingle archive.
 * Both source and target can be a file or stream. 
 */
@Command(name = "filter", description = { "Filter an AppleSingle file",
			"Please include a file name or indicate stdin should be read, but not both." },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class FilterCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

	@Option(names = "--stdin", description = "Read AppleSingle from stdin.")
	private boolean stdinFlag;
	
	@Option(names = "--stdout", description = "Write AppleSingle to stdout.")
	private boolean stdoutFlag;
	
	@Option(names = { "-o", "--output" }, description = "Write AppleSingle to file.")
	private Path outputFile;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path inputFile;
	
	@Option(names = "--prodos", description = "Apply ProDOS specific filter")
	private boolean prodosFlag;
	@Option(names = { "--mac", "--macintosh" }, description = "Apply Macintosh specific filter")
	private boolean macintoshFlag;
	@Option(names = "--msdos", description = "Apply MS-DOS specific filter")
	private boolean msdosFlag;
	@Option(names = "--afp", description = "Apply AFP specific filter")
	private boolean afpFlag;
	
	@Option(names = "--include", description = "Filter by including specific entryIds", split = ",")
	private Integer[] includeEntryIds;

	@Option(names = "--exclude", description = "Filter by excluding specific entryIds", split = ",")
	private Integer[] excludeEntryIds;
	
	@Override
	public Void call() throws IOException {
		try (PrintStream ps = this.stdoutFlag ? new PrintStream(NullOutputStream.INSTANCE) : System.out) {
			OSFilter osFilter = validate();
			
			SortedSet<Integer> included = toSet(includeEntryIds, osFilter);
			SortedSet<Integer> excluded = toSet(excludeEntryIds, null);
			List<Entry> entries = stdinFlag ? AppleSingle.asEntries(System.in) : AppleSingle.asEntries(inputFile);
			List<Entry> newEntries = entries.stream()
					.filter(e -> included.isEmpty() || included.contains(e.getEntryId()))
					.filter(e -> excluded.isEmpty() || !excluded.contains(e.getEntryId()))
					.collect(Collectors.toList());
			// Check if we ended up with different things
			SortedSet<EntryType> before = toEntryType(entries);
			SortedSet<EntryType> after = toEntryType(newEntries);
			before.removeAll(after);	// Note: modifies before
			if (!before.isEmpty()) {
				ps.printf("Removed the following entries:\n");
				before.forEach(e -> ps.printf("- %s\n", e.name));
			} else {
				ps.printf("No entries removed.\n");
			}
			
			OutputStream outputStream = stdoutFlag ? System.out : Files.newOutputStream(outputFile);
			AppleSingle.write(outputStream, newEntries);
		}
		return null;
	}
	private OSFilter validate() throws IOException {
		long count = Stream.of(prodosFlag, macintoshFlag, msdosFlag, afpFlag).filter(flag -> flag).count();
		// Expected boundaries
		if (count == 0) return null;
		if (count > 1) throw new IOException("Please choose only one operating system flag!");
		// Set the correct OS Flag
		if (prodosFlag) return OSFilter.PRODOS;
		if (macintoshFlag) return OSFilter.MACINTOSH;
		if (msdosFlag) return OSFilter.MS_DOS;
		if (afpFlag) return OSFilter.AFP;
		// Not a clue how you can get here...
		throw new IOException("Bug! Please put in a ticket or a pull request. Thanks! :-)");
	}
	private SortedSet<Integer> toSet(Integer[] entryIds, OSFilter filter) {
		SortedSet<Integer> set = new TreeSet<>();
		Optional.ofNullable(entryIds)
				.map(a -> Arrays.asList(a))
				.ifPresent(set::addAll);
		Optional.ofNullable(filter)
				.map(f -> f.types)
				.ifPresent(t -> Stream.of(t)
						              .map(e -> e.entryId)
						              .collect(Collectors.toCollection(() -> set)));
		return set;
	}
	private SortedSet<EntryType> toEntryType(Collection<Entry> entries) {
		return entries.stream()
				.map(e -> e.getEntryId())
				.map(EntryType::find)
				.collect(Collectors.toCollection(() -> new TreeSet<EntryType>()));
	}
	
	public enum OSFilter {
		PRODOS(EntryType.DATA_FORK, EntryType.RESOURCE_FORK, EntryType.REAL_NAME, EntryType.FILE_DATES_INFO, 
				EntryType.PRODOS_FILE_INFO), 
		MACINTOSH(EntryType.DATA_FORK, EntryType.RESOURCE_FORK, EntryType.REAL_NAME, EntryType.COMMENT, 
				EntryType.ICON_BW, EntryType.ICON_COLOR, EntryType.FILE_DATES_INFO, EntryType.FINDER_INFO, 
				EntryType.MACINTOSH_FILE_INFO), 
		MS_DOS(EntryType.DATA_FORK, EntryType.REAL_NAME, EntryType.FILE_DATES_INFO, EntryType.MSDOS_FILE_INFO), 
		AFP(EntryType.DATA_FORK, EntryType.REAL_NAME, EntryType.FILE_DATES_INFO, EntryType.SHORT_NAME, 
				EntryType.AFP_FILE_INFO, EntryType.DIRECTORY_ID);
		
		public final EntryType[] types;
		private OSFilter(EntryType... types) {
			this.types = types;
		}
	}
}
