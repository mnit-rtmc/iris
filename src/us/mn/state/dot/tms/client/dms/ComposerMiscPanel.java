/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;

/**
 * The ComposerMiscPanel is a GUI panel for miscellaneous widgets related to
 * the sign composer panel.
 *
 * @author Douglas Lau
 */
public class ComposerMiscPanel extends JPanel {

	/** Sign message composer */
	private final SignMessageComposer composer;

	/** Quick message label */
	private final ILabel quick_lbl = new ILabel("dms.quick.message");

	/** Combobox used to select a quick message */
	private final QuickMessageCBox quick_cbx;

	/** Duration label */
	private final ILabel dur_lbl = new ILabel("dms.duration");

	/** Used to select the expires time for a message (optional) */
	private final JComboBox<Expiration> dur_cbx =
		new JComboBox<Expiration>(Expiration.values());

	/** Page on time label */
	private final ILabel pg_on_lbl = new ILabel("dms.page.on.time");

	/** Page on time spinner */
	private final PgTimeSpinner pg_on_spn = new PgTimeSpinner();

	/** AMBER alert label */
	private final ILabel alert_lbl = new ILabel("dms.alert");

	/** AMBER Alert checkbox */
	private final JCheckBox alert_chx = new JCheckBox();

	/** Listener for spinner change events */
	private final ChangeListener spin_listener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			if (adjusting == 0) {
				adjusting++;
				composer.updateMessage();
				adjusting--;
			}
		}
	};

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	private int adjusting = 0;

	/** Create a new composer miscellaneous panel */
	public ComposerMiscPanel(DMSDispatcher ds, SignMessageComposer smc) {
		composer = smc;
		quick_cbx = new QuickMessageCBox(ds);
		layoutPanel();
		initializeWidgets();
		pg_on_spn.addChangeListener(spin_listener);
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		setLayout(gl);
		GroupLayout.ParallelGroup lg = gl.createParallelGroup(
			GroupLayout.Alignment.TRAILING);
		GroupLayout.ParallelGroup vg = gl.createParallelGroup(
			GroupLayout.Alignment.LEADING);
		// Quick message widgets
		quick_lbl.setLabelFor(quick_cbx);
		lg.addComponent(quick_lbl);
		vg.addComponent(quick_cbx);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g1.addComponent(quick_lbl).addComponent(quick_cbx);
		// Duraton widgets
		dur_lbl.setLabelFor(dur_cbx);
		lg.addComponent(dur_lbl);
		vg.addComponent(dur_cbx);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g2.addComponent(dur_lbl).addComponent(dur_cbx);
		// Page on time widgets
		pg_on_lbl.setLabelFor(pg_on_spn);
		lg.addComponent(pg_on_lbl);
		vg.addComponent(pg_on_spn);
		GroupLayout.ParallelGroup g3 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g3.addComponent(pg_on_lbl).addComponent(pg_on_spn);
		// AMBER alert widgets
		alert_lbl.setLabelFor(alert_chx);
		lg.addComponent(alert_lbl);
		vg.addComponent(alert_chx);
		GroupLayout.ParallelGroup g4 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g4.addComponent(alert_lbl).addComponent(alert_chx);
		// Finish group layout
		GroupLayout.SequentialGroup horz_g = gl.createSequentialGroup();
		horz_g.addGap(UI.hgap).addGroup(lg);
		horz_g.addGap(UI.hgap).addGroup(vg);
		gl.setHorizontalGroup(horz_g);
		GroupLayout.SequentialGroup vert_g = gl.createSequentialGroup();
		vert_g.addGap(UI.vgap).addGroup(g1).addGap(UI.vgap);
		vert_g.addGroup(g2).addGap(UI.vgap).addGroup(g3);
		vert_g.addGap(UI.vgap).addGroup(g4).addGap(UI.vgap);
		gl.setVerticalGroup(vert_g);
	}

	/** Set multiple sign selection mode */
	public void setMultiple(boolean m) {
		alert_lbl.setVisible(m);
		alert_chx.setEnabled(m);
		alert_chx.setVisible(m);
		alert_chx.setSelected(false);
		if (m)
			quick_cbx.setSelectedItem(null);
	}

	/** Clear the widgets */
	private void clearWidgets() {
		adjusting++;
		pg_on_spn.setValue("");
		adjusting--;
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		pg_on_spn.removeChangeListener(spin_listener);
		quick_cbx.dispose();
	}

	/** Select a sign */
	public void setSign(DMS proxy) {
		initializeWidgets();
		quick_cbx.populateModel(proxy);
	}

	/** Initialize the widgets */
	private void initializeWidgets() {
		dur_cbx.setSelectedIndex(0);
		boolean dur = SystemAttrEnum.DMS_DURATION_ENABLE.getBoolean();
		dur_lbl.setVisible(dur);
		dur_cbx.setVisible(dur);
		boolean pg = PgTimeSpinner.getIEnabled();
		pg_on_lbl.setVisible(pg);
		pg_on_spn.setVisible(pg);
	}

	/** Enable or Disable the message selector */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		if (!b)
			setMultiple(false);
		quick_cbx.setEnabled(b);
		boolean vis = quick_cbx.getItemCount() > 0;
		quick_lbl.setVisible(vis);
		quick_cbx.setVisible(vis);
		dur_cbx.setEnabled(b);
		dur_cbx.setSelectedItem(0);
		pg_on_spn.setEnabled(b);
		alert_chx.setEnabled(b);
	}

	/** Set the composed MULTI string */
	public void setComposedMulti(String ms) {
		adjusting++;
		quick_cbx.setComposedMulti(ms);
		pg_on_spn.setValue(ms);
		adjusting--;
	}

	/** Get the selected duration */
	public Integer getDuration() {
		Expiration e = (Expiration) dur_cbx.getSelectedItem();
		return (e != null) ? e.duration : null;
	}

	/** Get the selected page-on time.
	 * @return Page-on time, in deciseconds, or null. */
	public Integer getPageOnTime() {
		if (PgTimeSpinner.getIEnabled()) {
			Interval poi = pg_on_spn.getValueInterval();
			int pt = poi.round(DECISECONDS);
			if (pt > 0)
				return pt;
		}
		return null;
	}

	/** Get the selected message priority */
	public DmsMsgPriority getPriority() {
		return alert_chx.isSelected()
		     ? DmsMsgPriority.ALERT
		     : DmsMsgPriority.OPERATOR;
	}
}
