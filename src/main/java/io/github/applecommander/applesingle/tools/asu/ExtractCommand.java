package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Supports extracting components of an AppleSingle archive. 
 */
@Command(name = "extract", description = { "Extract contents of an AppleSingle file" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class ExtractCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;
	
	@Option(names = "--stdout", description = "Write selected fork to stdout")
	private boolean stdoutFlag;

	@Option(names = "--stdin", description = "Read AppleSingle from stdin")
	private boolean stdinFlag;
	
	@Option(names = "--fix-text", description = "Clear the high bit and fix line endings")
	private boolean fixTextFlag;

	@Option(names = "--fork", description = "Extract which fork type (specify data, resource, or both", 
			showDefaultValue = Visibility.ALWAYS)
	private ForkType forkType = ForkType.data;
	
	@Option(names = { "-o", "--output" }, description = "Write fork(s) to base filename")
	private String baseFilename;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path file;
	
	@Override
	public Void call() throws IOException {
		validateArguments();
		
		AppleSingle applesingle = stdinFlag ? AppleSingle.read(System.in) : AppleSingle.read(file);
		if (!stdoutFlag && baseFilename == null && applesingle.getRealName() == null) {
			throw new IOException("Please include an output base filename; this AppleSingle file does not contain a name");
		}
		if (baseFilename == null) {
			baseFilename = applesingle.getRealName();
		}
		
		writeFork(ForkType.data, applesingle.getDataFork());
		writeFork(ForkType.resource, applesingle.getResourceFork());
		return null;
	}
	
	public void validateArguments() throws IOException {
		if (stdoutFlag && baseFilename != null) {
			throw new IOException("Please choose one of stdout or output file");
		}
		if (stdoutFlag && forkType == ForkType.both) {
			throw new IOException("Stdout only supports one type of fork for output");
		}
		if ((stdinFlag && file != null) || (!stdinFlag && file == null)) {
			throw new IOException("Please select ONE of stdin or file");
		}
	}
	
	public void writeFork(ForkType forkType, byte[] data) throws IOException {
		if (this.forkType != forkType && this.forkType != ForkType.both) return;
		
		if (data == null || data.length == 0) {
			throw new IOException(String.format("There is no data in the %s fork, aborting", forkType));
		}
		
		if (fixTextFlag) {
			for (int i=0; i<data.length; i++) {
				data[i] = (byte)(data[i] & 0x7f);
				if (data[i] == 0x0d) data[i] = '\n';
			}
		}

		if (baseFilename != null) {
			String targetFilename = String.format("%s.%s", baseFilename, forkType.name());
			System.out.printf("Writing %s fork to file '%s'...\n", forkType.name(), targetFilename);
			Path path = Paths.get(targetFilename);
			Files.write(path, data);
		}
		if (stdoutFlag) {
			System.out.write(data);
		}
	}
}
