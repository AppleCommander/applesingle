package io.github.applecommander.applesingle;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

public class AppleSingleReaderTest {
	@Test(expected = NullPointerException.class)
	public void testDoesNotAcceptNull() {
		AppleSingleReader.builder(null);
	}
	
	@Test
	public void testReporters() throws IOException {
		Ticker versionCalled = new Ticker();
		Ticker numberOfEntriesCalled = new Ticker();
		Ticker entryReporterCalled = new Ticker();
		Ticker readAtCalled = new Ticker();
		// Intentionally calling ticker 2x to ensure events do get chained
		AppleSingleReader r = AppleSingleReader.builder(SAMPLE_FILE)
				.versionReporter(v -> versionCalled.tick())
				.versionReporter(v -> assertEquals(AppleSingle.VERSION_NUMBER2, v.intValue()))
				.versionReporter(v -> versionCalled.tick())
				.numberOfEntriesReporter(n -> numberOfEntriesCalled.tick())
				.numberOfEntriesReporter(n -> assertEquals(1, n.intValue()))
				.numberOfEntriesReporter(n -> numberOfEntriesCalled.tick())
				.readAtReporter((o,b,d) -> readAtCalled.tick())
				.readAtReporter((o,b,d) -> readAtCalled.tick())
				.entryReporter(e -> entryReporterCalled.tick())
				.entryReporter(e -> assertEquals("Hello, World!\n", new String(e.getData())))
				.entryReporter(e -> assertEquals(e.getEntryId(), EntryType.DATA_FORK.entryId))
				.entryReporter(e -> entryReporterCalled.tick())
				.build();
		// Executes on the reader
		AppleSingle.asEntries(r);
		// Validate
		assertEquals(2, versionCalled.count());
		assertEquals(2, numberOfEntriesCalled.count());
		assertEquals(2, entryReporterCalled.count());
		assertTrue(readAtCalled.count() >= 2);
	}
	
	/**
	 * AppleSingle file with a simple Data Fork and nothing else.
	 * <br/>
	 * <code>
	 * $ echo "Hello, World!" | asu create --stdin-fork=data --filetype=txt --stdout | asu filter --include=1 --stdin --stdout | hexdump -C
	 * 00000000  00 05 16 00 00 02 00 00  00 00 00 00 00 00 00 00  |................|
	 * 00000010  00 00 00 00 00 00 00 00  00 01 00 00 00 01 00 00  |................|
	 * 00000020  00 26 00 00 00 0e 48 65  6c 6c 6f 2c 20 57 6f 72  |.&....Hello, Wor|
	 * 00000030  6c 64 21 0a                                       |ld!.|
	 * </code>
	 */
	public static final byte[] SAMPLE_FILE = {
		0x00, 0x05, 0x16, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,
		0x00, 0x26, 0x00, 0x00, 0x00, 0x0e, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x20, 0x57, 0x6f, 0x72,
		0x6c, 0x64, 0x21, 0x0a
	};
	
	public static class Ticker {
		private int count;
		
		public void tick() {
			this.count++;
		}
		public int count() {
			return this.count;
		}
	}
}
