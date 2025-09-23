/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.util.Set;
import java.util.TreeSet;
import javax.swing.SpinnerNumberModel;
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
	static private final int MAX_DROP_ID = 65535;

	/** Comm link */
	private final CommLink comm_link;

	/** Controller cache */
	private final TypeCache<Controller> cache;

	/** Create a new drop number model */
	public DropNumberModel(CommLink cl, TypeCache<Controller> cc, int d) {
		super(d, -1, MAX_DROP_ID, 1);
		comm_link = cl;
		cache = cc;
	}

	/** Get a set of all used drops on the comm_link */
	private Set<Integer> getUsedDrops() {
		TreeSet<Integer> used = new TreeSet<Integer>();
		for (Controller c: cache) {
			if (c.getCommLink() == comm_link)
				used.add(c.getDrop());
		}
		return used;
	}

	/** Get the next value */
	@Override
	public Object getNextValue() {
		return getNextDrop();
	}

	/** Get the next drop */
	private Integer getNextDrop() {
		Set<Integer> used = getUsedDrops();
		int value = getNumber().intValue();
		for (int d = ++value; d < MAX_DROP_ID; d++) {
			if (!used.contains(d))
				return d;
		}
		return null;
	}

	/** Get the previous value */
	@Override
	public Object getPreviousValue() {
		return getPreviousDrop();
	}

	/** Get the previous drop */
	private Integer getPreviousDrop() {
		Set<Integer> used = getUsedDrops();
		int value = getNumber().intValue();
		for (int d = --value; d > 0; d--) {
			if (!used.contains(d))
				return d;
		}
		return null;
	}

	/** Get the next available value */
	public Integer getNextAvailable() {
		Set<Integer> used = getUsedDrops();
		for (int d = 1; d < MAX_DROP_ID; d++) {
			if (!used.contains(d))
				return d;
		}
		return null;
	}
}
