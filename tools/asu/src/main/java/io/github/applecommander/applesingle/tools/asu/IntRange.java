package io.github.applecommander.applesingle.tools.asu;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * A basic integer range used to track file usage.
 * <code>low</code> is inclusive while <code>high</code> is exclusive, because it made
 * the code "simpler".
 * 
 * @author rob
 */
public class IntRange {
	private int low;
	private int high;

	/** Create an integer range. */
	public static IntRange of(int low, int high) {
		if (low == high) throw new UnsupportedOperationException("low and high cannot be the same");
		return new IntRange(Math.min(low,high), Math.max(low,high));
	}
	/** Normalize a list by combining all integer ranges that match. */
	public static List<IntRange> normalize(List<IntRange> ranges) {
		Stack<IntRange> rangeStack = new Stack<>();
		ranges.stream()
				  .sorted((a,b) -> Integer.compare(a.low, b.low))
				  .forEach(r -> {
					  if (rangeStack.isEmpty()) {
						  rangeStack.add(r);
					  } else {
						  rangeStack.peek()
						  			.merge(r)
						  			.ifPresent(ranges::add);
					  }
				  });
		return rangeStack;
	}
	
	private IntRange(int low, int high) {
		this.low = low;
		this.high = high;
	}
	public int getLow() {
		return low;
	}
	public int getHigh() {
		return high;
	}
	/** Merge the other IntRange into this one, if it fits. */
	public Optional<IntRange> merge(IntRange other) {
		if (this.high == other.low) {
			this.high = other.high;
			return Optional.empty();
		} else if (this.low == other.high) {
			this.low = other.low;
			return Optional.empty();
		} else {
			return Optional.of(other);
		}
	}
	@Override
	public String toString() {
		return String.format("[%d..%d)", low, high);
	}
}