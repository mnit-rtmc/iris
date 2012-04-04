/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
import java.awt.Component;
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LCSHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyJList;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.StyleSummary;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A DMS manager is a container for SONAR DMS objects.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSManager extends ProxyManager<DMS> {

	/** DMS Map object marker */
	static protected final DmsMarker MARKER = new DmsMarker();

	/** Color definition for AWS controlled style */
	static protected final Color COLOR_HELIOTROPE = new Color(1, 0.5f,0.9f);

	/** User session */
	protected final Session session;

	/** Mapping of DMS names to cell renderers */
	protected final HashMap<String, DmsCellRenderer> renderers =
		new HashMap<String, DmsCellRenderer>();

	/** Action to blank the selected DMS */
	protected BlankDmsAction blankAction;

	/** Set the blank DMS action */
	public void setBlankAction(BlankDmsAction a) {
		blankAction = a;
	}

	/** Create a new DMS manager */
	public DMSManager(Session s, TypeCache<DMS> c, GeoLocManager lm) {
		super(c, lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<DMS> createStyleListModel(Symbol s) {
		return new DMSStyleModel(this, s.getLabel(), s.getLegend(),
			session.getSonarState().getConCache().getControllers());
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return I18N.get("dms.abbreviation");
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(AffineTransform at) {
		return MARKER.createTransformedShape(at);
	}

	/** Create a theme for DMSs */
	protected ProxyTheme<DMS> createTheme() {
		// NOTE: the ordering of themes controls which color is used
		//       to render the sign icon on the map
		ProxyTheme<DMS> theme = new ProxyTheme<DMS>(this,
			getProxyType(), MARKER);
		theme.addStyle(DMSHelper.STYLE_AVAILABLE,
			ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(DMSHelper.STYLE_DEPLOYED,
			ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(DMSHelper.STYLE_SCHEDULED,
			ProxyTheme.COLOR_SCHEDULED);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean())
			theme.addStyle(DMSHelper.STYLE_AWS_DEPLOYED, Color.RED);
		theme.addStyle(DMSHelper.STYLE_MAINTENANCE,
			ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(DMSHelper.STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		if(SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			theme.addStyle(DMSHelper.STYLE_AWS_CONTROLLED,
				COLOR_HELIOTROPE);
		}
		theme.addStyle(DMSHelper.STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		// NOTE: If a sign doesn't fit in one of the other themes,
		//       it will be rendered using the STYLE_ALL theme.
		theme.addStyle(DMSHelper.STYLE_ALL,
			ProxyTheme.COLOR_INACTIVE, ProxyTheme.OUTLINE_INACTIVE);
		return theme;
	}

	/** Get an array of all styles.  This overrides the ProxyManager
	 * version so that STYLE_NO_CONTROLLER is filtered out. */
	public String[] getStyles() {
		LinkedList<String> styles = new LinkedList<String>();
		for(Symbol s: theme.getSymbols()) {
			if(!DMSHelper.STYLE_NO_CONTROLLER.equals(s.getLabel()))
				styles.add(s.getLabel());
		}
		return (String[])styles.toArray(new String[0]);
	}

	/** Create a list cell renderer */
	public ListCellRenderer createCellRenderer() {
		return new ListCellRenderer() {
			public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
			{
				DmsCellRenderer r = lookupRenderer(value);
				if(r != null) {
					return r.getListCellRendererComponent(
						list, value, index, isSelected,
						cellHasFocus);
				} else
					return new JLabel();
			}
		};
	}

	/** Lookup a DMS cell renderer */
	protected DmsCellRenderer lookupRenderer(Object value) {
		if(value instanceof DMS) {
			DMS dms = (DMS)value;
			return renderers.get(dms.getName());
		}
		return null;
	}

	/** Add a proxy to the manager */
	protected void proxyAddedSlow(DMS dms) {
		super.proxyAddedSlow(dms);
		DmsCellRenderer r = newCellRenderer();
		r.setDms(dms);
		renderers.put(dms.getName(), r);
	}

	/** Create a cell renderer */
	private DmsCellRenderer newCellRenderer() {
		return new DmsCellRenderer(getCellSize());
	}

	/** Called when a proxy attribute has changed */
	public void proxyChanged(DMS dms, String a) {
		DmsCellRenderer r = lookupRenderer(dms);
		if(r != null)
			r.updateDms(dms, a);
	}

	/** Create a proxy JList for the given style */
	public ProxyJList<DMS> createList(String style) {
		ProxyJList<DMS> list = super.createList(style);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(0);
		return list;
	}

	/** Set the current cell size */
	public void setCellSize(CellRendererSize size) {
		super.setCellSize(size);
		// update all cell renderers
		for(String dms_id: renderers.keySet()) {
			DmsCellRenderer r = newCellRenderer();
			r.setDms(DMSHelper.lookup(dms_id));
			renderers.put(dms_id, r);
		}
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<DMS> createStyleSummary() {
		StyleSummary<DMS> summary = super.createStyleSummary(true);
		summary.setStyle(DMSHelper.STYLE_DEPLOYED);
		return summary;
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(DMS dms: s_model.getSelected())
				showPropertiesForm(dms);
		}
	}

	/** Show the properteis form for the given proxy */
	public void showPropertiesForm(DMS dms) {
		SmartDesktop desktop = session.getDesktop();
		desktop.show(new DMSProperties(session, dms));
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(DMS dms: s_model.getSelected())
				return createSinglePopup(dms);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " DMSs"));
		p.addSeparator();
		if(blankAction != null)
			p.add(blankAction);
		return p;
	}

	/** Create a popup menu for a single DMS selection */
	protected JPopupMenu createSinglePopup(DMS proxy) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(proxy)));
		p.addSeparator();
		if(blankAction != null)
			p.add(blankAction);
		if(TeslaAction.isConfigured())
			p.add(new TeslaAction<DMS>(proxy));
		p.add(new PropertiesAction<DMS>(proxy) {
			protected void do_perform() {
				showPropertiesForm();
			}
		});
		return p;
	}

	/** Find the map geo location for a DMS */
	public MapGeoLoc findGeoLoc(DMS proxy) {
		if(checkStyle(DMSHelper.STYLE_ALL, proxy))
			return super.findGeoLoc(proxy);
		else
			return null;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(DMS proxy) {
		return proxy.getGeoLoc();
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, DMS proxy) {
		// FIXME: this grabs to LCS type lock, and we probably
		//        already have the DMS type lock.  Plus, this doesn't
		//        work until the LCS objects have been enumerated.
		//        There's got to be a better way...
		if(LCSHelper.lookup(proxy.getName()) != null)
			return false;
		else
			return DMSHelper.checkStyle(s, proxy);
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 12;
	}
}
