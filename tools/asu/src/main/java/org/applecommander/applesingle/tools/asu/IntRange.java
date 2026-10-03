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
		if (low > high) throw new UnsupportedOperationException("low cannot be greater than high");
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
		return String.format("%d..%d", low, high-1);
	}
}