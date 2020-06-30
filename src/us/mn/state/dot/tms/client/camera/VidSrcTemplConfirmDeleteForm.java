/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020 SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.DisabledSelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Dialog for user to confirm if they want to delete a video source template.
 * Also displays a list of any camera templates that use this video source (if
 * any).
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class VidSrcTemplConfirmDeleteForm extends AbstractForm {
	
	/** Handle to the main vid source template editor form */
	private VidSourceTemplateEditor eForm;
	
	/** Model for Camera Templates assigned to the selected video source */
	private final DefaultListModel<String> camTemplateModel =
		new DefaultListModel<String>();

	/** List of Camera Templates linking to the selected video source (only
	 *  shows if there are camera templates associated).
	 */
	private final JList<String> camTemplateList =
			new JList<String>(camTemplateModel);
	
	/** Primary message to prompt the user. */
	private JLabel confirmMsgLbl;
	
	/** Message added if there are camera templates using this vid source. */
	private ILabel camTemplateMsgLbl;
	
	/** Label above camera template list. */
	private ILabel camTemplateLbl;
	
	/** Delete (confirm) button */
	private JButton deleteBtn;
	
	/** Cancel button */
	private JButton cancelBtn;
	
	protected VidSrcTemplConfirmDeleteForm(
			String t, VidSourceTemplateEditor f) {
		super(t);
		eForm = f;
		
		// instantiate GUI elements
		confirmMsgLbl = new JLabel(String.format(I18N.get(
				"camera.video_source.template.confirm_delete_msg"),
				eForm.getSelectedVideoSource().getLabel()));
		Dimension d = confirmMsgLbl.getPreferredSize();
		d.width = 250;
		d.height *= 2;
		confirmMsgLbl.setPreferredSize(d);
		
		// determine if there are any camera templates associated with this
		// video source template
		ArrayList<CameraTemplate> camTemplates =
				eForm.getCamTemplatesForVidSource();
		if (camTemplates.size() > 0) {
			camTemplateMsgLbl = new ILabel(
					"camera.video_source.template.confirm_cam_template_msg");
			d.height = 4*camTemplateMsgLbl.getPreferredSize().height;
			camTemplateMsgLbl.setPreferredSize(d);
			
			camTemplateLbl = new ILabel(
					"camera.video_source.template.confirm_cam_template");
			
			for (CameraTemplate ct: camTemplates)
				camTemplateModel.addElement(ct.getLabel());
			
			camTemplateList.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(Color.GRAY),
		            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
			camTemplateList.setSelectionModel(new DisabledSelectionModel());
			d = camTemplateList.getPreferredSize();
			d.width = 250;
			camTemplateList.setPreferredSize(d);
		}
		
		deleteBtn = new JButton(delete);
		cancelBtn = new JButton(cancel);
	}
	

	/** Initialize the form */
	@Override
	protected void initialize() {
		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel gbPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 10;
		gbc.ipady = 10;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		/* Main confirmation message */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbPanel.add(confirmMsgLbl, gbc);
		
		/* Secondary warning message (not always shown) */
		if (camTemplateModel.getSize() > 0) {
			gbc.gridy += 1;
			gbPanel.add(camTemplateMsgLbl, gbc);
			gbc.gridy += 1;
			gbPanel.add(camTemplateLbl, gbc);
			gbc.gridy += 1;
			JScrollPane ctPn = new JScrollPane(camTemplateList,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			gbc.anchor = GridBagConstraints.BASELINE_LEADING;
			gbPanel.add(ctPn, gbc);
		}
		
		/* Delete/Cancel buttons */
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridwidth = 1;
		gbc.gridy += 1;
		gbPanel.add(deleteBtn, gbc);
		gbc.gridx = 1;
		gbPanel.add(cancelBtn, gbc);
		add(gbPanel);
	}
	
	/** Button to delete */
	private IAction delete = new IAction(
			"camera.video_source.template.confirm_delete_btn") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			System.out.println("Deleting...");
			eForm.deleteSelectedVidSrc();
			close(Session.getCurrent().getDesktop());
		}
	};
	
	/** Close the form without deleting. */
	private IAction cancel = new IAction(
			"camera.video_source.template.confirm_cancel_btn") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			close(Session.getCurrent().getDesktop());
		}
	};
}

















