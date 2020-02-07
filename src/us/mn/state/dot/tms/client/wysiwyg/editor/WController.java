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
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * WYSIWYG DMS Message Editor Controller for handling exchanges between the
 * editor GUI form and the renderer.
 *
 * @author Gordon Parikh and John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")

public class WController {
	
	/** Keep a handle to the editor for any updates we need to make from here */
	WMsgEditorForm editor;
	
	/** Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	private MultiString multiString;
	private String multiStringText = "";
	
	/** MultiConfig for config-related stuff  */
	private MultiConfig multiConfig;
	
	/** Page list */
	// TODO should we make this a generic array? might make more sense but w/e
	private DefaultListModel<WMsgSignPage> page_list_model;
	private JList<WMsgSignPage> page_list;
	
	/** DMS List (for sign groups) */
	private Map<String,DMS> dmsList;
	private JComboBox<String> dms_list;
	
	/** Currently selected page (defaults to first available) */
	private int selectedPageIndx = 0;
	private WMsgSignPage selectedPage;
	
	public WController() {
		// empty controller - everything will be set later as it is available
	}	
	
	public WController(WMsgEditorForm e) {
		editor = e;
	}	
	
	public WController(WMsgEditorForm e, DMS d) {
		editor = e;
		setSign(d);
	}
	
	public WController(WMsgEditorForm e, SignGroup g) {
		editor = e;
		setSignGroup(g);
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, DMS d) {
		editor = e;
		setSign(d);
		setQuickMessage(q);
	}
	
	public WController(WMsgEditorForm e, QuickMessage q, SignGroup g) {
		editor = e;
		setSignGroup(g);
		setQuickMessage(q);
	}
	
	/** Set the editor form handle */
	public void setEditorForm(WMsgEditorForm e) {
		editor = e;
	}
	
	/** Set the sign being used */
	public void setSign(DMS d) {
		sign = d;
		
		// generate the MultiConfig for the sign
		if (sign != null) {
			try {
				multiConfig = MultiConfig.from(sign);
			} catch (TMSException e1) {
				// TODO what to do??
			}
			update();
		}
	}
	
	/** Set the sign group being used */
	public void setSignGroup(SignGroup g) {
		sg = g;
		
		// generate the MultiConfig for the sign group
		if (sg != null)
			multiConfig = MultiConfig.from(sg);
		update();
	}
	
	/** Set the quick message being edited */
	public void setQuickMessage(QuickMessage q) {
		qm = q;
		
		// get the MULTI string text from the quick message
		if (qm != null)
			multiStringText = qm.getMulti();
		else
			multiStringText = "";
		update();
	}
	
	/** Use the AffineTransform object from the editor's SignPixelPanel to
	 *  calculate the coordinates of the click on the sign itself, rather than
	 *  the JPanel in which it resides.
	 */
	private Point2D transformSignCoordinates(int x, int y) {
		// update the editor to make sure everything is in place
		update();
		
		// get the AffineTransform object from the pixel panel
		if (editor != null) {
			AffineTransform t = editor.getEditorPixelPanel().getTransform();
			
			// calculate the adjusted coordinates of the click
			if (t != null) {
				int tx = (int) t.getTranslateX();
				int ty = (int) t.getTranslateY();
				return new Point2D.Double(x-tx, y-ty);
			}
		}
		return null;
	}
	
	/** Handle a click on the main editor panel */
	public void handleClick(MouseEvent e) {
		// calculate the adjusted coordinates of the click
		Point2D pSign = transformSignCoordinates(e.getX(), e.getY());
		
		// just print for now
		if (pSign != null) {
			int b = e.getButton();
			int x = (int) pSign.getX();
			int y = (int) pSign.getY();
			System.out.println(String.format(
					"Mouse button %d clicked at (%d, %d) ...", b, x, y));
		}
	}
	
	/** Handle a mouse move event on the main editor panel */
	public void handleMouseMove(MouseEvent e) {
		// calculate the adjusted coordinates of the mouse pointer
		Point2D pSign = transformSignCoordinates(e.getX(), e.getY());
		
		// just print for now
		if (pSign != null) {
			int x = (int) pSign.getX();
			int y = (int) pSign.getY();
//			System.out.println(String.format(
//					"Mouse moved to (%d, %d) ...", x, y));
		}
	}
	
	/** Handle a mouse drag event on the main editor panel */
	public void handleMouseDrag(MouseEvent e) {
		// calculate the adjusted coordinates of the mouse pointer
		Point2D pSign = transformSignCoordinates(e.getX(), e.getY());
		
		// figure out what button was pressed when dragging
		String b = "";
		if (SwingUtilities.isLeftMouseButton(e))
			b = "left";
		else if (SwingUtilities.isRightMouseButton(e))
			b = "right";
		else if (SwingUtilities.isMiddleMouseButton(e))
			b = "middle";
		
		// just print for now
		if (pSign != null) {
			int x = (int) pSign.getX();
			int y = (int) pSign.getY();
//			System.out.println(String.format(
//					"Mouse dragged with %s button to (%d, %d) ...", b, x, y));
		}
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
						updateSelectedPage();
					}
				}
			}
		}
		page_list.getSelectionModel().addListSelectionListener(
				new PageSelectionHandler());
		
		return page_list;
	}
	
	/** Get any message text in the controller */
	public String getMultiText() {
		return multiStringText;
	}
	
	/** Edit the MULTI string text directly and update the GUI */
	public void editMulti(String multiText) {
		multiStringText = multiText;
		update();
	}
	
	/** Update everything that needs updating */
	public void update() {
		updatePageListModel();
		
		// TODO add more stuff here eventually
	}
	
	/** Update the model containing the list of WMsgSignPage objects. Note
	 *  that page_list_model must already exist */
	private void updatePageListModel() {
		// make sure the model exists
		if (page_list_model != null) {
			// clear the model
			page_list_model.clear();
			
			if (sign != null) {
				// get the pages for the message and add them to the model
				multiString = new MultiString(multiStringText);
				for (int i = 0; i < multiString.getNumPages(); i++) {
					WMsgSignPage sp = new WMsgSignPage(sign, multiString, i);
					page_list_model.addElement(sp);
				}
	
				// update the selected page
				updateSelectedPage();
			}
		}
	}
	
	/** Update the selected page to use one in the current page_list_model. */
	private void updateSelectedPage() {
		selectedPage = (WMsgSignPage) page_list_model.get(selectedPageIndx);
		
		if (editor != null) {
			editor.setPageNumberLabel(selectedPage.getPageNumberLabel());
			editor.updateWysiwygPanel();
		}
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
					if (editor != null) {
						editor.updateWysiwygPanel();
					}
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