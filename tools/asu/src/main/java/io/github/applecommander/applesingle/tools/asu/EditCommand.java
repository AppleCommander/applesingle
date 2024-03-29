package io.github.applecommander.applesingle.tools.asu;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.applesingle.Utilities;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Supports editing of AppleSingle archives.
 */
@Command(name = "edit", description = { "Edit an AppleSingle file" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		footerHeading = "%nNotes:%n",
		footer = { "* Dates should be supplied like '2007-12-03T10:15:30.00Z'.",
				   "* 'Known' ProDOS file types: TXT, BIN, INT, BAS, REL, SYS.",
				   "* Include the output file or specify stdout" },
		optionListHeading = "%nOptions:%n")
public class EditCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

	@Option(names = "--stdin", description = "Read AppleSingle file from stdin")
	private boolean stdinFlag;
	@Option(names = "--stdout", description = "Write AppleSingle file to stdout")
	private boolean stdoutFlag;

	@Option(names = "--stdin-fork", description = "Read fork from stdin (specify data or resource)")
	private ForkType stdinForkType;
	
	@Option(names = "--fix-text", description = "Set the high bit and fix line endings")
	private boolean fixTextFlag;

	@Option(names = "--data-fork", description = "Read data fork from file")
	private Path dataForkFile;

	@Option(names = "--resource-fork", description = "Read resource fork from file")
	private Path resourceForkFile;
	
	@Option(names = "--name", description = "Set the filename (defaults to name of data fork, if supplied)")
	private String realName;
	
	@Option(names = "--access", description = "Set the ProDOS access flags", converter = IntegerTypeConverter.class)
	private Integer access;
	
	@Option(names = "--filetype", description = "Set the ProDOS file type", converter = ProdosFileTypeConverter.class)
	private Integer filetype;
	
	@Option(names = "--auxtype", description = "Set the ProDOS auxtype", converter = IntegerTypeConverter.class)
	private Integer auxtype;
	
	@Option(names = "--creation-date", description = "Set the file creation date")
	private Instant creationDate;
	@Option(names = "--modification-date", description = "Set the file modification date")
	private Instant modificationDate;
	@Option(names = "--backup-date", description = "Set the file backup date")
	private Instant backupDate;
	@Option(names = "--access-date", description = "Set the file access date")
	private Instant accessDate;

	@Parameters(arity = "0..1", description = "AppleSingle file to modify")
	private Path file;
	
	@Override
	public Void call() throws IOException {
		validateArguments();

		AppleSingle original = stdinFlag ? AppleSingle.read(System.in) : AppleSingle.read(file);

		byte[] dataFork = prepDataFork();
		byte[] resourceFork = prepResourceFork();
		
		AppleSingle applesingle = buildAppleSingle(original, dataFork, resourceFork);
		writeAppleSingle(applesingle);
		
		return null;
	}
	
	public void validateArguments() throws IOException {
		if ((stdinFlag && file != null) || (!stdinFlag && file == null)) {
			throw new IOException("Please choose one of stdin or input file for original");
		}
		if ((dataForkFile != null && stdinForkType == ForkType.data) 
				|| (resourceForkFile != null && stdinForkType == ForkType.resource)) {
			throw new IOException("Stdin only supports one type of fork for input");
		}
		if (stdinForkType == ForkType.both) {
			throw new IOException("Unable to read two forks from stdin");
		}
	}
	
	public byte[] prepDataFork() throws IOException {
		byte[] dataFork = null;
		if (stdinForkType == ForkType.data) {
			dataFork = Utilities.toByteArray(System.in);
		} else if (dataForkFile != null) {
			dataFork = Files.readAllBytes(dataForkFile);
		}
		
		if (fixTextFlag && dataFork != null) {
			for (int i=0; i<dataFork.length; i++) {
				if (dataFork[i] == '\n') dataFork[i] = 0x0d;
				dataFork[i] = (byte)(dataFork[i] | 0x80);
			}
		}
		return dataFork;
	}
	
	public byte[] prepResourceFork() throws IOException {
		byte[] resourceFork = null;
		if (stdinForkType == ForkType.resource) {
			resourceFork = Utilities.toByteArray(System.in);
		} else if (resourceForkFile != null) {
			resourceFork = Files.readAllBytes(resourceForkFile);
		}
		return resourceFork;
	}
	
	public AppleSingle buildAppleSingle(AppleSingle original,
										byte[] dataFork,
										byte[] resourceFork) throws IOException {
		AppleSingle.Builder builder = AppleSingle.builder(original);
		if (realName != null) {
			builder.realName(realName);
		} else if (dataForkFile != null) {
			String name = dataForkFile.getFileName().toString();
			builder.realName(name);
		}
		if (access != null) builder.access(access.intValue());
		if (filetype != null) builder.fileType(filetype.intValue());
		if (auxtype != null) builder.auxType(auxtype.intValue());
		if (dataFork != null) builder.dataFork(dataFork);
		if (resourceFork != null) builder.resourceFork(resourceFork);
		
		if (dataForkFile != null || resourceForkFile != null) {
			Path path = Optional.ofNullable(dataForkFile).orElse(resourceForkFile);
			BasicFileAttributes attribs = Files.readAttributes(path, BasicFileAttributes.class);
			builder.creationDate(attribs.creationTime().toInstant());
			builder.modificationDate(attribs.lastModifiedTime().toInstant());
			builder.accessDate(attribs.lastAccessTime().toInstant());
		}
		if (creationDate != null) builder.creationDate(creationDate);
		if (modificationDate != null) builder.modificationDate(modificationDate);
		if (backupDate != null) builder.backupDate(backupDate);
		if (accessDate != null) builder.accessDate(accessDate);
		
		return builder.build();
	}
	
	public void writeAppleSingle(AppleSingle applesingle) throws IOException {
		if (stdoutFlag) {
			applesingle.save(System.out);
		} else {
			applesingle.save(file);
			System.out.printf("Saved to '%s'.\n", file);
		}
	}
}
