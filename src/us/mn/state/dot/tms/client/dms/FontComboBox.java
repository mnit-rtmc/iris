/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import javax.swing.JComboBox;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Font combobox.
 * @see FontComboBoxModel, SignMessageComposer
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontComboBox extends JComboBox
{
	/** Cache of font proxy objects */
	protected final TypeCache<Font> m_fonts;

	/** Font combo box model */
	private final FontComboBoxModel m_fontModel;

	/** constructor */
	public FontComboBox(TypeCache<Font> fonts, PixelMapBuilder builder) {
		m_fonts = fonts;
		setToolTipText(I18N.get("DMSDispatcher.FontComboBox.ToolTip"));
		m_fontModel = new FontComboBoxModel(fonts, builder);
		setModel(m_fontModel);
	}

	/** Get the selected font number */
	protected Integer getFontNumber() {
		Font font = (Font)getSelectedItem();
		if(font == null)
			return null;
		return font.getNumber();
	}

	/** dispose */
	public void dispose() {
		m_fontModel.dispose();
	}
}
