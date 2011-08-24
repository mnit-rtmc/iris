/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Font combobox.
 * @see FontComboBoxModel, SignMessageComposer
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontComboBox extends JComboBox implements ActionListener {

	/** Cache of font proxy objects */
	protected final TypeCache<Font> m_fonts;

	/** Font combo box model */
	private final FontComboBoxModel m_fontModel;

	/** Sign message composer */
	private final SignMessageComposer composer;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Create a new font combo box */
	public FontComboBox(TypeCache<Font> fonts, RasterBuilder builder, 
		SignMessageComposer c) 
	{
		m_fonts = fonts;
		composer = c;
		setToolTipText(I18N.get("DMSDispatcher.FontComboBox.ToolTip"));
		m_fontModel = new FontComboBoxModel(fonts, builder);
		setModel(m_fontModel);
		addActionListener(this);
	}

	/** Set the selected font number */
	public void setSelectedFontNumber(Integer fnum) {
		adjusting++;
		setSelectedItem(FontHelper.find(fnum));
		adjusting--;
	}

	/** Get the selected font number or null if nothing selected. */
	protected Integer getFontNumber() {
		Font font = (Font)getSelectedItem();
		if(font != null)
			return font.getNumber();
		else
			return null;
	}

	/** Dispose of the font combo box */
	public void dispose() {
		m_fontModel.dispose();
		removeActionListener(this);
	}

	/** Catch events: enter pressed, cbox item clicked, cursor up/down
	 * lost focus (e.g. tab pressed). Also called after a 
	 * setSelectedItem() call. Defined in interface ActionListener. */
	public void actionPerformed(ActionEvent e) {
		if(adjusting == 0)
			composer.updateMessage();
	}

	/** is this control IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.DMS_FONT_SELECTION_ENABLE.getBoolean();
	}

	/** enable or disable */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		// if disabled, reset value to default
		if(!b)
			setSelectedItem(null);
	}
}
