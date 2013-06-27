/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.awt.Color;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.camera.CameraPTZ;
import us.mn.state.dot.tms.client.camera.StreamPanel;
import us.mn.state.dot.tms.client.camera.VideoRequest;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GateArmDispatcher is a GUI component for deploying gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmDispatcher extends IPanel {

	/** Video size */
	static private final VideoRequest.Size SIZE = VideoRequest.Size.MEDIUM;

	/** SONAR session */
	private final Session session;

	/** Cache of gate arm proxy objects */
	private final TypeCache<GateArm> cache;

	/** Selection model */
	private final ProxySelectionModel<GateArm> sel_model;

	/** Proxy listener */
	private final ProxyListener<GateArm> p_listener =
		new ProxyListener<GateArm>()
	{
		public void proxyAdded(GateArm ga) { }
		public void enumerationComplete() { }
		public void proxyRemoved(GateArm ga) { }
		public void proxyChanged(final GateArm ga, final String a) {
			if(ga == watching) {
				runSwing(new Runnable() {
					public void run() {
						updateAttribute(ga, a);
					}
				});
			}
		}
	};

	/** Name label */
	private final JLabel name_lbl = createValueLabel();

	/** Location label */
	private final JLabel location_lbl = createValueLabel();

	/** Verification video panel */
	private final StreamPanel verify_pnl;

	/** Verify PTZ control */
	private final CameraPTZ verify_ptz;

	/** Status component */
	private final JLabel status_lbl = IPanel.createValueLabel();

	/** Operation description label */
	private final JLabel op_lbl = IPanel.createValueLabel();

	/** Arm state label */
	private final JLabel arm_state_lbl = IPanel.createValueLabel();

	/** Interlock label */
	private final JLabel interlock_lbl = new JLabel();

	/** Action to open the gate arm */
	private final IAction open_arm = new IAction("gate.arm.open") {
		protected void do_perform() {
			requestState(GateArmState.OPENING);
		}
	};

	/** Action to warn before closing gate arm */
	private final IAction warn_close_arm = new IAction(
		"gate.arm.warn.close")
	{
		protected void do_perform() {
			requestState(GateArmState.WARN_CLOSE);
		}
	};

	/** Action to close the gate arm */
	private final IAction close_arm = new IAction("gate.arm.close") {
		protected void do_perform() {
			requestState(GateArmState.CLOSING);
		}
	};

	/** Request a gate arm state change */
	private void requestState(GateArmState gas) {
		GateArm ga = watching;
		if(ga != null) {
			ga.setOwnerNext(session.getUser());
			ga.setArmStateNext(gas.ordinal());
		}
	}

	/** Approach video panel */
	private final StreamPanel approach_pnl;

	/** Approach PTZ control */
	private final CameraPTZ approach_ptz;

	/** Currently selected gate arm.  This will be null if there are zero or
	 * multiple devices selected. */
	private GateArm watching;

	/** Watch a gate arm */
	private void watch(final GateArm nw) {
		final GateArm ow = watching;
		if(ow != null)
			cache.ignoreObject(ow);
		watching = nw;
		if(nw != null)
			cache.watchObject(nw);
	}

	/** Selection listener */
	private final ProxySelectionListener<GateArm> sel_listener =
		new ProxySelectionListener<GateArm>()
	{
		public void selectionAdded(GateArm ga) {
			if(sel_model.getSelectedCount() <= 1)
				setSelected(ga);
		}
		public void selectionRemoved(GateArm ga) {
			if(sel_model.getSelectedCount() == 1) {
				for(GateArm g: sel_model.getSelected())
					setSelected(g);
			} else if(ga == watching)
				setSelected(null);
		}
	};

	/** Create a new gate arm dispatcher */
	public GateArmDispatcher(Session s, GateArmManager manager) {
		session = s;
		cache = s.getSonarState().getGateArms();
		cache.addProxyListener(p_listener);
		sel_model = manager.getSelectionModel();
		sel_model.addProxySelectionListener(sel_listener);
		verify_ptz = new CameraPTZ(s);
		verify_pnl = createStreamPanel(verify_ptz);
		approach_ptz = new CameraPTZ(s);
		approach_pnl = createStreamPanel(approach_ptz);
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		interlock_lbl.setOpaque(true);
		setTitle(I18N.get("gate.arm.selected"));
		add("device.name");
		add(name_lbl);
		add("location");
		add(location_lbl, Stretch.LAST);
		add(verify_pnl, Stretch.FULL);
		add("device.operation");
		add(op_lbl, Stretch.LAST);
		add("device.status");
		add(status_lbl);
		add(interlock_lbl, Stretch.RIGHT);
		add("gate.arm.state");
		add(arm_state_lbl);
		add(buildButtonBox(), Stretch.RIGHT);
		add(approach_pnl, Stretch.FULL);
		clear();
	}

	/** Create a stream panel */
	private StreamPanel createStreamPanel(CameraPTZ ptz) {
		VideoRequest vr = new VideoRequest(session.getProperties(),
			SIZE);
		SonarState st = session.getSonarState();
		Connection c = st.lookupConnection(st.getConnection());
		vr.setSonarSessionId(c.getSessionId());
		vr.setRate(30);
		return new StreamPanel(ptz, vr);
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box box = Box.createHorizontalBox();
		box.add(new JButton(open_arm));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(warn_close_arm));
		box.add(Box.createHorizontalStrut(UI.hgap));
		box.add(new JButton(close_arm));
		return box;
	}

	/** Dispose of the dispatcher */
	public void dispose() {
		cache.removeProxyListener(p_listener);
		sel_model.removeProxySelectionListener(sel_listener);
		removeAll();
		verify_pnl.dispose();
		verify_ptz.setCamera(null);
		approach_pnl.dispose();
		approach_ptz.setCamera(null);
		setSelected(null);
	}

	/** Set the selected gate arm */
	private void setSelected(final GateArm ga) {
		final GateArm p = watching;
		watch(ga);
		if(ga != p) {
			if(ga != null)
				updateAttribute(ga, null);
			else
				clear();
		}
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param ga The newly selected gate arm.  May not be null.
	 * @param a Attribute to update, null for all attributes. */
	private void updateAttribute(GateArm ga, String a) {
		if(a == null || a.equals("name"))
			name_lbl.setText(ga.getName());
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getDescription(
				ga.getGeoLoc()));
		}
		if(a == null || a.equals("camera")) {
			Camera c = ga.getCamera();
			verify_ptz.setCamera(c);
			verify_pnl.setCamera(c);
		}
		if(a == null || a.equals("operation"))
			updateStatus(ga);
		if(a == null || a.equals("armState")) {
			updateStatus(ga);
			arm_state_lbl.setText(GateArmState.fromOrdinal(
				ga.getArmState()).toString());
			updateButtons(ga);
		}
		if(a == null || a.equals("interlock")) {
			if(ga.getInterlock()) {
				interlock_lbl.setForeground(Color.WHITE);
				interlock_lbl.setBackground(Color.RED);
				interlock_lbl.setText(I18N.get(
					"gate.arm.interlock.on"));
			} else {
				interlock_lbl.setForeground(Color.WHITE);
				interlock_lbl.setBackground(Color.GREEN);
				interlock_lbl.setText(I18N.get(
					"gate.arm.interlock.off"));
			}
			updateButtons(ga);
		}
		if(a == null || a.equals("approach")) {
			Camera c = ga.getApproach();
			approach_ptz.setCamera(c);
			approach_pnl.setCamera(c);
		}
	}

	/** Update the status widgets */
	private void updateStatus(GateArm ga) {
		if(ControllerHelper.isFailed(ga.getController())) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status_lbl.setText(getStatus(ga));
		} else
			updateCritical(ga);
		op_lbl.setText(ga.getOperation());
	}

	/** Get gate arm controller communication status */
	static private String getStatus(GateArm ga) {
		return ControllerHelper.getStatus(ga.getController());
	}

	/** Update the critical error status */
	private void updateCritical(GateArm ga) {
		String critical = getStatus(ga);
		if(critical.isEmpty())
			updateMaintenance(ga);
		else {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
			status_lbl.setText(critical);
		}
	}

	/** Update the maintenance error status */
	private void updateMaintenance(GateArm ga) {
		String m = ControllerHelper.getMaintenance(ga.getController());
		if(m.isEmpty()) {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
			status_lbl.setText("");
		} else {
			status_lbl.setForeground(Color.BLACK);
			status_lbl.setBackground(Color.YELLOW);
			status_lbl.setText(m);
		}
	}

	/** Update the button enabled states */
	private void updateButtons(GateArm ga) {
		boolean e = session.canUpdate(ga, "armState");
		GateArmState gas = GateArmState.fromOrdinal(ga.getArmState());
		open_arm.setEnabled(e && gas == GateArmState.CLOSED &&
			!ga.getInterlock());
		warn_close_arm.setEnabled(e && gas == GateArmState.OPEN);
		close_arm.setEnabled(e && gas == GateArmState.WARN_CLOSE);
	}

	/** Clear all of the fields */
	private void clear() {
		name_lbl.setText("");
		location_lbl.setText("");
		verify_pnl.setCamera(null);
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		op_lbl.setText("");
		arm_state_lbl.setText("");
		interlock_lbl.setText("");
		interlock_lbl.setForeground(null);
		interlock_lbl.setBackground(null);
		open_arm.setEnabled(false);
		warn_close_arm.setEnabled(false);
		close_arm.setEnabled(false);
		approach_pnl.setCamera(null);
	}
}
