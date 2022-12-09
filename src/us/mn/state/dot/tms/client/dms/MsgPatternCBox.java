/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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

import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * The message pattern combobox is a widget which allows the user to select
 * a message pattern.
 *
 * @see us.mn.state.dot.tms.MsgPattern
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternCBox extends JComboBox<MsgPattern> {

	/** Check if a message pattern should be included in combo box */
	static private boolean isValidMulti(MsgPattern pat) {
		MultiString ms = new MultiString(pat.getMulti());
		return ms.isValidMulti();
	}

	/** Combo box model for message patterns */
	private final DefaultComboBoxModel<MsgPattern> model =
		new DefaultComboBoxModel<MsgPattern>();

	/** Create a new message pattern combo box */
	public MsgPatternCBox() {
		setModel(model);
	}

	/** Populate the message pattern model, sorted */
	public void populateModel(DMS dms) {
		TreeSet<MsgPattern> msgs = createMessageSet(dms);
		model.removeAllElements();
		model.addElement(null);
		for (MsgPattern pat: msgs)
			model.addElement(pat);
	}

	/** Create a set of message patterns for the specified DMS */
	private TreeSet<MsgPattern> createMessageSet(DMS dms) {
		TreeSet<MsgPattern> msgs = new TreeSet<MsgPattern>(
			new NumericAlphaComparator<MsgPattern>());
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms) {
				SignGroup sg = dsg.getSignGroup();
				Iterator<MsgPattern> pit =
					MsgPatternHelper.iterator();
				while (pit.hasNext()) {
					MsgPattern pat = pit.next();
					if (pat.getSignGroup() == sg) {
						if (isValidMulti(pat))
							msgs.add(pat);
					}
				}
			}
		}
		return msgs;
	}

	/** Set the enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		if (!e) {
			setSelectedItem(null);
			removeAllItems();
		}
	}

	/** Get the selected message pattern */
	public MsgPattern getSelectedPattern() {
		Object obj = getSelectedItem();
		return (obj instanceof MsgPattern)
		      ? (MsgPattern) obj
		      : null;
	}
}
