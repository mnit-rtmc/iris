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

import javax.swing.ListCellRenderer;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JList;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.wysiwyg.editor.tags.WMultiTagDialog;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;


// TODO TEMPORARY

/**
 * WYSIWYG DMS Message Editor MULTI Tag Option Panel containing buttons
 * with various options for MULTI Tag mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgMultiTagToolbar extends WToolbar {
	
	/** ComboBox containing list of MULTI tags for adding/editing */
	private JComboBox<WTokenType> multiTags;
	private DefaultComboBoxModel<WTokenType> multiTagModel;
	
	/** Add, edit, and delete buttons (edit and delete buttons are disabled if
	 *  not next to a tag). */
	private JButton addBtn;
	private JButton editBtn;
	private JButton deleteBtn;
	
	public WMsgMultiTagToolbar(WController c) {
		super(c);
		
		System.out.println(controller.toString());
		
		// use a left-aligned FlowLayout
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		// make and add the ComboBox
		makeMultiTagList();
		add(multiTags);
		
		// and the buttons
		addBtn = new JButton(addTag);
		add(addBtn);
		add(Box.createHorizontalStrut(50));
		editBtn = new JButton(controller.editTag);
		add(editBtn);
		deleteBtn = new JButton(controller.delete);
		add(deleteBtn);
	}
	
	/** Create the model and ComboBox containing the list of MULTI tags. */
	private void makeMultiTagList() {
		// initialize the model
		multiTagModel = new DefaultComboBoxModel<WTokenType>();
		
		// add the token types in a specific order, leaving some out
		//--------------------------------
		// IRIS Action tags
		//--------------------------------
		// Msg-Feed
		multiTagModel.addElement(WTokenType.feedMsg);
		
		// Parking Area Availability
		multiTagModel.addElement(WTokenType.parkingAvail);
		
		// Slow Traffic Warning
		multiTagModel.addElement(WTokenType.slowWarning);
		
		// Travel Time
		multiTagModel.addElement(WTokenType.travelTime);
		
		// Toll Zone Pricing
		multiTagModel.addElement(WTokenType.tolling);
		
		// Incident Locator
		multiTagModel.addElement(WTokenType.incidentLoc);
		
		// Variable Speed Advisory
		multiTagModel.addElement(WTokenType.speedAdvisory);
		
		// IPAWS CAP Time substitution
		multiTagModel.addElement(WTokenType.capTime);
		
		// IPAWS CAP Response Type substitution
		multiTagModel.addElement(WTokenType.capResponse);
		
		// IPAWS CAP Urgency substitution
		multiTagModel.addElement(WTokenType.capUrgency);
		
		//--------------------------------
		// Tags NOT implemented elsewhere
		//--------------------------------
		// Character Spacing
		multiTagModel.addElement(WTokenType.spacingChar);
		
		//--------------------------------
		// Tags implemented elsewhere
		//--------------------------------
		// New line (for line spacing)
		multiTagModel.addElement(WTokenType.newLine);
		
		// Page timing
		multiTagModel.addElement(WTokenType.pageTime);
		
		// Page background
		multiTagModel.addElement(WTokenType.pageBackground);
		
		// Foreground color
		multiTagModel.addElement(WTokenType.colorForeground);
		
		// Font
		multiTagModel.addElement(WTokenType.font);
		
		// Page justification
		multiTagModel.addElement(WTokenType.justificationPage);
		
		// Line Justification
		multiTagModel.addElement(WTokenType.justificationLine);
		
		// Color Rectangles
		multiTagModel.addElement(WTokenType.colorRectangle);
		
		// Text Rectangles
		multiTagModel.addElement(WTokenType.textRectangle);
		
		// Graphics
		multiTagModel.addElement(WTokenType.graphic);
		
		// make the ComboBox
		multiTags = new JComboBox<WTokenType>(multiTagModel);
		
		// set the renderer to take the name from the label
		multiTags.setRenderer(new MultiTagListRenderer());
		
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
	
	/** Add tag action */
	private IAction addTag = new IAction(
			"wysiwyg.multi_tag_dialog.add") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// get the token type from the box and open the proper form
			WTokenType tokType = (WTokenType) multiTags.getSelectedItem();
			WMultiTagDialog d = WMultiTagDialog.construct(
					controller, tokType, null);
			if (d != null) {
				JInternalFrame f = controller.getDesktop().show(d);
				f.requestFocusInWindow();
				d.requestFocusInWindow();
			}
		}
	};
	
	/** Enable or disable tag edit and delete buttons */
	public void updateTagEditButton(boolean state) {
		editBtn.setEnabled(state);
		deleteBtn.setEnabled(state);
	}
	
	@Override
	public void setColor(Color c, String mode) {
		// this does nothing in this toolbar
	}
}