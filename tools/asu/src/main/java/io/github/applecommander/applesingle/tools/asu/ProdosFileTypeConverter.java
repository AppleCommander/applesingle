package io.github.applecommander.applesingle.tools.asu;

import java.util.HashMap;
import java.util.Map;

import picocli.CommandLine.ITypeConverter;

/** Add support for the more common ProDOS file type strings as well as integers. */
public class ProdosFileTypeConverter extends IntegerTypeConverter implements ITypeConverter<Integer> {
	private final Map<String,String> fileTypes = new HashMap<String,String>() {
		private static final long serialVersionUID = 1812781095833750521L;
		{
			put("TXT", "$04");
			put("BIN", "$06");
			put("INT", "$fa");
			put("BAS", "$fc");
			put("REL", "$fe");
			put("SYS", "$ff");
		}
	};

	@Override
	public Integer convert(String value) {
		// If we find the string in our map, swap it for the correct hex string.
		// Else just pass in the original value.
		return super.convert(fileTypes.getOrDefault(value.toUpperCase(), value));
	}
}
