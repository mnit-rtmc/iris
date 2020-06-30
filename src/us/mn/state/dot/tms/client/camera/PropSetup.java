/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2020  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
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
	private final IAction enc_type_act = new IAction("encoder.type") {
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

	/** Encoder address */
	private final JTextField enc_address_txt = new JTextField("", 32);

	/** Encoder port text */
	private final JTextField enc_port_txt = new JTextField("", 8);

	/** Encoder multicast address */
	private final JTextField enc_mcast_txt = new JTextField("", 32);

	/** Model for encoder channel spinner */
	private final SpinnerNumberModel num_model =
		new SpinnerNumberModel(0, 0, 16, 1);

	/** Encoder channel spinner */
	private final JSpinner enc_chn_spn = new JSpinner(num_model);

	/** Checkbox to allow publishing camera images */
	private final JCheckBox publish_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			camera.setPublish(publish_chk.isSelected());
		}
	});
	
	/** Camera Template ComboBox */
	private final JComboBox<CameraTemplate> cam_tmplt_cbx = 
			new JComboBox<CameraTemplate>();
	
	/** Camera template action */
	private final IAction cam_tmplt_act = new IAction("camera.template"){
		protected void doActionPerformed(ActionEvent e) {
			CameraTemplate ct = getSelectedCameraTemplate();
		    camera.setCameraTemplate(ct);
		}
		@Override
		protected void doUpdateSelected() {
			cam_tmplt_cbx.setSelectedItem(camera.getCameraTemplate());
		}
	};
	
	/** Get the selected camera template */
	private CameraTemplate getSelectedCameraTemplate() {
		Object ct = cam_tmplt_cbx.getSelectedItem();
		return (ct instanceof CameraTemplate) ? (CameraTemplate) ct : null;
	}
	

	/** Checkbox to allow streaming camera images */
	private final JCheckBox streamable_chk = new JCheckBox(new IAction(null)
	{
		protected void doActionPerformed(ActionEvent e) {
			camera.setStreamable(streamable_chk.isSelected());
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
		CamCache cc = session.getSonarState().getCamCache();
		enc_type_cbx.setModel(new IComboBoxModel<EncoderType>(
				cc.getEncoderTypeModel()));
		enc_type_cbx.setAction(enc_type_act);
		enc_type_cbx.setRenderer(new EncoderTypeRenderer());
		cam_tmplt_cbx.setModel(new IComboBoxModel<CameraTemplate>(
				cc.getCameraTemplateModel()));
		cam_tmplt_cbx.setRenderer(new CameraTemplateRenderer());
		cam_tmplt_cbx.setAction(cam_tmplt_act);
		add("camera.num");
		add(cam_num_txt, Stretch.LAST);
		add("encoder.type");
		add(enc_type_cbx, Stretch.LAST);
		add("camera.enc_address");
		add(enc_address_txt, Stretch.LAST);
		add("camera.enc_port");
		add(enc_port_txt, Stretch.LAST);
		add("camera.enc_mcast");
		add(enc_mcast_txt, Stretch.LAST);
		add("camera.enc_channel");
		add(enc_chn_spn, Stretch.LAST);
		add("camera.template");
		add(cam_tmplt_cbx, Stretch.LAST);
		add("camera.publish");
		add(publish_chk, Stretch.LAST);
		add("camera.streamable");
		add(streamable_chk, Stretch.LAST);
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
		enc_address_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    String a = enc_address_txt.getText().trim();
			    camera.setEncAddress((a.length() > 0) ? a : null);
			}
		});
		enc_port_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    Integer ep = parseInt(enc_port_txt.getText());
			    enc_port_txt.setText((ep != null)
			                        ? ep.toString()
			                        : "");
			    camera.setEncPort(ep);
			}
		});
		enc_mcast_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			    String m = enc_mcast_txt.getText().trim();
			    camera.setEncMcast((m.length() > 0) ? m : null);
			}
		});
		enc_chn_spn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			    Number c = (Number) enc_chn_spn.getValue();
			    int ch = c.intValue();
			    if (ch > 0 && ch <= 16)
				camera.setEncChannel(ch);
			    else
				camera.setEncChannel(null);
			}
		});
	}
	
	/** Update the edit mode */
	public void updateEditMode() {
		cam_num_txt.setEnabled(canWrite("camNum"));
		enc_type_act.setEnabled(canWrite("encoderType"));
		enc_address_txt.setEnabled(canWrite("encAddress"));
		enc_port_txt.setEnabled(canWrite("encPort"));
		enc_mcast_txt.setEnabled(canWrite("encMcast"));
		enc_chn_spn.setEnabled(canWrite("encChannel"));
		publish_chk.setEnabled(canWrite("publish"));
		streamable_chk.setEnabled(canWrite("streamable"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("camNum")) {
			Integer cn = camera.getCamNum();
			cam_num_txt.setText((cn != null) ? cn.toString() : "");
		}
		if (a == null || a.equals("encoderType"))
			enc_type_act.updateSelected();
		if (a == null || a.equals("encAddress")) {
			String ep = camera.getEncAddress();
			enc_address_txt.setText((ep != null) ? ep : "");
		}
		if (a == null || a.equals("encPort")) {
			Integer ep = camera.getEncPort();
			enc_port_txt.setText((ep != null) ? ep.toString() : "");
		}
		if (a == null || a.equals("encMcast")) {
			String em = camera.getEncMcast();
			enc_mcast_txt.setText((em != null) ? em : "");
		}
		if (a == null || a.equals("encChannel")) {
			Integer ch = camera.getEncChannel();
			enc_chn_spn.setValue((ch != null) ? ch : 0);
		}
		if (a == null || a.equals("publish"))
			publish_chk.setSelected(camera.getPublish());
		if (a == null || a.equals("streamable"))
			streamable_chk.setSelected(camera.getStreamable());
		if (a == null || a.equals("cameraTemplate"))
			cam_tmplt_act.updateSelected();
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(camera, aname);
	}
}
