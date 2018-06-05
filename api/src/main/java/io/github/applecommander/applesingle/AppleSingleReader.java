package io.github.applecommander.applesingle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * The AppleSingleReader is a component that allows tools to react to processing that 
 * goes on when an AppleSingle file is being read.  The {@code Builder} allows multiple
 * {@code Consumer}'s and {@code ReadAtReporter}'s to be defined. 
 */
public final class AppleSingleReader {
	private AppleSingleReader() { /* Prevent construction */ }
	
	private byte[] data;
	private int pos = 0;
	private Consumer<Integer> versionReporter = v -> {};
	private Consumer<Integer> numberOfEntriesReporter = n -> {};
	private Consumer<Entry> entryReporter = e -> {};
	private ReadAtReporter readAtReporter = (s,b,d) -> {};
	
	public ByteBuffer read(int len, String description) {
		try {
			return readAt(pos, len, description);
		} finally {
			pos += len;
		}
	}
	public ByteBuffer readAt(int start, int len, String description) {
		byte[] chunk = new byte[len];
		System.arraycopy(data, start, chunk, 0, len);
		readAtReporter.accept(start, chunk, description);
		ByteBuffer buffer = ByteBuffer.wrap(chunk)
				.order(ByteOrder.BIG_ENDIAN);
		return buffer;
	}
	public void reportVersion(int version) {
		versionReporter.accept(version);
	}
	public void reportNumberOfEntries(int numberOfEntries) {
		numberOfEntriesReporter.accept(numberOfEntries);
	}
	public void reportEntry(Entry entry) {
		entryReporter.accept(entry);
	}

	/** Create a {@code Builder} for an {@code AppleSingleReader}. */
	public static Builder builder(byte[] data) {
		return new Builder(data);
	}
	public static class Builder {
		private AppleSingleReader reader = new AppleSingleReader();
		private Builder(byte[] data) {
			Objects.requireNonNull(data, "You must supply a byte[] of data");
			reader.data = data;
		}
		/** Add a version reporter.  Note that multiple can be added. */
		public Builder versionReporter(Consumer<Integer> consumer) {
			Objects.requireNonNull(consumer);
			reader.versionReporter = reader.versionReporter.andThen(consumer);
			return this;
		}
		/** Add a number of entries reporter.  Note that multiple can be added. */
		public Builder numberOfEntriesReporter(Consumer<Integer> consumer) {
			Objects.requireNonNull(consumer);
			reader.numberOfEntriesReporter = reader.numberOfEntriesReporter.andThen(consumer);
			return this;
		}
		/** Add an entry reporter.  Note that multiple can be added. */
		public Builder entryReporter(Consumer<Entry> consumer) {
			Objects.requireNonNull(consumer);
			reader.entryReporter = reader.entryReporter.andThen(consumer);
			return this;
		}
		/** Add a read at reporter.  Note that multiple can be added. */
		public Builder readAtReporter(ReadAtReporter consumer) {
			Objects.requireNonNull(consumer);
			reader.readAtReporter = reader.readAtReporter.andThen(consumer);
			return this;
		}
		public AppleSingleReader build() {
			return reader;
		}
	}

	/** 
	 * A reporter for the {@code AppleSingleReader#readAt(int, int, String)} method, 
	 * heavily modeled on the {@code Consumer} interface.
	 */
	public interface ReadAtReporter {
	    /**
	     * Performs this operation on the given arguments.
	     * 
	     * @param start the offset into the file
	     * @param data the specific data being processed
	     * @param description descriptive text regarding the data
	     */
		public void accept(int start, byte[] data, String description);

		/**
	     * Returns a composed {@code ReadAtReporter} that performs, in sequence, this
	     * operation followed by the {@code after} operation. If performing either
	     * operation throws an exception, it is relayed to the caller of the
	     * composed operation.  If performing this operation throws an exception,
	     * the {@code after} operation will not be performed.
	     *
	     * @param after the operation to perform after this operation
	     * @return a composed {@code ReadAtReporter} that performs in sequence this
	     * operation followed by the {@code after} operation
	     * @throws NullPointerException if {@code after} is null
	     */
	    public default ReadAtReporter andThen(ReadAtReporter after) {
	        Objects.requireNonNull(after);
	        return (s,b,d) -> { accept(s,b,d); after.accept(s,b,d); };
	    }
	}
}