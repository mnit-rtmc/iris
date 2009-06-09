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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Font combobox.
 * @see FontComboBoxModel, SignMessageComposer
 * @author Michael Darter
 * @author Douglas Lau
 */
public class FontComboBox extends JComboBox implements ActionListener
{
	/** Cache of font proxy objects */
	protected final TypeCache<Font> m_fonts;

	/** Font combo box model */
	private final FontComboBoxModel m_fontModel;

	/** This component's container */
	private final SignMessageComposer m_composer;

	/** constructor */
	public FontComboBox(TypeCache<Font> fonts, PixelMapBuilder builder, 
		SignMessageComposer c) 
	{
		m_fonts = fonts;
		m_composer = c;
		setToolTipText(I18N.get("DMSDispatcher.FontComboBox.ToolTip"));
		m_fontModel = new FontComboBoxModel(fonts, builder);
		setModel(m_fontModel);
		addActionListener(this);
	}

	/** Set the selected item.
	 *  @param obj Should be an Integer which is the font number, else
	 *	       the default font will be set. */
	public void setSelectedItem(Object obj) {
		if(obj == null) {
			obj = FontHelper.getDefault();
		} else if(obj instanceof Font) {
			// set arg
		// set current using font number
		} else if(obj instanceof Integer) {
			obj = FontHelper.find((Integer)obj);
		} else {
			obj = FontHelper.getDefault();
		}
		super.setSelectedItem(obj);
	}

	/** Ignore flag is > 0 when actionPerformed events should 
	 *  be ignored. */
	private int m_ignore = 0;

	/** Set the selected item and ignore any actionPerformed 
	 *  events that are generated. */
	public void setSelectedItemNoAction(Object obj) {
		++m_ignore;
		setSelectedItem(obj);
		--m_ignore;
	}

	/** Get the selected font number or null if nothing selected. */
	protected Integer getFontNumber() {
		Font font = (Font)getSelectedItem();
		Integer ret = null;
		if(font != null)
			ret = font.getNumber();
		return ret;
	}

	/** dispose */
	public void dispose() {
		m_fontModel.dispose();
		removeActionListener(this);
	}

	/** Catch events: enter pressed, cbox item clicked, cursor up/down
	 *  lost focus (e.g. tab pressed). Also called after a 
	 *  setSelectedItem() call. Defined in interface ActionListener. */
	public void actionPerformed(ActionEvent e) {
		// only update preview if user clicked font cbox
		if(m_ignore == 0)
			if("comboBoxChanged".equals(e.getActionCommand()))
				m_composer.selectPreview(true);
	}

	/** is this control IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.
			DMS_FONT_SELECTION_ENABLE.getBoolean();
	}

	/** enable or disable */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		// if disabled, reset value to default
		if(!b)
			setSelectedItem(FontHelper.getDefault());
	}
}
