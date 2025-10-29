/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2025  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.PlanPhaseHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.StreamPanel;
import us.mn.state.dot.tms.client.camera.VideoRequest;
import static us.mn.state.dot.tms.client.camera.VideoRequest.Size.MEDIUM;
import static us.mn.state.dot.tms.client.camera.VideoRequest.Size.THUMBNAIL;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
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

	/** Max arms to display on dispatcher */
	static private final int MAX_ARMS = 6;

	/** SONAR session */
	private final Session session;

	/** Action plan selection model */
	private final ProxySelectionModel<ActionPlan> sel_mdl;

	/** Gate arm selection model */
	private final ProxySelectionModel<GateArm> ga_sel_mdl;

	/** Name label */
	private final JLabel name_lbl = createValueLabel();

	/** Location label */
	private final JLabel location_lbl = createValueLabel();

	/** First warning DMS */
	private final WarningDms warn_dms_1;

	/** Second warning DMS */
	private final WarningDms warn_dms_2;

	/** Main stream panel */
	private final StreamPanel stream_pnl;

	/** Thumbnail stream panel */
	private final StreamPanel thumb_pnl;

	/** Swap video streams */
	private boolean swap_streams = false;

	/** Action to swap main / thumbnail stream panels */
	private final IAction swap_act = new IAction("gate.arm.stream.swap") {
		protected void doActionPerformed(ActionEvent e) {
			swap_streams = !swap_streams;
			updateCameraStreams();
		}
	};

	/** Gate arms */
	private final GateArm[] gate_arm = new GateArm[MAX_ARMS];

	/** Gate arm labels */
	private final JLabel[] gate_lbl = new JLabel[MAX_ARMS];

	/** Gate arm state labels */
	private final JLabel[] state_lbl = new JLabel[MAX_ARMS];

	/** Interlock labels */
	private final JLabel[] ilock_lbl = new JLabel[MAX_ARMS];

	/** Action to open the gate arm */
	private final IAction open_act = new IAction("gate.arm.open") {
		protected void doActionPerformed(ActionEvent e) {
			setPhase(PlanPhase.GATE_ARM_OPEN);
		}
	};

	/** Action to set gate arm "change" */
	private final IAction change_act = new IAction(
		"gate.arm.change")
	{
		protected void doActionPerformed(ActionEvent e) {
			setPhase(PlanPhase.GATE_ARM_CHANGE);
		}
	};

	/** Action to close the gate arm */
	private final IAction close_act = new IAction("gate.arm.close") {
		protected void doActionPerformed(ActionEvent e) {
			setPhase(PlanPhase.GATE_ARM_CLOSED);
		}
	};

	/** Set action plan phase */
	private void setPhase(String n) {
		ActionPlan ap = selected;
		PlanPhase pp = PlanPhaseHelper.lookup(n);
		if (ap != null && pp != null)
			ap.setPhase(pp);
	}

	/** Proxy watcher */
	private final ProxyWatcher<ActionPlan> watcher;

	/** Action plan view */
	private final ProxyView<ActionPlan> plan_view =
		new ProxyView<ActionPlan>()
	{
		public void enumerationComplete() { }
		public void update(ActionPlan ap, String a) {
			selected = ap;
			updateAttribute(ap, a);
		}
		public void clear() {
			selected = null;
			clearSelected();
		}
	};

	/** Currently selected action plan */
	private ActionPlan selected;

	/** Gate plan selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			watcher.setProxy(sel_mdl.getSingleSelection());
		}
	};

	/** Gate arm selection listener */
	private final ProxySelectionListener ga_sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			GateArm ga = ga_sel_mdl.getSingleSelection();
			if (ga != null) {
				Hashtags tags = new Hashtags(ga.getNotes());
				for (DeviceAction da:
					 DeviceActionHelper.find(tags))
				{
					sel_mdl.setSelected(da.getActionPlan());
					break;
				}
			}
		}
	};

	/** Gate arm proxy listener */
	private final SwingProxyAdapter<GateArm> ga_listener =
		new SwingProxyAdapter<GateArm>()
	{
		@Override
		protected void proxyChangedSwing(GateArm ga, String a) {
			boolean ch = false;
			for (int i = 0; i < MAX_ARMS; i++)
				ch |= (ga == gate_arm[i]);
			if (ch && (null == a || "styles".equals(a)))
				updateGateArms();
		}
	};

	/** Create a new gate arm dispatcher */
	public GateArmDispatcher(Session s) {
		session = s;
		TypeCache<GateArm> c = s.getSonarState().getGateArms();
		c.addProxyListener(ga_listener);
		TypeCache<ActionPlan> cache =
			s.getSonarState().getActionPlans();
		watcher = new ProxyWatcher<ActionPlan>(cache, plan_view, true);
		warn_dms_1 = new WarningDms(session);
		warn_dms_2 = new WarningDms(session);
		sel_mdl = s.getGatePlanManager().getSelectionModel();
		ga_sel_mdl = s.getGateArmManager().getSelectionModel();
		stream_pnl = createStreamPanel(MEDIUM);
		thumb_pnl = createStreamPanel(THUMBNAIL);
		for (int i = 0; i < MAX_ARMS; i++) {
			gate_lbl[i] = new JLabel();
			state_lbl[i] = createValueLabel();
			ilock_lbl[i] = new JLabel();
			// Make labels opaque to allow setting background color
			state_lbl[i].setOpaque(true);
			ilock_lbl[i].setOpaque(true);
		}
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("gate.arm.selected"));
		add("action.plan.name");
		add(name_lbl);
		add("location");
		add(location_lbl, Stretch.LAST);
		add(createStreamsBox(), Stretch.FULL);
		add(gate_lbl[0], Stretch.NONE);
		add(state_lbl[0], Stretch.NONE);
		add(ilock_lbl[0]);
		add(gate_lbl[3], Stretch.NONE);
		add(state_lbl[3], Stretch.NONE);
		add(ilock_lbl[3]);
		add(new JButton(open_act), Stretch.LAST);
		add(gate_lbl[1], Stretch.NONE);
		add(state_lbl[1], Stretch.NONE);
		add(ilock_lbl[1]);
		add(gate_lbl[4], Stretch.NONE);
		add(state_lbl[4], Stretch.NONE);
		add(ilock_lbl[4]);
		add(new JButton(change_act), Stretch.LAST);
		add(gate_lbl[2], Stretch.NONE);
		add(state_lbl[2], Stretch.NONE);
		add(ilock_lbl[2]);
		add(gate_lbl[5], Stretch.NONE);
		add(state_lbl[5], Stretch.NONE);
		add(ilock_lbl[5]);
		add(new JButton(close_act), Stretch.LAST);
		watcher.initialize();
		warn_dms_1.initialize();
		warn_dms_2.initialize();
		clearSelected();
		sel_mdl.addProxySelectionListener(sel_listener);
		ga_sel_mdl.addProxySelectionListener(ga_sel_listener);
	}

	/** Create streams box */
	private Box createStreamsBox() {
		Box vb = Box.createVerticalBox();
		vb.add(Box.createVerticalGlue());
		vb.add(warn_dms_1.pix_pnl);
		vb.add(Box.createVerticalStrut(UI.hgap));
		vb.add(warn_dms_2.pix_pnl);
		vb.add(Box.createVerticalStrut(UI.hgap));
		vb.add(thumb_pnl);
		vb.add(Box.createVerticalStrut(UI.hgap));
		vb.add(new JButton(swap_act));
		vb.add(Box.createVerticalGlue());
		Box b = Box.createHorizontalBox();
		b.add(stream_pnl);
		b.add(Box.createHorizontalStrut(UI.hgap));
		b.add(vb);
		return b;
	}

	/** Create a stream panel */
	private StreamPanel createStreamPanel(VideoRequest.Size sz) {
		VideoRequest vr = new VideoRequest(session.getProperties(), sz);
		vr.setSonarSessionId(session.getSessionId());
		return new StreamPanel(vr);
	}

	/** Dispose of the dispatcher */
	@Override
	public void dispose() {
		warn_dms_1.dispose();
		warn_dms_2.dispose();
		watcher.dispose();
		sel_mdl.removeProxySelectionListener(sel_listener);
		ga_sel_mdl.removeProxySelectionListener(ga_sel_listener);
		stream_pnl.dispose();
		thumb_pnl.dispose();
		clearSelected();
		super.dispose();
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param ap The newly selected action plan.  May not be null.
	 * @param a Attribute to update, null for all attributes. */
	private void updateAttribute(ActionPlan ap, String a) {
		if (null == a) {
			swap_streams = false;
			updateActionPlan(ap);
		}
		if (null == a || a.equals("phase"))
			updateButtons(ap);
	}

	/** Update the action plan */
	private void updateActionPlan(ActionPlan ap) {
		name_lbl.setText(ap.getName());
		TreeSet<GateArm> arms = ActionPlanHelper.findGateArms(ap);
		gate_arm[0] = arms.pollFirst();
		gate_arm[1] = arms.pollFirst();
		gate_arm[2] = arms.pollFirst();
		gate_arm[3] = arms.pollFirst();
		gate_arm[4] = arms.pollFirst();
		gate_arm[5] = arms.pollFirst();
		updateGateArms();
		GateArm ga = gate_arm[0];
		GeoLoc loc = (ga != null) ? ga.getGeoLoc() : null;
		location_lbl.setText(
			(loc != null) ? GeoLocHelper.getLocation(loc) : " "
		);
		TreeSet<DMS> signs = ActionPlanHelper.findDms(ap);
		warn_dms_1.setSelected(signs.pollFirst());
		warn_dms_2.setSelected(signs.pollFirst());
		updateCameraStreams();
	}

	/** Update camera streams */
	private void updateCameraStreams() {
		Camera c0 = null;
		Camera c1 = null;
		for (int i = 0; i < 6; i++) {
			GateArm ga = gate_arm[i];
			if (ga != null) {
				CameraPreset cp = ga.getPreset();
				if (cp != null) {
					Camera c = cp.getCamera();
					if (c != null) {
						if (c0 == null)
							c0 = c;
						else {
							c1 = c;
							break;
						}
					}
				}
			}
		}
		if (swap_streams) {
			stream_pnl.setCamera(c1);
			thumb_pnl.setCamera(c0);
		} else {
			stream_pnl.setCamera(c0);
			thumb_pnl.setCamera(c1);
		}
		swap_act.setEnabled(c0 != null && c1 != null);
	}

	/** Update the button enabled states */
	private void updateButtons(ActionPlan ap) {
		boolean e = session.isWritePermitted(ap, "phase");
		boolean has_signs = ActionPlanHelper.countDms(ap) > 0;
		PlanPhase pp = ap.getPhase();
		boolean is_open = (pp != null) &&
			PlanPhase.GATE_ARM_OPEN.equals(pp.getName());
		boolean is_change = (pp != null) &&
			PlanPhase.GATE_ARM_CHANGE.equals(pp.getName());
		boolean is_closed = (pp != null) &&
			PlanPhase.GATE_ARM_CLOSED.equals(pp.getName());
		open_act.setEnabled(e &&
			(is_change || (is_closed && !has_signs)));
		change_act.setEnabled(e &&
			(has_signs && (is_open || is_closed)));
		close_act.setEnabled(e &&
			(is_change || (is_open && !has_signs)));
	}

	/** Clear selected action plan */
	private void clearSelected() {
		swap_act.setEnabled(false);
		open_act.setEnabled(false);
		change_act.setEnabled(false);
		close_act.setEnabled(false);
		swap_streams = false;
		stream_pnl.setCamera(null);
		thumb_pnl.setCamera(null);
		name_lbl.setText("");
		location_lbl.setText("");
		warn_dms_1.setSelected(null);
		warn_dms_2.setSelected(null);
		for (int i = 0; i < MAX_ARMS; i++)
			gate_arm[i] = null;
		updateGateArms();
	}

	/** Update gate arm widgets */
	private void updateGateArms() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArm ga = gate_arm[i];
			StateStyle ss = new StateStyle(ga);
			state_lbl[i].setForeground(ss.foreground());
			state_lbl[i].setBackground(ss.background());
			state_lbl[i].setText(ss.text());
			if (ga != null) {
				gate_lbl[i].setText(ga.getName());
				InterlockStyle st = new InterlockStyle(
					ga.getInterlock());
				ilock_lbl[i].setForeground(st.foreground());
				ilock_lbl[i].setBackground(st.background());
				ilock_lbl[i].setBorder(BorderFactory
					.createLineBorder(Color.BLACK));
				ilock_lbl[i].setText(st.text());
			} else {
				gate_lbl[i].setText(" ");
				ilock_lbl[i].setForeground(null);
				ilock_lbl[i].setBackground(null);
				ilock_lbl[i].setBorder(null);
				ilock_lbl[i].setText(" ");
			}
		}
	}
}
