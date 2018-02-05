/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.StreamPanel;
import us.mn.state.dot.tms.client.camera.VideoRequest;
import static us.mn.state.dot.tms.client.camera.VideoRequest.Size.THUMBNAIL;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * ParkingAreaDispatcher is a GUI component for deploying parking areas.
 *
 * @author Douglas Lau
 */
public class ParkingAreaDispatcher extends IPanel
	implements ProxyView<ParkingArea>
{
	/** SONAR session */
	private final Session session;

	/** Selection model */
	private final ProxySelectionModel<ParkingArea> sel_mdl;

	/** Name label */
	private final JLabel name_lbl = createValueLabel();

	/** Facility label */
	private final JLabel facility_lbl = createValueLabel();

	/** Camera preset 1 stream panel */
	private final StreamPanel stream_1_pnl;

	/** Camera preset 2 stream panel */
	private final StreamPanel stream_2_pnl;

	/** Camera preset 3 stream panel */
	private final StreamPanel stream_3_pnl;

	/** Available label */
	private final JLabel available_lbl = createValueLabel();

	/** Trend label */
	private final JLabel trend_lbl = createValueLabel();

	/** Button group for open/closed state */
	private final ButtonGroup group = new ButtonGroup();

	/** Parking area open radio button */
	private final JRadioButton open_btn = new JRadioButton(
		I18N.get("parking_area.open"));

	/** Parking area closed radio button */
	private final JRadioButton closed_btn = new JRadioButton(
		I18N.get("parking_area.closed"));

	/** Proxy watcher */
	private final ProxyWatcher<ParkingArea> watcher;

	/** Currently selected parking area */
	private ParkingArea parking_area;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			setSelected(sel_mdl.getSingleSelection());
		}
	};

	/** Create a new parking area dispatcher */
	public ParkingAreaDispatcher(Session s, ParkingAreaManager manager) {
		session = s;
		TypeCache<ParkingArea> cache =
			s.getSonarState().getParkingAreas();
		watcher = new ProxyWatcher<ParkingArea>(cache, this, true);
		sel_mdl = manager.getSelectionModel();
		stream_1_pnl = createStreamPanel(THUMBNAIL);
		stream_2_pnl = createStreamPanel(THUMBNAIL);
		stream_3_pnl = createStreamPanel(THUMBNAIL);
	}

	/** Create a stream panel */
	private StreamPanel createStreamPanel(VideoRequest.Size sz) {
		VideoRequest vr = new VideoRequest(session.getProperties(), sz);
		vr.setSonarSessionId(session.getSessionId());
		return new StreamPanel(vr);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		group.add(open_btn);
		group.add(closed_btn);
		JPanel b_pnl = new JPanel(new GridLayout(1, 2));
		b_pnl.add(open_btn);
		b_pnl.add(closed_btn);
		setTitle(I18N.get("parking_area.selected"));
		add("device.name");
		add(name_lbl, Stretch.LAST);
		add("parking_area.facility");
		add(facility_lbl, Stretch.LAST);
		add(createStreamsBox(), Stretch.FULL);
		add("parking_area.available");
		add(available_lbl, Stretch.LAST);
		add("parking_area.trend");
		add(trend_lbl, Stretch.LAST);
		add("parking_area.status");
		add(b_pnl, Stretch.LAST);
		stream_1_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectStream1();
			}
		});
		stream_2_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectStream2();
			}
		});
		stream_3_pnl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectStream3();
			}
		});
		watcher.initialize();
		clear();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Select a camera stream */
	private void selectStream1() {
		ParkingArea pa = parking_area;
		if (pa != null)
			selectCamera(pa.getPreset1());
	}

	/** Select a camera stream */
	private void selectStream2() {
		ParkingArea pa = parking_area;
		if (pa != null)
			selectCamera(pa.getPreset2());
	}

	/** Select a camera stream */
	private void selectStream3() {
		ParkingArea pa = parking_area;
		if (pa != null)
			selectCamera(pa.getPreset3());
	}

	/** Select a camera */
	private void selectCamera(CameraPreset cp) {
		if (cp != null) {
			session.getCameraManager()
			       .getSelectionModel()
			       .setSelected(cp.getCamera());
		}
	}

	/** Create streams box */
	private Box createStreamsBox() {
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalGlue());
		b.add(stream_1_pnl);
		b.add(Box.createHorizontalGlue());
		b.add(Box.createHorizontalStrut(UI.hgap));
		b.add(stream_2_pnl);
		b.add(Box.createHorizontalStrut(UI.hgap));
		b.add(Box.createHorizontalGlue());
		b.add(stream_3_pnl);
		b.add(Box.createHorizontalGlue());
		return b;
	}

	/** Dispose of the dispatcher */
	@Override
	public void dispose() {
		watcher.dispose();
		sel_mdl.removeProxySelectionListener(sel_listener);
		stream_1_pnl.dispose();
		stream_2_pnl.dispose();
		stream_3_pnl.dispose();
		clear();
		super.dispose();
	}

	/** Set the selected parking area */
	public void setSelected(ParkingArea pa) {
		watcher.setProxy(pa);
		boolean w = isWritePermitted(pa, "open");
		open_btn.setAction(new OpenCloseAction(pa, true, w));
		closed_btn.setAction(new OpenCloseAction(pa, false, w));
	}

	/** Check if the user is permitted to update the given parking area */
	private boolean isWritePermitted(ParkingArea pa, String a) {
		return session.isWritePermitted(pa, a);
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one (or all) attribute(s) on the form.
	 * @param pa The newly selected parking area.  May not be null.
	 * @param a Attribute to update, null for all attributes. */
	@Override
	public void update(ParkingArea pa, String a) {
		parking_area = pa;
		if (null == a || a.equals("name"))
			name_lbl.setText(pa.getName());
		if (null == a || a.equals("facilityName"))
			facility_lbl.setText(pa.getFacilityName());
		if (null == a || a.equals("preset1"))
			updateCameraStream1(pa);
		if (null == a || a.equals("preset2"))
			updateCameraStream2(pa);
		if (null == a || a.equals("preset3"))
			updateCameraStream3(pa);
		if (null == a || a.equals("trueAvailable")) {
			StringBuilder sb = new StringBuilder();
			Integer ta = pa.getTrueAvailable();
			if (ta != null) {
				sb.append(ta);
				Integer c = pa.getCapacity();
				if (c != null) {
					sb.append(" (of ");
					sb.append(c);
					sb.append(")");
				}
				Boolean t = pa.getTrustData();
				if (null == t || !t)
					sb.append(" no trust");
			} else
				sb.append("???");
			available_lbl.setText(sb.toString());
		}
		if (null == a || a.equals("trend"))
			trend_lbl.setText(pa.getTrend());
		if (null == a || a.equals("open")) {
			Boolean o = pa.getOpen();
			if (o != null) {
				if (o)
					open_btn.setSelected(true);
				else
					closed_btn.setSelected(true);
			} else
				group.clearSelection();
		}
	}

	/** Update camera stream */
	private void updateCameraStream1(ParkingArea pa) {
		CameraPreset cp = pa.getPreset1();
		stream_1_pnl.setCamera((cp != null) ? cp.getCamera() : null);
	}

	/** Update camera stream */
	private void updateCameraStream2(ParkingArea pa) {
		CameraPreset cp = pa.getPreset2();
		stream_2_pnl.setCamera((cp != null) ? cp.getCamera() : null);
	}

	/** Update camera stream */
	private void updateCameraStream3(ParkingArea pa) {
		CameraPreset cp = pa.getPreset3();
		stream_3_pnl.setCamera((cp != null) ? cp.getCamera() : null);
	}

	/** Clear all of the fields */
	@Override
	public void clear() {
		parking_area = null;
		stream_1_pnl.setCamera(null);
		stream_2_pnl.setCamera(null);
		stream_3_pnl.setCamera(null);
		name_lbl.setText("");
		facility_lbl.setText("");
		available_lbl.setText("");
		trend_lbl.setText("");
		group.clearSelection();
		open_btn.setAction(new OpenCloseAction(null, true, false));
		closed_btn.setAction(new OpenCloseAction(null, false, false));
	}
}
