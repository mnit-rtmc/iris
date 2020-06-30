/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package us.mn.state.dot.tms.utils;

/** Helper class for creating
 *  unique SONAR record names.
 * 
 * @author John L. Stanley - SRF Consulting
 */

public class UniqueNameCreator {

	/** Interface definition for a lambda that
	 *  takes a string and returns an object. */
	public interface ILookup {
	   Object lookup(String n);
	}

	/** Unique name format */
	private String nameFormat;
	
	/** Lambda interface to lookup method.*/
	private ILookup iLookup;
	
	/** Minimum index (number part of lowest name tested) */
	private long minIndex = 1;

	/** Maximum index.
	 * Generator will wrap to minIndex when index exceeds this number.
	 * if null, maximum index = MAX_LONG. */
	private Long maxIndex = null;

	/** Index of most recent name generated */
	private long curIndex = 0;

	/** Maximum name length
	 * if null, no maximum length. */
	private Integer maxLength = null;

	/** Construct a UniqueNameCreator
	 * 
	 * Note:  The format MUST contain a %d (or
	 *  similar) integer sprinf-style
	 *  substitution-field that will be
	 *  replaced with a unique name index.
	 * i.e.  "prefix_%d"
	 * 
	 * @param format Unique name format.
	 * @param iLookup Lambda interface to helper's
	 *  lookup method.
	 * @throws IllegalFormatException if the format is invalid.
	 */
	public UniqueNameCreator(String format, ILookup iLookup) {
		this.nameFormat = format;
		this.iLookup = iLookup;

		// Make sure format string is valid
		String.format(nameFormat, 1L);
	}
	
	/** Set minimum index.
	 * if null, minimum is 1. */
	public void setMinIndex(Long minIndex) {
		if (minIndex == null)
			this.minIndex = 1L;
		else {
			assert minIndex >= 0;
			this.minIndex = minIndex;
		}
		this.curIndex = minIndex - 1;
	}
	// Add a variant to simplify use.
	public void setMinIndex(int minIndex) {
		setMinIndex((long) minIndex);
	}

	/** Set maximum index.
	 * if null, maximum index = MAX_LONG. */
	public void setMaxIndex(Long maxIndex) {
		if (maxIndex != null)
			assert maxIndex >= 0;
		this.maxIndex = maxIndex;
	}
	// Add a variant to simplify use.
	public void setMaxIndex(int maxIndex) {
		setMaxIndex((long) maxIndex);
	}

	/** Set range. */
	public void setRange(Long minIndex, Long maxIndex) {
		setMinIndex(minIndex);
		setMaxIndex(maxIndex);
	}
	// Add a few variants to simplify use.
	// (Avoids having to cast small numbers to long.)
	public void setRange(int minIndex, Long maxIndex) {
		setMinIndex((long)minIndex);
		setMaxIndex(maxIndex);
	}
	public void setRange(int minIndex, int maxIndex) {
		setMinIndex((long)minIndex);
		setMaxIndex((long)maxIndex);
	}

	/** Set the maximum name length.
	 * if null, no maximum length. */
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	/** Test name for uniqueness.
	 * @return Returns true if name is used.  False if it is not. */
	private boolean isAlreadyUsed(String sName) {
		return (iLookup.lookup(sName) != null);
	}

	/** Create the next name */
	private String createNextName() {
		++curIndex;
		if ((maxIndex != null) && (curIndex > maxIndex))
			curIndex = minIndex;
		else if (curIndex < 0)
			curIndex = minIndex;
		String n = String.format(nameFormat, curIndex);
		if ((maxLength != null) && (n.length() > maxLength)) {
			curIndex = minIndex;
			n = String.format(nameFormat, curIndex);
		}
		return n;
	}

	/** Create a unique name */
	public synchronized String createUniqueName() {
		String n = createNextName();
		// Following line must come AFTER createNextName()
		long startIndex = curIndex;
		while (isAlreadyUsed(n)) {
			n = createNextName();
			if (startIndex == curIndex) {
				// tried every possible name and failed...
				assert false;
				return null;
			}
		}
		return n;
	}
}
