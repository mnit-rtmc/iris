/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.Comparator;
import us.mn.state.dot.tms.SignText;

/**
 * Comparator for sorting sign text messages.
 *
 * @author Douglas Lau
 */
public class SignTextComparator implements Comparator<SignText> {

	/** Compare one sign text message with another */
	public int compare(SignText t0, SignText t1) {
		Short s0 = t0.getLine();
		Short s1 = t1.getLine();
		int c = s0.compareTo(s1);
		if(c != 0)
			return c;
		s0 = t0.getPriority();
		s1 = t1.getPriority();
		c = s0.compareTo(s1);
		if(c != 0)
			return c;
		return t0.getMulti().compareTo(t1.getMulti());
	}

	/** Check equality */
	public boolean equals(Object o) {
		return o == this;
	}
}
