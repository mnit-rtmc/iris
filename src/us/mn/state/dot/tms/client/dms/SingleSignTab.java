/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California, Davis
 * Copyright (C) 2018  SRF Consulting Group
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraPresetAction;
import us.mn.state.dot.tms.client.incident.IncidentSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A SingleSignTab is a GUI component for displaying the status of a single
 * selected DMS within the DMS dispatcher.  One instance of this class is
 * created on client startup by DMSDispatcher.
 *
 * @see us.mn.state.dot.tms.client.dms.DMSDispatcher
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SingleSignTab extends IPanel {

	/** Preview filter color */
	static private final Color PREVIEW_CLR = new Color(0, 0, 255, 48);

	/** Get the expiration time of a DMS message */
	static private String getExpiration(DMS dms) {
		Long et = (dms != null) ? dms.getExpireTime() : null;
		if (et != null) {
			SimpleDateFormat tf = new SimpleDateFormat("HH:mm");
			return tf.format(new Date(et));
		} else
			return "-";
	}

	/** Displays the id of the DMS */
	private final JLabel name_lbl = createValueLabel();

	/** Displays the brightness of the DMS */
	private final JLabel brightness_lbl = createValueLabel();

	/** Displays the camera preset for the DMS */
	private final JButton preset_btn = new JButton();

	/** Displays the location of the DMS */
	private final JLabel location_lbl = createValueLabel();

	/** Displays the sign status or faults */
	private final JLabel status_lbl = createValueLabel();

	/** Displays the associated incident */
	private final JButton inc_btn = new JButton();

	/** Displays the current operation of the DMS */
	private final JLabel operation_lbl = createValueLabel();

	/** Displays the expiration time of the current message */
	private final JLabel expiration_lbl = createValueLabel();

	/** Client session */
	private final Session session;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Tabbed pane for current/preview panels */
	private final JTabbedPane tab = new JTabbedPane();

	/** Current sign face panel */
	private final SignFacePanel current_pnl = new SignFacePanel();

	/** Preview sign face panel */
	private final SignFacePanel preview_pnl = new SignFacePanel();

	/** Pager for selected pixel panel */
	private SignPixelPager pager;

	/** Mouse listener for popup menus */
	private final MouseListener popper = new MouseAdapter() {
		public void mousePressed(MouseEvent me) {
			doPopup(me);
		}
		// NOTE: needed for cross-platform functionality
		public void mouseReleased(MouseEvent me) {
			doPopup(me);
		}
	};

	/** Cache of DMS proxy objects */
	private final TypeCache<DMS> cache;

	/** DMS watcher */
	private final ProxyWatcher<DMS> watcher;

	/** DMS view */
	private final ProxyView<DMS> dms_view =
		new ProxyView<DMS>()
	{
		public void enumerationComplete() { }
		public void update(DMS dms, String a) {
			selected = dms;
			updateAttribute(dms, a);
		}
		public void clear() {
			selected = null;
			clearSelected();
		}
	};

	/** Currently selected DMS */
	private DMS selected;

	/** Preview mode */
	private boolean preview;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	private int adjusting = 0;

	/** Create a new single sign tab */
	public SingleSignTab(Session s, DMSDispatcher d) {
		session = s;
		dispatcher = d;
		cache = s.getSonarState().getDmsCache().getDMSs();
		watcher = new ProxyWatcher<DMS>(cache, dms_view, true);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		preset_btn.setBorder(UI.buttonBorder());
		setPresetAction(null);
		inc_btn.setBorder(UI.buttonBorder());
		setIncidentAction(null);
		// Make label opaque so that we can set the background color
		status_lbl.setOpaque(true);
		add("device.name");
		add(name_lbl);
		add("dms.brightness");
		add(brightness_lbl);
		add("camera");
		add(preset_btn, Stretch.RIGHT);
		add("location");
		add(location_lbl, Stretch.LAST);
		add("device.status");
		add(status_lbl, Stretch.TRIPLE);
		add(inc_btn, Stretch.RIGHT);
		add("device.operation");
		add(operation_lbl, Stretch.TRIPLE);
		add("dms.expiration");
		add(expiration_lbl, Stretch.LAST);
		tab.add(I18N.get("dms.msg.current"), current_pnl);
		tab.add(I18N.get("dms.msg.preview"), preview_pnl);
		add(tab, Stretch.CENTER);
		createJobs();
		watcher.initialize();
	}

	/** Create the widget jobs */
	private void createJobs() {
		tab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				selectPreview(!preview);
				if (!preview)
					updateMsgCurrent(selected);
			}
		});
		current_pnl.addMouseListener(popper);
		preview_pnl.addMouseListener(popper);
	}

	/** Process a popup menu event */
	private void doPopup(MouseEvent me) {
		if (me.isPopupTrigger())
			session.getDMSManager().showPopupMenu(me);
	}

	/** Dispose of the sign tab */
	@Override
	public void dispose() {
		watcher.dispose();
		current_pnl.removeMouseListener(popper);
		preview_pnl.removeMouseListener(popper);
		setSelected(null);
		setPager(null);
		super.dispose();
	}

	/** Select the preview (or current) tab */
	public void selectPreview(boolean p) {
		if ((0 == adjusting) && (p != preview)) {
			preview = p;
			adjusting++;
			setMessage(selected);
			adjusting--;
		}
	}

	/** Set the displayed message */
	public void setMessage() {
		setMessage(selected);
	}

	/** Set the displayed message */
	private void setMessage(DMS dms) {
		SignFacePanel face_pnl = getFacePanel();
		SignPixelPanel pix_pnl = face_pnl.setSign(dms);
		setPager(createPager(dms, pix_pnl));
		tab.setSelectedComponent(face_pnl);
	}

	/** Get the appropriate face panel */
	private SignFacePanel getFacePanel() {
		return (preview) ? preview_pnl : current_pnl;
	}

	/** Create a pixel panel pager */
	private SignPixelPager createPager(DMS dms, SignPixelPanel pix_pnl) {
		if (dms != null) {
			RasterBuilder rb = DMSHelper.createRasterBuilder(dms);
			if (rb != null) {
				return (preview)
				      ? createPreviewPager(dms, rb, pix_pnl)
				      : createCurrentPager(dms, rb, pix_pnl);
			}
		}
		return null;
	}

	/** Create a preview panel pager */
	private SignPixelPager createPreviewPager(DMS dms, RasterBuilder rb,
		SignPixelPanel pix_pnl)
	{
		String ms = dispatcher.getPreviewMulti(dms);
		return new SignPixelPager(pix_pnl, rb, ms, PREVIEW_CLR);
	}

	/** Update the current panel */
	private SignPixelPager createCurrentPager(DMS dms, RasterBuilder rb,
		SignPixelPanel pix_pnl)
	{
		String ms = DMSHelper.getMultiString(dms);
		Color clr = SignPixelPanel.filterColor(dms);
		return new SignPixelPager(pix_pnl, rb, ms, clr);
	}

	/** Set a single selected DMS */
	public void setSelected(DMS dms) {
		watcher.setProxy(dms);
	}

	/** Clear the selected DMS */
	private void clearSelected() {
		setPager(null);
		name_lbl.setText("");
		brightness_lbl.setText("");
		setPresetAction(null);
		location_lbl.setText("");
		status_lbl.setText("");
		status_lbl.setForeground(null);
		status_lbl.setBackground(null);
		setIncidentAction(null);
		operation_lbl.setText("");
		expiration_lbl.setText("-");
	}

	/** Set the camera preset action */
	private void setPresetAction(DMS dms) {
		CameraPreset cp = (dms != null) ? dms.getPreset() : null;
		preset_btn.setAction(new CameraPresetAction(session, cp));
	}

	/** Set the incident action */
	private void setIncidentAction(DMS dms) {
		Incident inc = DMSHelper.lookupIncident(dms);
		inc_btn.setAction(new IncidentSelectAction(inc,
			session.getIncidentManager()));
	}

	/** Update one (or all) attribute(s) on the form.
	 * @param dms The newly selected DMS. May not be null.
	 * @param a Attribute to update, null for all attributes. */
	private void updateAttribute(DMS dms, String a) {
		if (a == null || a.equals("name"))
			name_lbl.setText(dms.getName());
		if (a == null || a.equals("status")) {
			Object o = DMSHelper.optStatus(dms, DMS.LIGHT_OUTPUT);
			if (o instanceof Integer)
				brightness_lbl.setText("" + o + "%");
			else
				brightness_lbl.setText("");
		}
		if (a == null || a.equals("preset"))
			setPresetAction(dms);
		// FIXME: this won't update when geoLoc attributes change
		if (a == null || a.equals("geoLoc")) {
			location_lbl.setText(GeoLocHelper.getLocation(
				dms.getGeoLoc()));
		}
		if (a == null || a.equals("operation"))
			updateStatus(dms);
		if ("msgCurrent".equals(a) && !preview) {
			setMessage(dms);
			updateMsgCurrent(dms);
		}
	}

	/** Update the status widgets */
	private void updateStatus(DMS dms) {
		String status = DMSHelper.optFaults(dms);
		if (DMSHelper.isOffline(dms)) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.GRAY);
			status = "OFFLINE";
		} else if (status != null) {
			status_lbl.setForeground(Color.WHITE);
			status_lbl.setBackground(Color.BLACK);
		} else {
			status_lbl.setForeground(null);
			status_lbl.setBackground(null);
		}
		status_lbl.setText((status != null) ? status : "");
		setIncidentAction(dms);
		operation_lbl.setText(dms.getOperation());
	}

	/** Update the current message on a sign */
	private void updateMsgCurrent(DMS dms) {
		if (0 == adjusting) {
			adjusting++;
			dispatcher.updateMsgCurrent(dms);
			adjusting--;
		}
		expiration_lbl.setText(getExpiration(dms));
	}

	/** Set the sign pixel pager */
	private void setPager(SignPixelPager p) {
		SignPixelPager op = pager;
		if (op != null)
			op.dispose();
		pager = p;
		if (null == p) {
			current_pnl.setFilterColor(null);
			current_pnl.setSign(null);
			preview_pnl.setSign(null);
		}
	}
}
