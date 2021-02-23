/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016-2021  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * SignGroup combo box model.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class SignGroupComboBoxModel extends ProxyListModel<SignGroup>
	implements ComboBoxModel<SignGroup>
{
	/** Create a new SignGroupComboBox model */
	static public SignGroupComboBoxModel create(TypeCache<SignGroup> sg) {
		SignGroupComboBoxModel mdl = new SignGroupComboBoxModel(sg);
		mdl.initialize();
		return mdl;
	}

	/** Currently selected SignGroup */
	private SignGroup sel_signgroup = null;

	/** Create a new SignGroupComboBox model */
	private SignGroupComboBoxModel(TypeCache<SignGroup> sg) {
		super(sg);
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<SignGroup> comparator() {
		return new NumericAlphaComparator<SignGroup>();
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
		if (sg instanceof SignGroup)
			sel_signgroup = (SignGroup) sg;
		else
			sel_signgroup = null;
		fireContentsChanged(this, -1, -1);
	}
}
