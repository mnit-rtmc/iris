/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
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
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.StreamType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;

/**
 * Camera properties setup panel.
 *
 * @author Douglas Lau
 */
public class PropSetup extends IPanel {

	/** Parse an integer */
	static private Integer parseInt(String t) {
		try {
			return Integer.parseInt(t);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Camera number text */
	private final JTextField cam_num_txt = new JTextField("", 8);

	/** Encoder type combobox */
	private final JComboBox<EncoderType> enc_type_cbx =
		new JComboBox<EncoderType>();

	/** Encoder type action */
	private final IAction enc_type_act = new IAction("camera.encoder.type"){
		protected void doActionPerformed(ActionEvent e) {
		      camera.setEncoderType(getSelectedEncoderType());
		}
		@Override
		protected void doUpdateSelected() {
			enc_type_cbx.setSelectedItem(camera.getEncoderType());
		}
	};

	/** Get the selected encoder type */
	private EncoderType getSelectedEncoderType() {
		Object et = enc_type_cbx.getSelectedItem();
		return (et instanceof EncoderType) ? (EncoderType) et : null;
	}

	/** Encoder stream URI */
	private final JTextField encoder_txt = new JTextField("", 32);

	/** Encoder multicast URI */
	private final JTextField enc_mcast_txt = new JTextField("", 32);

	/** Model for encoder channel spinner */
	private final SpinnerNumberModel num_model =
		new SpinnerNumberModel(1, 0, 10, 1);

	/** Encoder channel spinner */
	private final JSpinner enc_chn_spn = new JSpinner(num_model);

	/** Stream type combobox */
	private final JComboBox<StreamType> str_type_cbx =
		new JComboBox<StreamType>(StreamType.values());

	/** Stream type action */
	private final IAction str_type_act = new IAction("camera.stream.type") {
		protected void doActionPerformed(ActionEvent e) {
		      camera.setStreamType(str_type_cbx.getSelectedIndex());
		}
		@Override
		protected void doUpdateSelected() {
			str_type_cbx.setSelectedIndex(camera.getStreamType());
		}
	};

	/** Checkbox to allow publishing camera images */
	private final JCheckBox publish_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			camera.setPublish(publish_chk.isSelected());
		}
	});

	/** User session */
	private final Session session;

	/** Camera proxy */
	private final Camera camera;

	/** Create a new camera properties setup panel */
	public PropSetup(Session s, Camera c) {
		session = s;
		camera = c;
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		enc_type_cbx.setModel(new IComboBoxModel<EncoderType>(session
			.getSonarState().getCamCache().getEncoderTypeModel()));
		enc_type_cbx.setAction(enc_type_act);
		str_type_cbx.setAction(str_type_act);
		add("camera.num");
		add(cam_num_txt, Stretch.LAST);
		add("camera.encoder.type");
		add(enc_type_cbx, Stretch.LAST);
		add("camera.encoder");
		add(encoder_txt, Stretch.LAST);
		add("camera.enc_mcast");
		add(enc_mcast_txt, Stretch.LAST);
		add("camera.encoder.note", Stretch.END);
		add("camera.encoder.channel");
		add(enc_chn_spn, Stretch.LAST);
		add("camera.stream.type");
		add(str_type_cbx, Stretch.LAST);
		add("camera.publish");
		add(publish_chk, Stretch.LAST);
		createJobs();
	}

	/** Create jobs */
	private void createJobs() {
		cam_num_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    Integer cn = parseInt(cam_num_txt.getText());
			    cam_num_txt.setText((cn != null)
			                        ? cn.toString()
			                        : "");
			    camera.setCamNum(cn);
			}
		});
		encoder_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    camera.setEncoder(encoder_txt.getText());
			}
		});
		enc_mcast_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    camera.setEncMulticast(enc_mcast_txt.getText());
			}
		});
		enc_chn_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    Number c = (Number)enc_chn_spn.getValue();
			    camera.setEncoderChannel(c.intValue());
			}
		});
	}

	/** Update the edit mode */
	public void updateEditMode() {
		cam_num_txt.setEnabled(canUpdate("camNum"));
		enc_type_act.setEnabled(canUpdate("encoderType"));
		encoder_txt.setEnabled(canUpdate("encoder"));
		enc_mcast_txt.setEnabled(canUpdate("encMulticast"));
		enc_chn_spn.setEnabled(canUpdate("encoderChannel"));
		str_type_act.setEnabled(canUpdate("streamType"));
		publish_chk.setEnabled(canUpdate("publish"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("camNum")) {
			Integer cn = camera.getCamNum();
			cam_num_txt.setText((cn != null) ? cn.toString() : "");
		}
		if (a == null || a.equals("encoderType"))
			enc_type_act.updateSelected();
		if (a == null || a.equals("encoder"))
			encoder_txt.setText(camera.getEncoder());
		if (a == null || a.equals("encMulticast"))
			enc_mcast_txt.setText(camera.getEncMulticast());
		if (a == null || a.equals("encoderChannel"))
			enc_chn_spn.setValue(camera.getEncoderChannel());
		if (a == null || a.equals("streamType"))
			str_type_act.updateSelected();
		if (a == null || a.equals("publish"))
			publish_chk.setSelected(camera.getPublish());
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(camera, aname);
	}
}
