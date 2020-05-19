/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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


package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import us.mn.state.dot.tms.TollZone;
import us.mn.state.dot.tms.TollZoneHelper;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTolling;

/**
 * WYSIWYG DMS Message Editor dialog form for editing Tolling action tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WTollingTagDialog extends WMultiTagDialog {
	protected WtTolling editTok;
	private WTagParamEnumField<PricingMode> modeField;
	private ArrayList<WTagParamSonarObjectField<TollZone>> tollZoneFields = 
			new ArrayList<WTagParamSonarObjectField<TollZone>>();
	private String modeStr;
	private PricingMode mode;
	protected ArrayList<String> tzNames = new ArrayList<String>();
	protected ArrayList<TollZone> tollZones = new ArrayList<TollZone>();
	private TollZone[] allTollZones;
	private JPanel extraTollZones;
	private final ArrayList<JPanel> extraTollZonePanels =
			new ArrayList<JPanel>();
	private JButton addTollZoneBtn;
	private JButton deleteTollZoneBtn;
	
	public WTollingTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
		
		// make the list of all toll zones right away
		getTollZones();
	}
	
	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtTolling) tok;
		modeStr = editTok.getMode();
		mode = PricingMode.getEnumFromMode(modeStr);
		tzNames = new ArrayList<String>();
		tollZones = new ArrayList<TollZone>();
		
		for (String tzs: editTok.getZones()) {
			tzNames.add(tzs);
			TollZone tz = TollZoneHelper.lookup(tzs);
			if (tz != null)
				tollZones.add(tz);
		}
	}
	
	@Override
	protected void addTagForm() {
		// TODO something is weird about the order in which the methods in
		// this class are called requiring us to call this an extra time
		if (editTok != null)
			loadFields(editTok);
		
		// add a ComboBox for the mode field
		modeField = new WTagParamEnumField<PricingMode>(
				PricingMode.values(), mode, false);
		addField("wysiwyg.tolling_dialog.mode", modeField);
		
		// add the first tolling zone field
		TollZone firstZone = null;
		if (!tollZones.isEmpty())
			firstZone = tollZones.get(0);
		WTagParamSonarObjectField<TollZone> tzf = 
				new WTagParamSonarObjectField<TollZone>(
						allTollZones, firstZone, true);
		tollZoneFields.add(tzf);
		addField("wysiwyg.tolling_dialog.zone", tzf);
		
		// add an empty panel so we can add more toll zone fields later
		extraTollZones = new JPanel();
		extraTollZones.setLayout(new BoxLayout(
				extraTollZones, BoxLayout.Y_AXIS));
		
		// set the preferred height of this panel to 3 toll zone fields
		// (this is easier than figuring out how to get the whole frame to
		// resize)
		Dimension d = extraTollZones.getPreferredSize();
		d.height = 3*tzf.getPreferredSize().height;
		extraTollZones.setPreferredSize(d);
		add(extraTollZones);
		
		// add buttons to trigger adding or deleting fields
		addTollZoneBtn = new JButton(addTollingZoneFieldAction);
		deleteTollZoneBtn = new JButton(deleteTollingZoneFieldAction);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(addTollZoneBtn);
		p.add(deleteTollZoneBtn);
		add(p);
		
		// if we have more than 1 tolling zone already, add more fields and
		// enable the delete button
		if (tollZones.size() > 1) {
			for (int i = 1; i < tollZones.size(); ++i) {
				TollZone tz = tollZones.get(i);
				addTollingZoneField(tz);
			}
			deleteTollZoneBtn.setEnabled(true);
		} else
			deleteTollZoneBtn.setEnabled(false);
	}

	/** Get a list of Tolling Zones */
	private void getTollZones() {
		ArrayList<TollZone> tollZoneArrayList = new ArrayList<TollZone>();
		Iterator<TollZone> it = TollZoneHelper.iterator();
		while (it.hasNext()) {
			tollZoneArrayList.add(it.next());
		}
		allTollZones = new TollZone[tollZoneArrayList.size()];
		tollZoneArrayList.toArray(allTollZones);
	}
	
	private void refresh() {
		revalidate();
		repaint();
	}
	
	private void addTollingZoneField(TollZone tz) {
		// make a new field and add it to the "extra" panel
		WTagParamSonarObjectField<TollZone> tzf =
				new WTagParamSonarObjectField<TollZone>(
						allTollZones, tz, true);
		tollZoneFields.add(tzf);
		JPanel p = makeFieldPanel("wysiwyg.tolling_dialog.zone", tzf);
		extraTollZonePanels.add(p);
		extraTollZones.add(p);
		refresh();
		
		// make sure the delete button is enabled
		deleteTollZoneBtn.setEnabled(true);
	}
	
	/** Add new tolling zone field action */
	private final IAction addTollingZoneFieldAction = new IAction(
			"wysiwyg.tolling_dialog.add_zone") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			addTollingZoneField(null);
		}
	};
	
	/** Delete tolling zone field action */
	private final IAction deleteTollingZoneFieldAction = new IAction(
			"wysiwyg.tolling_dialog.delete_zone") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// check the number of tolling zone fields
			if (tollZoneFields.size() > 1) {
				// if there is more than one, delete the last one
				tollZoneFields.remove(tollZoneFields.size()-1);
				extraTollZones.remove(extraTollZonePanels.remove(
						extraTollZonePanels.size()-1));
			}
			
			// disable the delete button once there is only 1 field
			if (tollZoneFields.size() == 1)
				deleteTollZoneBtn.setEnabled(false);
			
			refresh();
		}
	};

	@Override
	protected boolean validateForm() {
		boolean modeValid = modeField.contentsValid();
		ArrayList<WTagParamComponent> tzfs =
				new ArrayList<WTagParamComponent>(tollZoneFields);
		boolean zonesValid = validateFields(tzfs);
		return modeValid && zonesValid;
	}
	
	@Override
	protected WtTolling makeNewTag() {
		// get the mode field
		mode = modeField.getSelectedItem();
		modeStr = mode.getMode();
		
		// get all zone fields and pack into an array
		tzNames.clear();
		tollZones.clear();
		for (WTagParamSonarObjectField<TollZone> tzf: tollZoneFields) {
			TollZone tz = tzf.getSelectedItem();
			tollZones.add(tz);
			tzNames.add(tz.getName());
		}
		String tollZoneNames[] = new String[tzNames.size()];
		tzNames.toArray(tollZoneNames);
		return new WtTolling(modeStr, tollZoneNames);
	}

	/** Pricing modes */
	private enum PricingMode {
		priced("p"),
		open("o"),
		closed("c");
		
		private String mode;
		
		private PricingMode(String m) {
			mode = m;
		}
		
		public String getMode() {
			return mode;
		}
		
		public static PricingMode getEnumFromMode(String m) {
			for (PricingMode e: values()) {
				String em = e.getMode();
				if (em.equals(m))
					return e;
			}
			return null;
		}
	};
}