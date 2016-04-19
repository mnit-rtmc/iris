/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.IncidentHelper;
import us.mn.state.dot.tms.LaneConfiguration;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraSelectAction;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.roads.LaneConfigurationPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The IncidentDispatcher is a GUI component for creating incidents.
 *
 * @author Douglas Lau
 */
public class IncidentDispatcher extends IPanel
	implements ProxyListener<Incident>
{
	/** Size in pixels for each lane */
	static private final int LANE_SIZE = UI.scaled(32);

	/** Formatter for incident names */
	static private final SimpleDateFormat NAME_FMT =
		new SimpleDateFormat("yyyyMMddHHmmssSSS");

	/** Card layout name for camera combo box */
	static private final String CAMERA_CBOX = "camera_cbox";

	/** Card layout name for camera button */
	static private final String CAMERA_BTN = "camera_btn";

	/** User session */
	private final Session session;

	/** Incident manager */
	private final IncidentManager manager;

	/** Selection model */
	private final ProxySelectionModel<Incident> sel_model;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			updateSelected();
		}
	};

	/** Incident creator */
	private final IncidentCreator creator;

	/** Cache of incident proxy objects */
	private final TypeCache<Incident> cache;

	/** Incident detail proxy list model */
	private final ProxyListModel<IncidentDetail> detail_mdl;

	/** Type label */
	private final JLabel type_lbl = createValueLabel();

	/** Incident detail combo box */
	private final JComboBox<IncidentDetail> detail_cbx =
		new JComboBox<IncidentDetail>();

	/** Location of incident */
	private final JLabel location_lbl = createValueLabel();

	/** Card layout for camera widgets */
	private final CardLayout cam_cards = new CardLayout();

	/** Camera panel */
	private final JPanel cam_pnl = new JPanel(cam_cards);

	/** Verify camera combo box */
	private final JComboBox<Camera> camera_cbx = new JComboBox<Camera>();

	/** Verify camera button */
	private final JButton camera_btn = new JButton();

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LANE_SIZE, true);

	/** Impact panel */
	private final ImpactPanel impact_pnl = new ImpactPanel(LANE_SIZE);

	/** Action to log an incident change */
	private final IAction log_inc = new IAction("incident.log") {
		protected void doActionPerformed(ActionEvent e) {
			Incident inc = sel_model.getSingleSelection();
			if (inc instanceof ClientIncident)
				create((ClientIncident) inc);
			else if (inc != null)
				logUpdate(inc);
		}
	};

	/** Action to deploy devices */
	private final IAction deploy_inc = new IAction("incident.deploy") {
		protected void doActionPerformed(ActionEvent e) {
			Incident inc = sel_model.getSingleSelection();
			if (inc != null &&
			  !(inc instanceof ClientIncident))
				showDeployForm(inc);
		}
	};

	/** Action to clear an incident */
	private final IAction clear_inc = new IAction("incident.clear") {
		protected void doActionPerformed(ActionEvent e) {
			Incident inc = sel_model.getSingleSelection();
			if (inc != null) {
				boolean c = clear_btn.isSelected();
				inc.setCleared(c);
				if (c && !inc.getConfirmed())
					inc.destroy();
			}
		}
	};

	/** Button to clear an incident */
	private final JCheckBox clear_btn = new JCheckBox(clear_inc);

	/** Action to edit incident */
	private final IAction edit_inc = new IAction("incident.edit") {
		protected void doActionPerformed(ActionEvent e) {
			Incident inc = sel_model.getSingleSelection();
			if (inc != null)
				editIncident(inc);
		}
	};

	/** Currently watching incident */
	private Incident watching;

	/** Watch an incident */
	private void watch(final Incident nw) {
		final Incident ow = watching;
		if (ow != null)
			cache.ignoreObject(ow);
		watching = nw;
		if (nw != null)
			cache.watchObject(nw);
	}

	/** Create a new incident dispatcher */
	public IncidentDispatcher(Session s, IncidentManager man,
		IncidentCreator ic)
	{
		session = s;
		manager = man;
		sel_model = manager.getSelectionModel();
		creator = ic;
		cache = s.getSonarState().getIncCache().getIncidents();
		detail_mdl = new ProxyListModel<IncidentDetail>(
			s.getSonarState().getIncCache().getIncidentDetails());
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		detail_mdl.initialize();
		detail_cbx.setRenderer(new IncidentDetailRenderer());
		detail_cbx.setModel(new IComboBoxModel<IncidentDetail>(
			detail_mdl));
		type_lbl.setHorizontalTextPosition(SwingConstants.TRAILING);
		cam_pnl.add(camera_cbx, CAMERA_CBOX);
		cam_pnl.add(camera_btn, CAMERA_BTN);
		camera_btn.setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.LOWERED));
		setTitle(I18N.get("incident.selected"));
		add("incident.type");
		add(type_lbl, Stretch.LAST);
		add("incident.detail");
		add(detail_cbx, Stretch.LAST);
		add("location");
		add(location_lbl, Stretch.LAST);
		add("camera");
		add(cam_pnl, Stretch.LAST);
		add(buildImpactBox(), Stretch.FULL);
		add(buildButtonBox(), Stretch.RIGHT);
		createButtonJobs();
		cache.addProxyListener(this);
		clearSelected();
		sel_model.addProxySelectionListener(sel_listener);
	}

	/** Build the impact box */
	private JPanel buildImpactBox() {
		lane_config.add(Box.createVerticalStrut(LANE_SIZE / 2));
		lane_config.add(impact_pnl);
		lane_config.add(Box.createVerticalStrut(LANE_SIZE / 2));
		return lane_config;
	}

	/** Build the button box */
	private Box buildButtonBox() {
		Box btns = Box.createHorizontalBox();
		btns.add(new JButton(log_inc));
		btns.add(Box.createHorizontalStrut(UI.hgap));
		btns.add(new JButton(deploy_inc));
		btns.add(Box.createHorizontalStrut(UI.hgap));
		btns.add(clear_btn);
		btns.add(Box.createHorizontalStrut(UI.hgap));
		btns.add(new JButton(edit_inc));
		return btns;
	}

	/** Create jobs for button press events */
	private void createButtonJobs() {
		impact_pnl.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				Incident inc = watching;
				if (inc != null)
					enableWidgets(inc);
			}
		});
	}

	/** Edit (replace) an existing incident */
	private void editIncident(Incident inc) {
		ClientIncident ci = new ClientIncident(inc.getName(),
			inc.getEventType(), inc.getDetail(), inc.getLaneType(),
			inc.getRoad(), inc.getDir(), inc.getLat(),
			inc.getLon(), inc.getImpact());
		sel_model.setSelected(ci);
		creator.replaceIncident(inc);
	}

	/** Create a new incident */
	private void create(ClientIncident inc) {
		String name = createUniqueIncidentName();
		if (isAddPermitted(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			Incident rpl = getReplaces(inc);
			if (rpl != null) {
				if (!needsReplacing(inc, rpl)) {
					logUpdate(rpl);
					return;
				}
				attrs.put("replaces", getOriginalReplaces(rpl));
				destroyIncident(rpl);
			}
			attrs.put("event_desc_id", inc.getEventType());
			IncidentDetail dtl = getSelectedDetail();
			if (dtl != null)
				attrs.put("detail", dtl);
			attrs.put("lane_type", inc.getLaneType());
			attrs.put("road", inc.getRoad());
			attrs.put("dir", inc.getDir());
			attrs.put("lat", inc.getLat());
			attrs.put("lon", inc.getLon());
			attrs.put("camera", getSelectedCamera());
			attrs.put("impact", impact_pnl.getImpact());
			attrs.put("cleared", false);
			cache.createObject(name, attrs);
			Incident proxy = getProxy(name);
			if (proxy != null)
				sel_model.setSelected(proxy);
			else
				sel_model.clearSelection();
		}
	}

	/** Get the incident that is being replaced */
	private Incident getReplaces(ClientIncident inc) {
		String rpl = inc.getReplaces();
		return (rpl != null) ? cache.lookupObject(rpl) : null;
	}

	/** Test if an incident needs to be replaced */
	private boolean needsReplacing(ClientIncident inc, Incident rpl) {
		return getSelectedDetail() != rpl.getDetail() ||
		       inc.getRoad() != rpl.getRoad() ||
		       inc.getDir() != rpl.getDir() ||
		       inc.getLat() != rpl.getLat() ||
		       inc.getLon() != rpl.getLon() ||
		       getSelectedCamera() != rpl.getCamera();
	}

	/** Get name of original incident this replaces */
	static private String getOriginalReplaces(Incident inc) {
		String rpl = inc.getReplaces();
		if (rpl != null && rpl.length() > 0)
			return rpl;
		else
			return inc.getName();
	}

	/** Destroy the named incident */
	private void destroyIncident(Incident inc) {
		inc.setCleared(true);
		inc.destroy();
	}

	/** Get the selected incident detail */
	private IncidentDetail getSelectedDetail() {
		Object detail = detail_cbx.getSelectedItem();
		if (detail instanceof IncidentDetail)
			return (IncidentDetail) detail;
		else
			return null;
	}

	/** Get the selected camera */
	private Object getSelectedCamera() {
		Object cam = camera_cbx.getSelectedItem();
		if (cam != null)
			return cam;
		else
			return "";
	}

	/** Update an incident */
	private void logUpdate(Incident inc) {
		inc.setImpact(impact_pnl.getImpact());
	}

	/** Show the device deploy form */
	private void showDeployForm(Incident inc) {
		session.getDesktop().show(new DeviceDeployForm(session, inc,
			manager));
	}

	/** Create a unique incident name */
	private String createUniqueIncidentName() {
		String name = NAME_FMT.format(System.currentTimeMillis());
		return name.substring(0, 16);
	}

	/** Get the incident proxy object */
	private Incident getProxy(String name) {
		// wait for up to 20 seconds for proxy to be created
		for (int i = 0; i < 200; i++) {
			Incident inc = IncidentHelper.lookup(name);
			if (inc != null)
				return inc;
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				// Ignore
			}
		}
		return null;
	}

	/** A new proxy has been added */
	@Override
	public void proxyAdded(Incident proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	@Override
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	@Override
	public void proxyRemoved(Incident proxy) {
		// Note: the IncidentManager will remove the proxy from the
		//       ProxySelectionModel, so we can ignore this.
	}

	/** A proxy has been changed */
	@Override
	public void proxyChanged(Incident proxy, String a) {
		if (proxy == sel_model.getSingleSelection())
			updateAttribute(proxy, a);
	}

	/** Update an attribute for the given proxy.
	 * FIXME: this should use ProxyWatcher */
	private void updateAttribute(final Incident proxy, final String a) {
		runSwing(new Runnable() {
			public void run() {
				doUpdateAttribute(proxy, a);
			}
		});
	}

	/** Dispose of the dispatcher */
	@Override
	public void dispose() {
		sel_model.removeProxySelectionListener(sel_listener);
		cache.removeProxyListener(this);
		detail_mdl.dispose();
		clearSelected();
		super.dispose();
	}

	/** Update the selected object(s) */
	private void updateSelected() {
		Incident inc = sel_model.getSingleSelection();
		if (inc != null)
			setSelected(inc);
		else
			clearSelected();
		if (!(inc instanceof ClientIncident))
			manager.removeClientIncident();
	}

	/** Clear the selection */
	private void clearSelected() {
		watch(null);
		disableWidgets();
	}

	/** Disable the dispatcher widgets */
	private void disableWidgets() {
		clearEventType();
		detail_cbx.setSelectedItem(null);
		detail_cbx.setEnabled(false);
		location_lbl.setText("");
		camera_cbx.setSelectedItem(null);
		camera_cbx.setEnabled(false);
		setCameraAction(null);
		cam_cards.show(cam_pnl, CAMERA_BTN);
		log_inc.setEnabled(false);
		deploy_inc.setEnabled(false);
		clear_inc.setEnabled(false);
		clear_btn.setSelected(false);
		edit_inc.setEnabled(false);
		lane_config.clear();
		impact_pnl.setImpact("");
	}

	/** Set a single selected incident */
	private void setSelected(Incident inc) {
		if (inc instanceof ClientIncident)
			watch(null);
		else
			watch(inc);
		LaneConfiguration lc = manager.laneConfiguration(inc);
		lane_config.setConfiguration(lc);
		impact_pnl.setConfiguration(lc);
		impact_pnl.setImpact(inc.getImpact());
		updateAttribute(inc, null);
		enableWidgets(inc);
	}

	/** Enable the dispatcher widgets */
	private void enableWidgets(Incident inc) {
		if (inc instanceof ClientIncident) {
			boolean p = isAddPermitted("oname");
			detail_cbx.setEnabled(p);
			camera_cbx.setEnabled(p);
			cam_cards.show(cam_pnl, CAMERA_CBOX);
			log_inc.setEnabled(p);
			deploy_inc.setEnabled(false);
			clear_inc.setEnabled(false);
			edit_inc.setEnabled(false);
		} else {
			boolean update = isUpdatePermitted(inc);
			detail_cbx.setEnabled(false);
			camera_cbx.setEnabled(false);
			cam_cards.show(cam_pnl, CAMERA_BTN);
			log_inc.setEnabled(update && isImpactChanged(inc));
			deploy_inc.setEnabled(update && canDeploy(inc) &&
				!inc.getCleared());
			clear_inc.setEnabled(update);
			edit_inc.setEnabled(update && !inc.getCleared());
		}
	}

	/** Check if the impact has changed */
	private boolean isImpactChanged(Incident inc) {
		String imp = impact_pnl.getImpact();
		return !imp.equals(inc.getImpact());
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(Incident inc, String a) {
		if (a == null) {
			detail_cbx.setSelectedItem(inc.getDetail());
			type_lbl.setText(manager.getTypeDesc(inc));
			type_lbl.setIcon(manager.getIcon(inc));
			location_lbl.setText(
				manager.getGeoLoc(inc).getDescription());
			if (inc instanceof ClientIncident)
				camera_cbx.setModel(createCameraModel(inc));
			setCameraAction(inc);
		}
		if (a == null || a.equals("impact"))
			impact_pnl.setImpact(inc.getImpact());
		if (a == null || a.equals("cleared"))
			clear_btn.setSelected(inc.getCleared());
		if (a != null && (a.equals("impact") || a.equals("cleared")))
			enableWidgets(inc);
	}

	/** Set the camera action */
	private void setCameraAction(Incident inc) {
		Camera cam = IncidentHelper.getCamera(inc);
		camera_btn.setAction(new CameraSelectAction(cam,
		    session.getCameraManager().getSelectionModel()));
		camera_btn.setEnabled(cam != null);
	}

	/** Clear the event type */
	private void clearEventType() {
		type_lbl.setText("");
		type_lbl.setIcon(manager.getIcon((Incident) null));
	}

	/** Create a combo box model for nearby cameras */
	private DefaultComboBoxModel<Camera> createCameraModel(Incident inc) {
		DefaultComboBoxModel<Camera> mdl =
			new DefaultComboBoxModel<Camera>();
		if (inc instanceof ClientIncident) {
			ClientIncident ci = (ClientIncident)inc;
			Position pos = ci.getWgs84Position();
			for (Camera cam: CameraHelper.findNearest(pos, 5)) {
				mdl.addElement(cam);
				if (mdl.getSelectedItem() == null)
					mdl.setSelectedItem(cam);
			}
			mdl.addElement(null);
		} else {
			mdl.addElement(inc.getCamera());
			mdl.setSelectedItem(inc.getCamera());
		}
		return mdl;
	}

	/** Check if the user is permitted to add the named incident */
	public boolean isAddPermitted(String oname) {
		return session.isAddPermitted(Incident.SONAR_TYPE, oname);
	}

	/** Check if the user is permitted to update the given incident */
	private boolean isUpdatePermitted(Incident inc) {
		return session.isUpdatePermitted(inc);
	}

	/** Check if the user can deploy signs for an incident */
	private boolean canDeploy(Incident inc) {
		switch (LaneType.fromOrdinal(inc.getLaneType())) {
		case MAINLINE:
			return canSendIndications();
		default:
			return false;
		}
	}

	/** Check if the user can send LCS array indications */
	private boolean canSendIndications() {
		return isLcsUpdatePermitted("indicationsNext")
		    && isLcsUpdatePermitted("ownerNext");
	}

	/** Check if the user is permitted to update an LCS array attribute */
	private boolean isLcsUpdatePermitted(String a) {
		return session.isUpdatePermitted(LCSArray.SONAR_TYPE, a);
	}
}
