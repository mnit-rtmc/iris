/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.util.TreeSet;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;

/**
 * Drop number model for drop address spinners.
 *
 * @author Douglas Lau
 */
public class DropNumberModel extends SpinnerNumberModel {

	/** Maximum allowed drop address */
	static protected final short MAX_DROP_ID = 9999;

	/** Comm link */
	protected final CommLink comm_link;

	/** Controller cache */
	protected final TypeCache<Controller> cache;

	/** Create a new drop number model */
	public DropNumberModel(CommLink cl, TypeCache<Controller> cc, int d) {
		super(d, 1, MAX_DROP_ID, 1);
		comm_link = cl;
		cache = cc;
	}

	/** Class to find next/previous available drop address */
	protected class DropFinder implements Checker<Controller> {
		TreeSet<Short> used = new TreeSet<Short>();
		public boolean check(Controller c) {
			if(c.getCommLink() == comm_link)
				used.add(c.getDrop());
			return false;
		}
		public Short getNextDrop() {
			short value = getNumber().shortValue();
			for(short d = ++value; d < MAX_DROP_ID; d++) {
				if(!used.contains(d))
					return d;
			}
			return null;
		}
		public Short getPreviousDrop() {
			short value = getNumber().shortValue();
			for(short d = --value; d > 0; d--) {
				if(!used.contains(d))
					return d;
			}
			return null;
		}
		public Short getNextAvailable() {
			for(short d = 1; d < MAX_DROP_ID; d++) {
				if(!used.contains(d))
					return d;
			}
			return null;
		}
	}

	/** Get the next value */
	public Object getNextValue() {
		DropFinder drop_finder = new DropFinder();
		cache.findObject(drop_finder);
		return drop_finder.getNextDrop();
	}

	/** Get the previous value */
	public Object getPreviousValue() {
		DropFinder drop_finder = new DropFinder();
		cache.findObject(drop_finder);
		return drop_finder.getPreviousDrop();
	}

	/** Get the next available value */
	public Short getNextAvailable() {
		DropFinder drop_finder = new DropFinder();
		cache.findObject(drop_finder);
		return drop_finder.getNextAvailable();
	}
}
