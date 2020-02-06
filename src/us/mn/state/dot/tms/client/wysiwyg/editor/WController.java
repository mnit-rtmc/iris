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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * WYSIWYG DMS Message Editor Controller for handling exchanges between the
 * editor GUI form and the renderer.
 *
 * @author Gordon Parikh and John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")

public class WController {
	
	/* Keep a handle to the editor for any updates we need to make from here */
	WMsgEditorForm editor;
	
	/* Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	
	/* Page list */
	// TODO should we make this a generic array? might make more sense but w/e
	private DefaultListModel<WMsgSignPage> page_list_model;
	private JList<WMsgSignPage> page_list;
	
	/* DMS List (for sign groups) */
	private Map<String,DMS> dmsList;
	private JComboBox<String> dms_list;
	
	/* Currently selected page (defaults to first available) */
	private int selectedPageIndx = 0;
	private WMsgSignPage selectedPage;
	
	public WController(WMsgEditorForm e) {
		editor = e;
	}	
	
	public WController(WMsgEditorForm e, DMS d) {
		editor = e;
		sign = d;
	}
	
	public WController(WMsgEditorForm e, SignGroup g) {
		editor = e;
		sg = g;
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, DMS d) {
		editor = e;
		qm = q;
		sign = d;
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, SignGroup g) {
		editor = e;
		qm = q;
		sg = g;
	}
	
	/** Change the sign being used */
	public void setSign(DMS d) {
		sign = d;
	}
	
	/** Return a JList of WMsgSignPage objects from the selected/created message */
	public JList<WMsgSignPage> getPageList() {
		page_list_model = new DefaultListModel<WMsgSignPage>();
		updatePageListModel();

		// reset the list
		page_list = new JList<WMsgSignPage>(page_list_model);
		
		// set the renderer on the list
		ListCellRenderer<WMsgSignPage> rndr = new WMsgSignPageListRenderer();
		page_list.setCellRenderer(rndr);
		
		// set up the page selection handler
		page_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		class PageSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					int indx = lsm.getMinSelectionIndex();
					
					if (indx != -1) {
						selectedPageIndx = indx;
						selectedPage = page_list.getModel()
								.getElementAt(selectedPageIndx);
						editor.setPageNumberLabel(
								selectedPage.getPageNumberLabel());
						editor.updateWysiwygPanel();
					}
				}
			}
		}
		page_list.getSelectionModel().addListSelectionListener(
				new PageSelectionHandler());
		
		return page_list;
	}
	
	/** Update the model containing the list of WMsgSignPage objects. Note
	 *  that page_list_model must already exist */
	private void updatePageListModel() {
		// make sure the model exists
		if (page_list_model != null) {
			// clear the model
			page_list_model.clear();
			
			// get the pages for the message and add them to the model
			String ms = qm.getMulti();
			MultiString mso = new MultiString(ms);
			for (int i = 0; i < mso.getNumPages(); i++) {
				WMsgSignPage sp = new WMsgSignPage(sign, mso, i);
				page_list_model.addElement(sp);
			}

			// update the selected page
			updateSelectedPage();
		}
	}
	
	/** Update the selected page to use one in the current page_list_model. */
	private void updateSelectedPage() {
		selectedPage = (WMsgSignPage) page_list_model.get(selectedPageIndx);
		editor.setPageNumberLabel(selectedPage.getPageNumberLabel());
		editor.updateWysiwygPanel();
	}
	
	/** Get a JComboBox containing a list of sign names (only applies when
	 * editing a group).
	 * TODO same note as with JList above - should maybe abstract this more
	 * but whatever... 
	 */
	public JComboBox<String> getSignListForGroup() {
		if (sg != null) {
			// get the list of signs in the sign group
			// look through the DmsSignGroups to find all signs with this group
			dmsList = new HashMap<String,DMS>();
			Iterator<DmsSignGroup> dsgit = DmsSignGroupHelper.iterator();
			while (dsgit.hasNext()) {
				DmsSignGroup dsg = dsgit.next();
				if (dsg.getSignGroup() == sg) {
					DMS dms = dsg.getDms();
					dmsList.put(dms.getName(), dms);
				}
			}
			
			// selection handler for the combo box
			class SignSelectionListener implements ActionListener {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					JComboBox<String> cb = (JComboBox<String>) e.getSource();
					String dmsName = (String) cb.getSelectedItem();
					// TODO - do we need to catch any exceptions here?
					sign = dmsList.get(dmsName);
					
					// TODO a bunch more needs to happen here in addition to this
					// (need to remake page list and stuff)
					updatePageListModel();
					editor.updateWysiwygPanel();
				}
			}
			
			// setup and return the combo box (making sure to sort the list)
			String[] dmsNames = Arrays.stream(dmsList.keySet().toArray()).
					toArray(String[]::new);
			Arrays.sort(dmsNames);
			dms_list = new JComboBox<String>(dmsNames);
			dms_list.addActionListener(new SignSelectionListener());
			
			// also set the current sign to the first one in the group
			sign = dmsList.get(dmsNames[0]);
		}
		return dms_list;
	}
	
	/* Get the current DMS object */
	public DMS getSign() {
		return sign;
	}
	
	public WMsgSignPage getSelectedPage() {
		return selectedPage;
	}
}