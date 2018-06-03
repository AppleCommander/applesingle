package io.github.applecommander.applesingle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Consumer;

public final class AppleSingleReader {
	private byte[] data;
	private int pos = 0;
	private Consumer<Integer> versionReporter = v -> {};
	private Consumer<Integer> numberOfEntriesReporter = n -> {};
	private Consumer<Entry> entryReporter = e -> {};
	private ReadAtReporter readAtReporter = (s,l,b,d) -> {};
	
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
		readAtReporter.accept(start, len, chunk, description);
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

	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private AppleSingleReader reader = new AppleSingleReader();
		private Builder() { 
			// Prevent construction 
		}
		public Builder data(byte[] data) {
			Objects.requireNonNull(data);
			reader.data = data;
			return this;
		}
		public Builder versionReporter(Consumer<Integer> consumer) {
			Objects.requireNonNull(consumer);
			reader.versionReporter = reader.versionReporter.andThen(consumer);
			return this;
		}
		public Builder numberOfEntriesReporter(Consumer<Integer> consumer) {
			Objects.requireNonNull(consumer);
			reader.numberOfEntriesReporter = reader.numberOfEntriesReporter.andThen(consumer);
			return this;
		}
		public Builder entryReporter(Consumer<Entry> consumer) {
			Objects.requireNonNull(consumer);
			reader.entryReporter = reader.entryReporter.andThen(consumer);
			return this;
		}
		public Builder readAtReporter(ReadAtReporter consumer) {
			Objects.requireNonNull(consumer);
			reader.readAtReporter = reader.readAtReporter.andThen(consumer);
			return this;
		}
		public AppleSingleReader build() {
			Objects.requireNonNull(reader.data, "You must supply a byte[] of data");
			return reader;
		}
	}

	/** 
	 * A reporter for the {@code AppleSingleReader#readAt(int, int, String)} method, 
	 * heaviliy modeled on the {@code Consumer} interface.
	 */
	public interface ReadAtReporter {
		public void accept(int start, int len, byte[] data, String description);
	    default ReadAtReporter andThen(ReadAtReporter after) {
	        Objects.requireNonNull(after);
	        return (s,l,b,d) -> { accept(s,l,b,d); after.accept(s,l,b,d); };
	    }
	}
}