package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Supports creation of AppleSingle archives. 
 */
@Command(name = "create", description = { "Create an AppleSingle file" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class CreateCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;
	
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
	
	@Option(names = "--filetype", description = "Set the ProDOS file type (also accepts BIN/BAS/SYS)", converter = ProdosFileTypeConverter.class)
	private Integer filetype;
	
	@Option(names = "--auxtype", description = "Set the ProDOS auxtype", converter = IntegerTypeConverter.class)
	private Integer auxtype;

	@Parameters(arity = "0..1", description = "AppleSingle file to create")
	private Path file;
	
	@Override
	public Void call() throws IOException {
		validateArguments();

		byte[] dataFork = prepDataFork();
		byte[] resourceFork = prepResourceFork();
		
		AppleSingle applesingle = buildAppleSingle(dataFork, resourceFork);
		writeAppleSingle(applesingle);
		
		return null;
	}
	
	public void validateArguments() throws IOException {
		if ((stdoutFlag && file != null) || (!stdoutFlag && file == null)) {
			throw new IOException("Please choose one of stdout or output file");
		}
		if ((dataForkFile != null && stdinForkType == ForkType.data) 
				|| (resourceForkFile != null && stdinForkType == ForkType.resource)) {
			throw new IOException("Stdin only supports one type of fork for input");
		}
		if (dataForkFile == null && resourceForkFile == null && stdinForkType == null) {
			throw new IOException("Please select at least one fork type");
		}
		if (stdinForkType == ForkType.both) {
			throw new IOException("Unable to read two forks from stdin");
		}
	}
	
	public byte[] prepDataFork() throws IOException {
		byte[] dataFork = null;
		if (stdinForkType == ForkType.data) {
			dataFork = AppleSingle.toByteArray(System.in);
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
			resourceFork = AppleSingle.toByteArray(System.in);
		} else if (resourceForkFile != null) {
			resourceFork = Files.readAllBytes(resourceForkFile);
		}
		return resourceFork;
	}
	
	public AppleSingle buildAppleSingle(byte[] dataFork, byte[] resourceFork) {
		AppleSingle.Builder builder = AppleSingle.builder();
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
