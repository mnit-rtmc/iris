/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import javax.swing.ComboBoxModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.Font;

/**
 * Combobox model for fonts.
 * @see FontComboBox, ProxyListModel, Font, TypeCache
 * @author Michael Darter
 */
public class FontComboBoxModel extends ProxyListModel<Font>
	implements ComboBoxModel 
{
	/** currently selected font */
	Font m_selected;

	/** constructor */
	public FontComboBoxModel(TypeCache<Font> arg_fonts) {
		super(arg_fonts);
		initialize();
	}

	/** Get the item at the specified index */
	public Object getElementAt(int index) {
		Font f = (Font)super.getElementAt(index);
		return f;
	}

	/** Get the selected item */
	public Object getSelectedItem() {
		return m_selected;
	}

	/** 
	 * Set the selected item. This method is called by the combobox when:
	 * 	-the focus leaves the combobox with a String arg when editable.
	 *      -a combobox item is clicked on via the mouse.
	 *      -a combobox item is moved to via the cursor keys.
	 */
	public void setSelectedItem(Object f) {
		if(f instanceof Font)
			m_selected = (Font)f;
		else if(f == null)
			m_selected = null;
		else
			assert false : "unexpected type in setSelectedItem().";
	}
}
