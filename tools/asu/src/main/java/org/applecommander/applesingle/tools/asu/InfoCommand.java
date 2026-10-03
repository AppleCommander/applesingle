/*
 * AppleSingle - A Java library and command-line tool for AppleSingle support.
 * Copyright (C) 2026  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.applecommander.applesingle.tools.asu;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.applecommander.applesingle.AppleSingle;
import org.applecommander.applesingle.FileDatesInfo;
import org.applecommander.applesingle.ProdosFileInfo;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Display basic information from an AppleSingle archive. 
 */
@Command(name = "info", description = { "Display information about an AppleSingle file",
			"Please include a file name or indicate stdin should be read, but not both." },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class InfoCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

	@Option(names = "--stdin", description = "Read AppleSingle from stdin.")
	private boolean stdinFlag;
	
	@Parameters(arity = "0..1", description = "File to process")
	private File file;
	
	@Override
	public Void call() throws IOException {
		AppleSingle applesingle = stdinFlag ? AppleSingle.read(System.in) : AppleSingle.read(file);
		
		System.out.printf("Real Name: %s\n", Optional.ofNullable(applesingle.getRealName()).orElse("-Unknown-"));
		
		System.out.printf("ProDOS info:\n");
		if (applesingle.getProdosFileInfo() == null) {
			System.out.println("  Not supplied.");
		} else {
			ProdosFileInfo prodosFileInfo = applesingle.getProdosFileInfo();
			System.out.printf("  Access: 0x%02X\n", prodosFileInfo.getAccess());
			System.out.printf("  File Type: 0x%02X\n", prodosFileInfo.getFileType());
			System.out.printf("  Auxtype: 0x%04X\n", prodosFileInfo.getAuxType());
		}
		
		System.out.printf("File dates info:\n");
		if (applesingle.getFileDatesInfo() == null) {
			System.out.println("  Not supplied.");
		} else {
			FileDatesInfo fileDatesInfo = applesingle.getFileDatesInfo();
			System.out.printf("  Creation: %s\n", fileDatesInfo.getCreationInstant());
			System.out.printf("  Modification: %s\n", fileDatesInfo.getModificationInstant());
			System.out.printf("  Access: %s\n", fileDatesInfo.getAccessInstant());
			System.out.printf("  Backup: %s\n", fileDatesInfo.getBackupInstant());
		}
		
		System.out.printf("Data Fork: Present, %,d bytes\n", applesingle.getDataFork().length);
		
		System.out.printf("Resource Fork: %s\n", 
				Optional.ofNullable(applesingle.getResourceFork())
					.map(d -> String.format("Present, %,d bytes", d.length))
					.orElse("Not present"));
		return null;
	}
}
