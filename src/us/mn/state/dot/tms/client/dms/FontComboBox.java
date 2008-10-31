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

package us.mn.state.dot.tms.client.dms;

import java.rmi.RemoteException;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JComboBox;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.dms.DMSDispatcher;
import us.mn.state.dot.tms.Font;

/**
 * Combobox for font selection. This combobox contains sonar Font objects.
 * It is single selection and non-editable. The combobox is loaded with all
 * IRIS fonts via the model FontComboBoxModel, which extends ProxyListModel.
 * When a user changes the combobox selected font, the "preferred font" in
 * the associated DMS is updated.
 * @see DMSDispatcher, ProxyListModel, Font, FontImpl, DMS, DMSImpl, TypeCache
 * @author Michael Darter
 */
public class FontComboBox extends JComboBox 
{
	/* parent container */
	DMSDispatcher m_dmsDispatcher;

	/* constructor */
	public FontComboBox(DMSDispatcher dmsDispatcher, TypeCache<Font> arg_fonts) {
		super();
		assert dmsDispatcher != null;
		m_dmsDispatcher = dmsDispatcher;
		setModel(new FontComboBoxModel(arg_fonts));
	}

	/** Set the currently selected font. A null arg is ignored. */
	public void setSelectedItem(Object item) {
		super.setSelectedItem(item);
		setPreferredDMSFont((Font) item);
	}

	/** Set preferred font in associated DMS */
	protected void setPreferredDMSFont(Font f) {
		if(f == null)
			return;
		DMSProxy dms = m_dmsDispatcher.getSelectedDms();
		if(dms == null)
			return;
		if(dms.dms != null) {
			String fname = f.getName();
			try {
				dms.dms.setPreferredFontName(fname);
			} catch(RemoteException ex) {}
		}
	}

	/** Get preferred font in associated DMS */
	protected String getPreferredDMSFont()
	{
		String fname = "";
		DMSProxy dms = m_dmsDispatcher.getSelectedDms();
		if(dms == null)
			return fname;
		if(dms.dms != null) {
			try {
				fname = dms.dms.getPreferredFontName();
			} catch(RemoteException ex) {}
		}
		return fname;
	}

	/** 
	 *  Get the currently selected font.
	 *  @return The selected font or null of nothing selected
	 */
	public Font getSelectedItem()
	{
		Object obj = super.getSelectedItem();
		if(obj == null)
			return null;
		if(obj instanceof Font)
			return (Font)obj;
		assert false : "Unknown object in getSelectedItem()";
		return null;
	}

	/** return the combobox item index of a matching item or -1 if not found */
	protected int search(String fontName) { 
		final int NOTFOUND = -1;
		if( fontName == null )
			return NOTFOUND;
		for(int i=0; i<getItemCount(); ++i) {
			Font f = (Font)getItemAt(i);
			if(f.getName().equals(fontName))
				return i;
		}
		return NOTFOUND;
	}


	/** Set the default combobox selection */
	public void setDefaultSelection() {
		setSelectedIndex(getDefaultSelectionIndex());
	}

	/** 
	 *  return the combobox index of the font specified in the
	 *  associated DMS.
	 *  @return the index of the preferred font else -1 for no selection.
	 */
	protected int getDefaultSelectionIndex() {
		final int NOTFOUND = -1;

		// nothing in combobox yet
		if(getItemCount() <= 0)
			return NOTFOUND;

		// combobox is loaded, get preferred DMS font name
		String fname = getPreferredDMSFont();
		return search(fname);
	}
}

