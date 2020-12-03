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
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Dialog window containing information on video source template config
 * substitution fields.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class VidSrcTemplateConfigHelp extends AbstractForm {
	
	/** Headings for the "table" */
	private final JTextArea vscFieldLbl = new JTextArea(1, 15);
	private final JTextArea vscFieldDesc = new JTextArea(1, 35);
	
	/** Text areas containing selectable substitution field text and labels
	 *  containing non-selectable descriptions of the fields.
	 */
	/** {addr} field */
	private final JTextArea vscAddrLbl = new JTextArea(1, 15);
	private final JTextArea vscAddrDesc = new JTextArea(1, 35);
	
	/** {port} field */
	private final JTextArea vscPortLbl = new JTextArea(2, 15);
	private final JTextArea vscPortDesc = new JTextArea(2, 35);
	
	/** {addrport} field */
	private final JTextArea vscAddrPortLbl = new JTextArea(1, 15);
	private final JTextArea vscAddrPortDesc = new JTextArea(1, 35);
	
	/** {maddr} field */
	private final JTextArea vscMAddrLbl = new JTextArea(1, 15);
	private final JTextArea vscMAddrDesc = new JTextArea(1, 35);
	
	/** {mport} field */
	private final JTextArea vscMPortLbl = new JTextArea(2, 15);
	private final JTextArea vscMPortDesc = new JTextArea(2, 35);
	
	/** {maddrport} field */
	private final JTextArea vscMAddrPortLbl = new JTextArea(1, 15);
	private final JTextArea vscMAddrPortDesc = new JTextArea(1, 35);
	
	/** {chan} field */
	private final JTextArea vscChanLbl = new JTextArea(1, 15);
	private final JTextArea vscChanDesc = new JTextArea(1, 35);
	
	/** {name} field */
	private final JTextArea vscNameLbl = new JTextArea(1, 15);
	private final JTextArea vscNameDesc = new JTextArea(1, 35);
	
	/** {dist} field */
	private final JTextArea vscDistLbl = new JTextArea(1, 15);
	private final JTextArea vscDistDesc = new JTextArea(1, 35);
	
	/** {session-id} field */
	private final JTextArea vscSIdLbl = new JTextArea(1, 15);
	private final JTextArea vscSIdDesc = new JTextArea(1, 35);
	
	/** {pname} field */
	private final JTextArea vscPNameLbl = new JTextArea(2, 15);
	private final JTextArea vscPNameDesc = new JTextArea(2, 35);
	
	/** {<other>} field */
	private final JTextArea vscMiscLbl = new JTextArea(3, 15);
	private final JTextArea vscMiscDesc = new JTextArea(3, 35);
	
	/** Notes */
	private final ILabel notesLbl = new ILabel(
			"camera.video_source.template.config.notes");
	private final JTextArea note1 = new JTextArea(2, 50);
	private final JTextArea note2 = new JTextArea(4, 50);
	
	protected VidSrcTemplateConfigHelp() {
		super(I18N.get("camera.video_source.template.config.help"), true);
		
		// instantiate labels and set options
		initLabels();
		
		// load text
		setLabelText();
	}
	
	/** Initialize the labels (just instantiates and sets options, no text). */
	private void initLabels() {
		JTextArea labels[] = {vscFieldLbl, vscAddrLbl, vscPortLbl,
				vscAddrPortLbl, vscMAddrLbl, vscMPortLbl, vscMAddrPortLbl,
				vscChanLbl, vscNameLbl, vscDistLbl, vscSIdLbl, vscPNameLbl,
				vscMiscLbl};
		JTextArea descriptions[] = {vscFieldDesc, vscAddrDesc, vscPortDesc,
				vscAddrPortDesc, vscMAddrDesc, vscMPortDesc, vscMAddrPortDesc,
				vscChanDesc, vscNameDesc, vscDistDesc, vscSIdDesc,
				vscPNameDesc, vscMiscDesc, note1, note2};
		
		// draw a border to make it look like a table
		Border border = BorderFactory.createLineBorder(Color.GRAY);
		
		// initialize substitution field name labels
		for (JTextArea l: labels) {
			// not editable, and the background is the gray panel color
			l.setEditable(false);
			l.setBackground(UIManager.getColor("Panel.background"));
			l.setBorder(BorderFactory.createCompoundBorder(border,
			            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		}
		
		// initialize description labels
		for (JTextArea d: descriptions) {
			// not editable, and the background is the gray panel color
			d.setEditable(false);
			d.setBackground(UIManager.getColor("Panel.background"));
			
			// word line wrapping
			d.setLineWrap(true);
			d.setWrapStyleWord(true);
			d.setBorder(BorderFactory.createCompoundBorder(border,
			            BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		}
	}
	
	/** Set the text in each "label" */
	private void setLabelText() {
		// "headings" (in bold)
		Font f = vscFieldLbl.getFont();
		Font fb = f.deriveFont(f.getStyle() | Font.BOLD);
		vscFieldLbl.setText(I18N.get(
				"camera.video_source.template.config.field_name"));
		vscFieldLbl.setFont(fb);
		vscFieldDesc.setText(I18N.get(
				"camera.video_source.template.config.field_desc"));
		vscFieldDesc.setFont(fb);
		
		// "labels" (hard-coded since they won't change)
		vscAddrLbl.setText("{addr}");
		vscPortLbl.setText("{port}");
		vscAddrPortLbl.setText("{addrport}");
		vscMAddrLbl.setText("{maddr}");
		vscMPortLbl.setText("{mport}");
		vscMAddrPortLbl.setText("{maddrport}");
		vscChanLbl.setText("{chan}");
		vscNameLbl.setText("{name}");
		vscDistLbl.setText("{dist}");
		vscSIdLbl.setText("{session-id}");
		vscPNameLbl.setText("{pname}");
		vscMiscLbl.setText(I18N.get(
				"camera.video_source.template.config.misc"));
		
		// "descriptions" (I18N'd)
		vscAddrDesc.setText(I18N.get(
				"camera.video_source.template.config.addr"));
		vscPortDesc.setText(I18N.get(
				"camera.video_source.template.config.port"));
		vscAddrPortDesc.setText(I18N.get(
				"camera.video_source.template.config.addrport"));
		vscMAddrDesc.setText(I18N.get(
				"camera.video_source.template.config.maddr"));
		vscMPortDesc.setText(I18N.get(
				"camera.video_source.template.config.mport"));
		vscMAddrPortDesc.setText(I18N.get(
				"camera.video_source.template.config.maddrport"));
		vscChanDesc.setText(I18N.get(
				"camera.video_source.template.config.chan"));
		vscNameDesc.setText(I18N.get(
				"camera.video_source.template.config.name"));
		vscDistDesc.setText(I18N.get(
				"camera.video_source.template.config.dist"));
		vscSIdDesc.setText(I18N.get(
				"camera.video_source.template.config.session_id"));
		vscPNameDesc.setText(I18N.get(
				"camera.video_source.template.config.pname"));
		vscMiscDesc.setText(I18N.get(
				"camera.video_source.template.config.misc.msg"));
		
		// additional notes
		note1.setText(I18N.get(
				"camera.video_source.template.config.notes.1"));
		note2.setText(I18N.get(
				"camera.video_source.template.config.notes.2"));
	}
	
	/** Layout the form */
	@Override
	protected void initialize() {
		// put everything in a box layout panel
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		// add the fields in rows of label: description, followed by the notes
		
		// use a layout with no gaps to make it look like a table
		FlowLayout flayout = new FlowLayout(FlowLayout.LEFT);
		flayout.setHgap(0);
		flayout.setVgap(0);
		
		// start with the headings
		JPanel row = new JPanel(flayout);
		row.add(vscFieldLbl);
		row.add(vscFieldDesc);
		p.add(row);

		row = new JPanel(flayout);
		row.add(vscAddrLbl);
		row.add(vscAddrDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscPortLbl);
		row.add(vscPortDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscAddrPortLbl);
		row.add(vscAddrPortDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscMAddrLbl);
		row.add(vscMAddrDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscMPortLbl);
		row.add(vscMPortDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscMAddrPortLbl);
		row.add(vscMAddrPortDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscChanLbl);
		row.add(vscChanDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscNameLbl);
		row.add(vscNameDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscDistLbl);
		row.add(vscDistDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscSIdLbl);
		row.add(vscSIdDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscPNameLbl);
		row.add(vscPNameDesc);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(vscMiscLbl);
		row.add(vscMiscDesc);
		p.add(row);
		
		row = new JPanel(new FlowLayout(FlowLayout.LEFT));
		row.add(notesLbl);
		p.add(row);

		row = new JPanel(flayout);
		row.add(note1);
		p.add(row);
		
		row = new JPanel(flayout);
		row.add(note2);
		p.add(row);
		
		add(p);
	}
}















