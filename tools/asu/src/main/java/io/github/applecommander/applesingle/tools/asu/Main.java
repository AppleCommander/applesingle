package io.github.applecommander.applesingle.tools.asu;

import java.util.Optional;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

/**
 * Primary entry point into the AppleSingle utility. 
 */
@Command(name = "asu", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
	descriptionHeading = "%n",
	commandListHeading = "%nCommands:%n",
	optionListHeading = "%nOptions:%n",
	description = "AppleSingle utility", 
	subcommands = { 
			AnalyzeCommand.class, 
			CreateCommand.class,
			EditCommand.class,
			ExtractCommand.class,
			FilterCommand.class,
			HelpCommand.class, 
			InfoCommand.class, 
			})
public class Main implements Runnable {
	@Option(names = "--debug", description = "Dump full stack traces if an error occurs")
	private static boolean debugFlag;
	
	public static void main(String[] args) {
		try {
			new CommandLine(new Main()).execute(args);
		} catch (Throwable t) {
			if (Main.debugFlag) {
				t.printStackTrace(System.err);
			} else {
				String message = t.getMessage();
				while (t != null) {
					message = t.getMessage();
					t = t.getCause();
				}
				System.err.printf("Error: %s\n", Optional.ofNullable(message).orElse("An error occurred."));
			}
			System.exit(1);
		}
	}
	
	@Override
	public void run() {
		CommandLine.usage(this, System.out);
	}
}
