/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Combobox model for fonts.
 * @see FontComboBox, ProxyListModel, Font, TypeCache
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontComboBoxModel extends ProxyListModel<Font>
	implements ComboBoxModel<Font>
{
	/** Create a new font combo box model */
	static public FontComboBoxModel create(TypeCache<Font> fonts,
		RasterBuilder b)
	{
		FontComboBoxModel mdl = new FontComboBoxModel(fonts, b);
		mdl.initialize();
		return mdl;
	}

	/** Raster graphic builder */
	private final RasterBuilder builder;

	/** Currently selected font */
	private Font sel_font;

	/** Create a new font combo box model */
	private FontComboBoxModel(TypeCache<Font> fonts, RasterBuilder b) {
		super(fonts);
		builder = b;
	}

	/** Get a font proxy comparator */
	@Override
	protected Comparator<Font> comparator() {
		return new Comparator<Font>() {
			public int compare(Font f0, Font f1) {
				Integer n0 = f0.getNumber();
				Integer n1 = f1.getNumber();
				return n0.compareTo(n1);
			}
			public boolean equals(Object o) {
				return o == this;
			}
			public int hashCode() {
				return super.hashCode();
			}
		};
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Font proxy) {
		return builder != null && builder.isFontUsable(proxy);
	}

	/** Get the selected item */
	@Override
	public Object getSelectedItem() {
		return sel_font;
	}

	/**
	 * Set the selected item. This method is called by the combobox when:
	 * 	-the focus leaves the combobox with a String arg when editable.
	 *      -a combobox item is clicked on via the mouse.
	 *      -a combobox item is moved to via the cursor keys.
	 */
	@Override
	public void setSelectedItem(Object f) {
		if (f instanceof Font)
			sel_font = (Font) f;
		else if (f == null)
			sel_font = null;
		else
			assert false : "unexpected type in setSelectedItem().";
		// this results in a call to editor's setSelectedItem method
		fireContentsChanged(this, -1, -1);
	}
}
