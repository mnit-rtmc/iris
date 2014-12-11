/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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
import javax.swing.ComboBoxModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * SignGroup combo box model
 *
 * @author Travis Swanston
 */
public class SignGroupComboBoxModel extends ProxyListModel<SignGroup>
	implements ComboBoxModel
{
	/** Currently selected SignGroup */
	private SignGroup sel_signgroup = null;

	/** Create a new SignGroupComboBox model */
	public SignGroupComboBoxModel(TypeCache<SignGroup> sg) {
		super(sg);
		initialize();
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<SignGroup> comparator() {
		return new Comparator<SignGroup>() {
			public int compare(SignGroup sg0, SignGroup sg1) {
				String s0 = sg0.getName();
				String s1 = sg1.getName();
				return s0.compareTo(s1);
			}
			public boolean equals(Object o) {
				return o == this;
			}
			public int hashCode() {
				return super.hashCode();
			}
		};
	}

	/**
	 * Get the selected item.
	 *
	 * @return The selected SignGroup (as type Object), or null if none
	 *         selected.
	 */
	@Override
	public Object getSelectedItem() {
		return sel_signgroup;
	}

	/** Set the selected item. */
	@Override
	public void setSelectedItem(Object sg) {
		if (sg == null)
			sel_signgroup = null;
		else if (sg instanceof SignGroup)
			sel_signgroup = (SignGroup)sg;
		// notify
		fireContentsChanged(this, -1, -1);
	}
}
