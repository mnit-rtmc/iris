/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sched.FocusLostJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.EncoderType;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.client.roads.LocationPanel;
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
	private final LocationPanel location;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(3, 24);

	/** Controller action */
	private final IAction controller = new IAction("controller") {
		protected void do_perform() {
			controllerPressed();
		}
	};

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
		protected void do_perform() {
		      proxy.setEncoderType(enc_type_cbx.getSelectedIndex());
		}
	};

	/** Checkbox to allow publishing camera images */
	private final JCheckBox publish_chk = new JCheckBox(new IAction(null) {
		protected void do_perform() {
			proxy.setPublish(publish_chk.isSelected());
		}
	});

	/** Create a new camera properties form */
	public CameraProperties(Session s, Camera c) {
		super(I18N.get("camera") + ": ", s, c);
		location = new LocationPanel(s);
	}

	/** Get the SONAR type cache */
	protected TypeCache<Camera> getTypeCache() {
		return state.getCamCache().getCameras();
	}

	/** Initialize the widgets on the form */
	@Override protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add(I18N.get("location"), createLocationPanel());
		tab.add(I18N.get("device.setup"), createSetupPanel());
		add(tab);
		updateAttribute(null);
		if(canUpdate())
			createJobs();
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	@Override protected void dispose() {
		location.dispose();
		super.dispose();
	}

	/** Create the location panel */
	private JPanel createLocationPanel() {
		location.setGeoLoc(proxy.getGeoLoc());
		location.initialize();
		location.addRow(I18N.get("device.notes"), notes_txt);
		location.setCenter();
		location.addRow(new JButton(controller));
		return location;
	}

	/** Controller lookup button pressed */
	private void controllerPressed() {
		Controller c = proxy.getController();
		if(c != null) {
			session.getDesktop().show(
				new ControllerForm(session, c));
		}
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
		notes_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setNotes(notes_txt.getText());
			}
		});
		encoder_txt.addFocusListener(new FocusLostJob(WORKER) {
			@Override public void perform() {
				proxy.setEncoder(encoder_txt.getText());
			}
		});
		enc_chn_spn.addChangeListener(new ChangeJob(WORKER) {
			@Override public void perform() {
				Number c = (Number)enc_chn_spn.getValue();
				proxy.setEncoderChannel(c.intValue());
			}
		});
	}

	/** Update one attribute on the form */
	@Override protected void doUpdateAttribute(String a) {
		if(a == null || a.equals("controller"))
			controller.setEnabled(proxy.getController() != null);
		if(a == null || a.equals("notes")) {
			notes_txt.setEnabled(canUpdate("notes"));
			notes_txt.setText(proxy.getNotes());
		}
		if(a == null || a.equals("encoder")) {
			encoder_txt.setEnabled(canUpdate("encoder"));
			encoder_txt.setText(proxy.getEncoder());
		}
		if(a == null || a.equals("encoderChannel")) {
			enc_chn_spn.setEnabled(canUpdate("encoderChannel"));
			enc_chn_spn.setValue(proxy.getEncoderChannel());
		}
		if(a == null || a.equals("encoderType")) {
			enc_type_cbx.setEnabled(canUpdate("encoderType"));
			enc_type_cbx.setAction(null);
			enc_type_cbx.setSelectedIndex(proxy.getEncoderType());
			enc_type_cbx.setAction(encoder_type);
		}
		if(a == null || a.equals("publish")) {
			publish_chk.setEnabled(canUpdate("publish"));
			publish_chk.setSelected(proxy.getPublish());
		}
	}
}
