/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import java.awt.Component;
import java.awt.FlowLayout;

import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;


// TODO TEMPORARY

/**
 * WYSIWYG DMS Message Editor MULTI Tag Option Panel containing buttons
 * with various options for MULTI Tag mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgMultiTagToolbar extends JPanel {
	
	/** Handle to the controller */
	private WController controller;
	
	/** ComboBox containing list of MULTI tags for adding/editing */
	private JComboBox<WTokenType> multiTags;
	private DefaultComboBoxModel<WTokenType> multiTagModel;
	
	/** Card layout JPanel - what is shown changes based on the tag selected */
	private JPanel tagParamPnl;
	
	/** TODO PLACEHOLDER */
	private JLabel placeholder;
	
	public WMsgMultiTagToolbar(WController c) {
		controller = c;

		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		// make and add the ComboBox
		makeMultiTagList();
		add(multiTags);
		
		placeholder = new JLabel("<MULTI TAG OPTIONS>");
		add(placeholder);
	}
	
	/** Create the model and ComboBox containing the list of MULTI tags. */
	private void makeMultiTagList() {
		// initialize the model
		multiTagModel = new DefaultComboBoxModel<WTokenType>();
		
		// go through the WTokenTypes and add all except color rectangles,
		// text rectangles, and graphics (we won't allow editing those)
		
		// TODO there are other types we want to exclude too...
		
		for (WTokenType tt: WTokenType.values()) {
			if (tt != WTokenType.colorRectangle
					&& tt != WTokenType.textRectangle
					&& tt != WTokenType.graphic) {
				multiTagModel.addElement(tt);
			}
		}
		
		// make the ComboBox
		multiTags = new JComboBox<WTokenType>(multiTagModel);
		
		// set the renderer to take the name from the label
		multiTags.setRenderer(new MultiTagListRenderer());
		
		// TODO action listener for handling selection
		
	}
	
	private class MultiTagListRenderer implements ListCellRenderer<WTokenType> {
		private DefaultListCellRenderer cell = new DefaultListCellRenderer();
		
		@Override  
		public Component getListCellRendererComponent(
				JList<?extends WTokenType> list, WTokenType tt,
		      int index, boolean isSelected, boolean cellHasFocus) {
			cell.getListCellRendererComponent(
					list, tt, index, isSelected, cellHasFocus);
			cell.setText(tt.getLabel());
		    return cell;
		  }
	}
}