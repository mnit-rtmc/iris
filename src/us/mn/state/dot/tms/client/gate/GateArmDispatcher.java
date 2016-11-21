/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2016  Minnesota Department of Transportation
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import static us.mn.state.dot.tms.GateArmArray.MAX_ARMS;
import us.mn.state.dot.tms.GateArmHelper;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.StreamPanel;
import us.mn.state.dot.tms.client.camera.VideoRequest;
import static us.mn.state.dot.tms.client.camera.VideoRequest.Size.MEDIUM;
import static us.mn.state.dot.tms.client.camera.VideoRequest.Size.THUMBNAIL;
import us.mn.state.dot.tms.client.dms.DMSPanelPager;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * GateArmDispatcher is a GUI component for deploying gate arms.
 *
 * @author Douglas Lau
 */
public class GateArmDispatcher extends IPanel
	implements ProxyView<GateArmArray>
{
	/** Get the filter color for a DMS */
	static private Color filterColor(DMS dms) {
		return dms != null ? SignPixelPanel.filterColor(dms) : null;
	}

	/** SONAR session */
	private final Session session;

	/** Selection model */
	private final ProxySelectionModel<GateArmArray> sel_mdl;

	/** DMS Proxy view */
	private final ProxyView<DMS> dms_view = new ProxyView<DMS>() {
		public void update(DMS d, String a) {
			if (a == null ||
			    "styles".equals(a) ||
			    "msgCurrent".equals(a))
				updateDms(d);
		}
		public void clear() {
			clearDms();
		}
	};

	/** Name label */
	private final JLabel name_lbl = createValueLabel();

	/** Location label */
	private final JLabel location_lbl = createValueLabel();

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
			GateArmArray ga = ga_array;
			if(ga != null) {
				updateCameraStream(ga);
				updateApproachStream(ga);
			}
		}
	};

	/** Sign pixel panel */
	private final SignPixelPanel current_pnl = new SignPixelPanel(80, 132,
		true);

	/** DMS panel pager */
	private DMSPanelPager dms_pager;

	/** Gate arm labels */
	private final JLabel[] gate_lbl = new JLabel[MAX_ARMS];

	/** Gate arm state labels */
	private final JLabel[] state_lbl = new JLabel[MAX_ARMS];

	/** Array Arm state label */
	private final JLabel arm_state_lbl = createValueLabel();

	/** Interlock label */
	private final JLabel interlock_lbl = new JLabel();

	/** Action to open the gate arm */
	private final IAction open_arm = new IAction("gate.arm.open") {
		protected void doActionPerformed(ActionEvent e) {
			requestState(GateArmState.OPENING);
		}
	};

	/** Action to warn before closing gate arm */
	private final IAction warn_close_arm = new IAction(
		"gate.arm.warn.close")
	{
		protected void doActionPerformed(ActionEvent e) {
			requestState(GateArmState.WARN_CLOSE);
		}
	};

	/** Action to close the gate arm */
	private final IAction close_arm = new IAction("gate.arm.close") {
		protected void doActionPerformed(ActionEvent e) {
			requestState(GateArmState.CLOSING);
		}
	};

	/** Request a gate arm state change */
	private void requestState(GateArmState gas) {
		GateArmArray ga = ga_array;
		if(ga != null) {
			ga.setOwnerNext(session.getUser());
			ga.setArmStateNext(gas.ordinal());
		}
	}

	/** Proxy watcher */
	private final ProxyWatcher<GateArmArray> watcher;

	/** Currently selected gate arm array */
	private GateArmArray ga_array;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			setSelected(sel_mdl.getSingleSelection());
		}
	};

	/** DMS Proxy watcher */
	private final ProxyWatcher<DMS> dms_watcher;

	/** Currently selected DMS */
	private DMS dms;

	/** Create a new gate arm dispatcher */
	public GateArmDispatcher(Session s, GateArmArrayManager manager) {
		session = s;
		TypeCache<GateArmArray> cache =
			s.getSonarState().getGateArmArrays();
		watcher = new ProxyWatcher<GateArmArray>(cache, this, true);
		TypeCache<DMS> dms_cache =
			s.getSonarState().getDmsCache().getDMSs();
		dms_watcher = new ProxyWatcher<DMS>(dms_cache, dms_view, false);
		sel_mdl = manager.getSelectionModel();
		stream_pnl = createStreamPanel(MEDIUM);
		thumb_pnl = createStreamPanel(THUMBNAIL);
		for(int i = 0; i < MAX_ARMS; i++) {
			gate_lbl[i] = new JLabel();
			state_lbl[i] = createValueLabel();
			// Make label opaque to allow setting background color
			state_lbl[i].setOpaque(true);
		}
		arm_state_lbl.setOpaque(true);
		interlock_lbl.setOpaque(true);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		interlock_lbl.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLACK),
			UI.panelBorder()));
		setTitle(I18N.get("gate.arm.selected"));
		add("device.name");
		add(name_lbl);
		add("location");
		add(location_lbl, Stretch.LAST);
		add(createStreamsBox(), Stretch.FULL);
		add(gate_lbl[0], Stretch.NONE);
		add(state_lbl[0]);
		add(gate_lbl[4], Stretch.NONE);
		add(state_lbl[4]);
		add("gate.arm.array.state");
		add(interlock_lbl, Stretch.TALL);
		add(gate_lbl[1], Stretch.NONE);
		add(state_lbl[1]);
		add(gate_lbl[5], Stretch.NONE);
		add(state_lbl[5]);
		add(arm_state_lbl, Stretch.NONE);
		add(new JLabel(), Stretch.LAST);
		add(gate_lbl[2], Stretch.NONE);
		add(state_lbl[2]);
		add(gate_lbl[6], Stretch.NONE);
		add(state_lbl[6]);
		add(buildButtonBox(), Stretch.TALL);
		add(gate_lbl[3], Stretch.NONE);
		add(state_lbl[3]);
		add(gate_lbl[7], Stretch.NONE);
		add(state_lbl[7], Stretch.LAST);
		stream_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectStream(swap_streams);
			}
		});
		thumb_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectStream(!swap_streams);
			}
		});
		current_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectDms();
			}
		});
		watcher.initialize();
		dms_watcher.initialize();
		clear();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Select a camera stream */
	private void selectStream(boolean swap) {
		GateArmArray ga = ga_array;
		if(ga != null) {
			Camera c = swap ? ga.getApproach() : ga.getCamera();
			if(c != null)
				selectCamera(c);
		}
	}

	/** Select a camera */
	private void selectCamera(Camera c) {
		session.getCameraManager().getSelectionModel().setSelected(c);
	}

	/** Select the DMS */
	private void selectDms() {
		DMS d = dms;
		if(d != null) {
			dms_watcher.setProxy(null);
			session.getDMSManager().getSelectionModel().
				setSelected(d);
		}
	}

	/** Create streams box */
	private Box createStreamsBox() {
		Box vb = Box.createVerticalBox();
		vb.add(Box.createVerticalGlue());
		vb.add(current_pnl);
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
	@Override
	public void dispose() {
		dms_watcher.dispose();
		watcher.dispose();
		setPager(null);
		sel_mdl.removeProxySelectionListener(sel_listener);
		stream_pnl.dispose();
		thumb_pnl.dispose();
		clear();
		super.dispose();
	}

	/** Set the selected gate arm array */
	public void setSelected(GateArmArray ga) {
		watcher.setProxy(ga);
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param ga The newly selected gate arm.  May not be null.
	 * @param a Attribute to update, null for all attributes. */
	@Override
	public void update(GateArmArray ga, String a) {
		ga_array = ga;
		if(a == null)
			swap_streams = false;
		if(a == null || a.equals("name"))
			name_lbl.setText(ga.getName());
		if(a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getDescription(
				ga.getGeoLoc()));
		}
		if(a == null || a.equals("camera"))
			updateCameraStream(ga);
		if(a == null || a.equals("approach"))
			updateApproachStream(ga);
		if(a == null || a.equals("dms"))
			dms_watcher.setProxy(ga.getDms());
		if(a == null || a.equals("camera") || a.equals("approach"))
			updateSwapButton(ga);
		if(a == null || a.equals("styles")) {
			if(ItemStyle.FAILED.checkBit(ga.getStyles())) {
				arm_state_lbl.setForeground(Color.WHITE);
				arm_state_lbl.setBackground(Color.GRAY);
			} else {
				arm_state_lbl.setForeground(Color.BLACK);
				arm_state_lbl.setBackground(Color.WHITE);
			}
			updateGateArms(ga);
		}
		if(a == null || a.equals("armState")) {
			arm_state_lbl.setText(" " + GateArmState.fromOrdinal(
				ga.getArmState()).toString() + " ");
		}
		if(a == null || a.equals("interlock"))
			updateInterlock(ga);
		if(a == null || a.equals("armState") || a.equals("interlock"))
			updateButtons(ga);
	}

	/** Update camera stream */
	private void updateCameraStream(GateArmArray ga) {
		Camera c = ga.getCamera();
		if(swap_streams)
			thumb_pnl.setCamera(c);
		else
			stream_pnl.setCamera(c);
	}

	/** Update approach stream */
	private void updateApproachStream(GateArmArray ga) {
		Camera c = ga.getApproach();
		if(swap_streams)
			stream_pnl.setCamera(c);
		else
			thumb_pnl.setCamera(c);
	}

	/** Update the DMS */
	private void updateDms(DMS d) {
		dms = d;
		current_pnl.setFilterColor(filterColor(d));
		RasterGraphic[] rg = DMSHelper.getRasters(d);
		if(rg != null) {
			String ms = DMSHelper.getMultiString(d);
			current_pnl.setDimensions(d);
			setPager(new DMSPanelPager(current_pnl, rg, ms));
		} else
			clearDms();
	}

	/** Clear the DMS */
	private void clearDms() {
		setPager(null);
		current_pnl.setFilterColor(null);
		current_pnl.clear();
	}

	/** Set the DMS panel pager */
	private void setPager(DMSPanelPager p) {
		DMSPanelPager op = dms_pager;
		if(op != null)
			op.dispose();
		dms_pager = p;
	}

	/** Update the interlock label */
	private void updateInterlock(GateArmArray ga) {
		switch(GateArmInterlock.fromOrdinal(ga.getInterlock())) {
		case NONE:
			interlock_lbl.setForeground(Color.BLACK);
			interlock_lbl.setBackground(Color.GREEN);
			interlock_lbl.setText(I18N.get(
				"gate.arm.interlock.none"));
			break;
		case DENY_OPEN:
			interlock_lbl.setForeground(Color.WHITE);
			interlock_lbl.setBackground(Color.RED);
			interlock_lbl.setText(I18N.get(
				"gate.arm.interlock.deny_open"));
			break;
		case DENY_CLOSE:
			interlock_lbl.setForeground(Color.BLACK);
			interlock_lbl.setBackground(Color.YELLOW);
			interlock_lbl.setText(I18N.get(
				"gate.arm.interlock.deny_close"));
			break;
		case DENY_ALL:
			interlock_lbl.setForeground(Color.WHITE);
			interlock_lbl.setBackground(Color.RED);
			interlock_lbl.setText(I18N.get(
				"gate.arm.interlock.deny_all"));
			break;
		case SYSTEM_DISABLE:
			interlock_lbl.setForeground(Color.WHITE);
			interlock_lbl.setBackground(Color.GRAY);
			interlock_lbl.setText(I18N.get(
				"gate.arm.interlock.system_disable"));
			break;
		}
	}

	/** Update the swap button enabled states */
	private void updateSwapButton(GateArmArray ga) {
		swap_act.setEnabled(ga != null && ga.getCamera() != null &&
			ga.getApproach() != null);
	}

	/** Update the button enabled states */
	private void updateButtons(GateArmArray ga) {
		boolean e = session.isUpdatePermitted(ga, "armStateNext");
		GateArmState gas = GateArmState.fromOrdinal(ga.getArmState());
		open_arm.setEnabled(e && gas == GateArmState.CLOSED &&
			isOpenAllowed(ga));
		warn_close_arm.setEnabled(e && gas == GateArmState.OPEN &&
			isCloseAllowed(ga));
		close_arm.setEnabled(e && isClosePossible(gas) &&
			isCloseAllowed(ga));
	}

	/** Check if gate arm open is allowed */
	private boolean isOpenAllowed(GateArmArray ga) {
		switch(GateArmInterlock.fromOrdinal(ga.getInterlock())) {
		case DENY_OPEN:
		case DENY_ALL:
		case SYSTEM_DISABLE:
			return false;
		default:
			return true;
		}
	}

	/** Check if gate arm close is allowed */
	private boolean isCloseAllowed(GateArmArray ga) {
		switch(GateArmInterlock.fromOrdinal(ga.getInterlock())) {
		case DENY_CLOSE:
		case DENY_ALL:
		case SYSTEM_DISABLE:
			return false;
		default:
			return true;
		}
	}

	/** Check if gate arm close is possible */
	private boolean isClosePossible(GateArmState gas) {
		switch(gas) {
		case FAULT:
		case WARN_CLOSE:
			return true;
		default:
			return false;
		}
	}

	/** Clear all of the fields */
	@Override
	public void clear() {
		ga_array = null;
		swap_act.setEnabled(false);
		open_arm.setEnabled(false);
		warn_close_arm.setEnabled(false);
		close_arm.setEnabled(false);
		swap_streams = false;
		stream_pnl.setCamera(null);
		thumb_pnl.setCamera(null);
		name_lbl.setText("");
		location_lbl.setText("");
		arm_state_lbl.setText(" ");
		arm_state_lbl.setBackground(null);
		arm_state_lbl.setForeground(null);
		interlock_lbl.setText(" ");
		interlock_lbl.setForeground(null);
		interlock_lbl.setBackground(null);
		clearArms();
		dms_watcher.setProxy(null);
	}

	/** Clear gate arms */
	private void clearArms() {
		for(int i = 0; i < MAX_ARMS; i++) {
			gate_lbl[i].setText(" ");
			state_lbl[i].setText(" ");
			state_lbl[i].setForeground(DARK_BLUE);
			state_lbl[i].setBackground(null);
		}
	}

	/** Update gate arms */
	private void updateGateArms(GateArmArray ga) {
		clearArms();
		Iterator<GateArm> it = GateArmHelper.iterator();
		while(it.hasNext()) {
			GateArm g = it.next();
			if(g.getGaArray() == ga)
				updateArmState(g);
		}
	}

	/** Update individual gate arm states.
	 * @param ga The gate arm.  May not be null. */
	private void updateArmState(GateArm ga) {
		int i = ga.getIdx() - 1;
		if(i >= 0 && i < MAX_ARMS) {
			gate_lbl[i].setText(ga.getName());
			state_lbl[i].setText(trim(getArmState(ga), 12));
			updateStateColor(ga, i);
		}
	}

	/** Trim string to a maximum length */
	static private String trim(String s, int len) {
		assert(len >= 0);
		return s.substring(0, Math.min(s.length(), len));
	}

	/** Get one gate arm state */
	private String getArmState(GateArm ga) {
		String cs = ControllerHelper.getStatus(ga.getController());
		if(cs.length() > 0)
			return cs;
		String ms = ControllerHelper.getMaintenance(ga.getController());
		if(ms.length() > 0)
			return ms;
		return GateArmState.fromOrdinal(ga.getArmState()).toString();
	}

	/** Update the status widgets */
	private void updateStateColor(GateArm ga, int i) {
		Controller c = ga.getController();
		if(ControllerHelper.isFailed(c)) {
			state_lbl[i].setForeground(Color.WHITE);
			state_lbl[i].setBackground(Color.GRAY);
		} else if(ControllerHelper.getStatus(c).length() > 0) {
			state_lbl[i].setForeground(Color.WHITE);
			state_lbl[i].setBackground(Color.BLACK);
		} else if(ControllerHelper.getMaintenance(c).length() > 0) {
			state_lbl[i].setForeground(Color.BLACK);
			state_lbl[i].setBackground(Color.YELLOW);
		} else {
			state_lbl[i].setForeground(DARK_BLUE);
			state_lbl[i].setBackground(null);
		}
	}
}
