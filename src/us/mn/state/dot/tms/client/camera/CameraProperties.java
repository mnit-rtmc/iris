/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing and editing the properties of a camera.
 *
 * @author Douglas Lau
 */
public class CameraProperties extends SonarObjectForm<Camera> {

	/** Location panel */
	private final PropLocation location_pnl;

	/** Video stream encoder host (and port) */
	private final JTextField encoder_txt = new JTextField("", 20);

	/** Model for encoder channel spinner */
	private final SpinnerNumberModel num_model =
		new SpinnerNumberModel(1, 0, 10, 1);

	/** Encoder channel spinner */
	private final JSpinner enc_chn_spn = new JSpinner(num_model);

	/** Encoder type combobox */
	private final JComboBox enc_type_cbx =
		new JComboBox(EncoderType.getDescriptions());

	/** Encoder type action */
	private final IAction encoder_type = new IAction("camera.encoder.type"){
		protected void doActionPerformed(ActionEvent e) {
		      proxy.setEncoderType(enc_type_cbx.getSelectedIndex());
		}
	};

	/** Checkbox to allow publishing camera images */
	private final JCheckBox publish_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			proxy.setPublish(publish_chk.isSelected());
		}
	});

	/** Create a new camera properties form */
	public CameraProperties(Session s, Camera c) {
		super(I18N.get("camera") + ": ", s, c);
		location_pnl = new PropLocation(s, c);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<Camera> getTypeCache() {
		return state.getCamCache().getCameras();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		location_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), location_pnl);
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		if(canUpdate())
			createJobs();
		super.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		location_pnl.dispose();
		super.dispose();
	}

	/** Create camera setup panel */
	private IPanel createSetupPanel() {
		IPanel p = new IPanel();
		p.add("camera.encoder");
		p.add(encoder_txt, Stretch.LAST);
		p.add("camera.encoder.channel");
		p.add(enc_chn_spn, Stretch.LAST);
		p.add("camera.encoder.type");
		p.add(enc_type_cbx, Stretch.LAST);
		p.add("camera.publish");
		p.add(publish_chk, Stretch.LAST);
		return p;
	}

	/** Create jobs */
	private void createJobs() {
		encoder_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				proxy.setEncoder(encoder_txt.getText());
			}
		});
		enc_chn_spn.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Number c = (Number)enc_chn_spn.getValue();
				proxy.setEncoderChannel(c.intValue());
			}
		});
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		location_pnl.updateAttribute(a);
		if(a == null || a.equals("encoder")) {
			encoder_txt.setEnabled(canUpdate("encoder"));
			encoder_txt.setText(proxy.getEncoder());
		}
		if(a == null || a.equals("encoderChannel")) {
			enc_chn_spn.setEnabled(canUpdate("encoderChannel"));
			enc_chn_spn.setValue(proxy.getEncoderChannel());
		}
		if(a == null || a.equals("encoderType")) {
			enc_type_cbx.setAction(null);
			enc_type_cbx.setSelectedIndex(proxy.getEncoderType());
			encoder_type.setEnabled(canUpdate("encoderType"));
			enc_type_cbx.setAction(encoder_type);
		}
		if(a == null || a.equals("publish")) {
			publish_chk.setEnabled(canUpdate("publish"));
			publish_chk.setSelected(proxy.getPublish());
		}
	}
}
