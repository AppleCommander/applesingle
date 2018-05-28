package io.github.applecommander.applesingle.tools.asu;

import java.io.IOException;
import java.io.OutputStream;

/** An OutputStream that doesn't do output. */
public class NullOutputStream extends OutputStream {
	public static final NullOutputStream INSTANCE = new NullOutputStream();

	private NullOutputStream() { /* Prevent construction */	}
	
	@Override
	public void write(int b) throws IOException {
		// Do Nothing
	}
}
