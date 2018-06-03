package io.github.applecommander.applesingle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utilities {
	private Utilities() { /* Prevent construction */ }
	
	/** Utility method to read all bytes from an InputStream. */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (true) {
			byte[] buf = new byte[1024];
			int len = inputStream.read(buf);
			if (len == -1) break;
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		return outputStream.toByteArray();
	}

	/** Convert bytes in an Entry to a 7-bit ASCII string.  Emphasis on 7-bit in case Apple II high bit is along for the ride. */
	public static String entryToAsciiString(Entry entry) {
		byte[] data = entry.getData();
		for (int i=0; i<data.length; i++) {
			data[i] = (byte)(data[i] & 0x7f);
		}
		return new String(data);
	}
}
