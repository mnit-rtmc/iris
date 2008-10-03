/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.TimingPlan;
import us.mn.state.dot.tms.TimingPlanList;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.TrafficDeviceAttribute;
import us.mn.state.dot.tms.client.AttributeTab;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.TrafficDeviceAttributeTableModel;
import us.mn.state.dot.tms.client.toast.TMSObjectForm;
import us.mn.state.dot.tms.client.toast.TrafficDeviceForm;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;
import us.mn.state.dot.tms.utils.Agency;
import us.mn.state.dot.tms.utils.I18NMessages;
import us.mn.state.dot.tms.utils.TMSProxy;

/**
 * This is a form for viewing and editing the properties of a dynamic message
 * sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSProperties extends TrafficDeviceForm {

	/** Format a number for display */
	static protected String formatNumber(int i) {
		if(i > 0)
			return String.valueOf(i);
		else
			return UNKNOWN;
	}

	/** Format milimeter units for display */
	static protected String formatMM(int i) {
		if(i > 0)
			return i + " mm";
		else
			return UNKNOWN;
	}

	/** Format miliamp units for display */
	static protected String formatMA(int i) {
		if(i > 0)
			return i + " (" + (i / 2f) + " ma)";
		else
			return UNKNOWN;
	}

	/** Format pixel units for display */
	static protected String formatPixels(int i) {
		if(i > 0)
			return i + " pixels";
		else if(i == 0)
			return "Variable";
		else
			return UNKNOWN;
	}

	/** Celcius temperature string */
	static protected final String CELCIUS = "\u00B0 C";

	/** Format the temperature */
	static protected String formatTemp(int minTemp, int maxTemp) {
		if(maxTemp == DMS.UNKNOWN_TEMP)
			maxTemp = minTemp;
		if(minTemp == maxTemp) {
			if(minTemp == DMS.UNKNOWN_TEMP)
				return UNKNOWN;
			else
				return "" + minTemp + CELCIUS;
		} else
			return "" + minTemp + "..." + maxTemp + CELCIUS;
	}

	/** Default LedStar brightness table */
	static protected final int[] LEDSTAR_DEFAULT = {
		512, 0, 100,
		6400, 0, 10,
		16384, 10, 20,
		24576, 20, 30,
		32768, 30, 50,
		40960, 50, 70,
		51200, 70, 255
	};

	/** Frame title */
	static protected String TITLE = 
		I18NMessages.get("DMSProperties.Title")+": ";

	/** Remote dynamic message sign interface */
	protected DMS sign;

	/** Camera combo box */
	protected final JComboBox camera = new JComboBox();

	/** Sign group model */
	protected final SignGroupModel sign_group_model;

	/** Sign text table model */
	protected SignTextTableModel sign_text_model;

	/** Sign group table component */
	protected final JTable group_table = new JTable();

	/** Button to delete a sign group */
	protected final JButton delete_group = new JButton("Delete Group");

	/** Sign text table component */
	protected final JTable sign_text_table = new JTable();

	/** Button to delete sign text message */
	protected final JButton delete_text = new JButton("Delete Message");

	/** Sign pixel panel */
	protected final SignPixelPanel pixel_panel = new SignPixelPanel();

	/** Travel time template string field */
	protected final JTextArea travel = new JTextArea();

	/** Make label */
	protected final JLabel make = new JLabel();

	/** Model label */
	protected final JLabel model = new JLabel();

	/** Version label */
	protected final JLabel version = new JLabel();

	/** Sign access label */
	protected final JLabel access = new JLabel();

	/** Sign type label */
	protected final JLabel type = new JLabel();

	/** Sign height label */
	protected final JLabel height = new JLabel();

	/** Sign width label */
	protected final JLabel width = new JLabel();

	/** Horizontal border label */
	protected final JLabel hBorder = new JLabel();

	/** Vertical border label */
	protected final JLabel vBorder = new JLabel();

	/** Sign legend label */
	protected final JLabel legend = new JLabel();

	/** Beacon label */
	protected final JLabel beacon = new JLabel();

	/** Sign technology label */
	protected final JLabel tech = new JLabel();

	/** Character height label */
	protected final JLabel cHeight = new JLabel();

	/** Character width label */
	protected final JLabel cWidth = new JLabel();

	/** Sign height (pixels) label */
	protected final JLabel pHeight = new JLabel();

	/** Sign width (pixels) label */
	protected final JLabel pWidth = new JLabel();

	/** Horizontal pitch label */
	protected final JLabel hPitch = new JLabel();

	/** Vertical pitch label */
	protected final JLabel vPitch = new JLabel();

	/** Label to display LDC pot base */
	protected final JLabel ldcPotBase = new JLabel();

	/** Spinner to adjuct LDC pot base */
	protected final JSpinner ldcPotBaseSpn = new JSpinner(
		new SpinnerNumberModel(20, 20, 65, 5));

	/** Pixel current low label */
	protected final JLabel currentLow = new JLabel();

	/** Pixel current low threshold spinner */
	protected final JSpinner currentLowSpn = new JSpinner(
		new SpinnerNumberModel(5, 0, 100, 1));

	/** Pixel current high label */
	protected final JLabel currentHigh = new JLabel();

	/** Pixel current high threshold spinner */
	protected final JSpinner currentHighSpn = new JSpinner(
		new SpinnerNumberModel(40, 0, 100, 1));

	/** Bad pixel limit */
	protected final JLabel badLimit = new JLabel();

	/** Bad pixel limit spinner */
	protected final JSpinner badLimitSpn = new JSpinner(
		new SpinnerNumberModel(35, 0, 2625, 5));

	/** Operation description label */
	protected final JLabel operation = new JLabel();

	/** Power supply status table */
	protected final JTable power_table = new JTable();

	/** Pixel test activation button (optional) */
	protected final JButton pixelTest;

	/** Lamp test activation button (optional) */
	protected final JButton lampTest;

	/** Fan test activation button (optional) */
	protected final JButton fanTest;

	/** Get sign status button (optional) */
	protected final JButton getStatusButton;

	/** Reset sign button (optional) */
	protected final JButton resetButton;

	/** Reset modem button (optional) */
	protected final JButton resetModemButton;

	/** Bad pixel count label */
	protected final JLabel badPixels = new JLabel();

	/** Lamp status label */
	protected final JLabel lamp = new JLabel();

	/** Fan status label */
	protected final JLabel fan = new JLabel();

	/** Heat tape status label */
	protected final JLabel heat_tape = new JLabel();

	/** Cabinet temperature label */
	protected final JLabel cabinetTemp = new JLabel();

	/** Ambient temperature label */
	protected final JLabel ambientTemp = new JLabel();

	/** Housing temperature label */
	protected final JLabel housingTemp = new JLabel();

	/** Note */
	protected final JLabel dms_note = new JLabel();

	/** Brightness table */
	protected BrightnessTable b_table;

	/** Timing plan table component */
	protected final JTable plan_table = new JTable();

	/** Add AM plan button */
	protected final JButton am_plan = new JButton("Add AM Plan");

	/** Add PM plan button */
	protected final JButton pm_plan = new JButton("Add PM Plan");

	/** Photocell brightness control */
	protected final JRadioButton con_photocell =
		new JRadioButton("Photocell control");

	/** Manual brightness control button */
	protected final JRadioButton con_manual =
		new JRadioButton("Manual control");

	/** Form object */
	protected final TMSObjectForm form = this;

	/** Sonar state */
	protected final SonarState state;

	/** SONAR user */
	protected final User user;

	/** Array of timing plans */
	protected TimingPlan[] plans;

	/** attribute editor tab (optional) */
	protected AttributeTab attribute_tab=null;

	/** Create a new DMS properties form 
	 *  @param tc TmsConnection
	 *  @param id Dms ID, e.g. "V1"
	 */
	public DMSProperties(TmsConnection tc, String id) {
		super(TITLE + id, tc, id);
		state = tc.getSonarState();
		user = state.lookupUser(tc.getUser().getName());
		sign_group_model = new SignGroupModel(id,
			state.getDmsSignGroups(), state.getSignGroups(),
			connection.isAdmin());

		// pixel test button (optional)
		if (!Agency.isId(Agency.CALTRANS_D10))
			pixelTest = new JButton(I18NMessages.get(
				"DMSProperties.PixelTestButton"));
		else
			pixelTest = null;

		// lamp test button is (optional)
		if (false)
			lampTest = new JButton("Lamp test");
		else
			lampTest = null;

		// fan test button is (optional)
		if (!Agency.isId(Agency.CALTRANS_D10))
			fanTest = new JButton(I18NMessages.get(
				"DMSProperties.FanTestButton"));
		else
			fanTest = null;

		// get status button (optional)
		if (Agency.isId(Agency.CALTRANS_D10))
			getStatusButton = new JButton(I18NMessages.get(
				"DMSProperties.GetStatusButton"));
		else
			getStatusButton = null;

		// reset button (optional)
		if (Agency.isId(Agency.CALTRANS_D10))
			resetButton = new JButton(I18NMessages.get(
				"DMSProperties.ResetButton"));
		else
			resetButton = null;

		// reset modem button (optional)
		if (Agency.isId(Agency.CALTRANS_D10))
			resetModemButton = new JButton(I18NMessages.get(
				"DMSProperties.ResetModemButton"));
		else
			resetModemButton = null;
	}

	protected int h_pix;
	protected int v_pix;
	protected int c_pix;
	protected int hp_mm;
	protected int vp_mm;
	protected int h_mm;
	protected int hb_mm;

	/** Initialize the widgets on the form */
	protected void initialize() throws RemoteException {
		TMSProxy tms = connection.getProxy();
		SortedList s = tms.getDMSList();
		sign = (DMS)s.getElement(id);

		h_pix = sign.getSignWidthPixels();
		v_pix = sign.getLineHeightPixels();
		c_pix = sign.getCharacterWidthPixels();
		hp_mm = sign.getHorizontalPitch();
		vp_mm = sign.getVerticalPitch();
		h_mm = sign.getSignWidth();
		hb_mm = sign.getHorizontalBorder();

		ListModel model = state.getCameraModel();
		camera.setModel(new WrapperComboBoxModel(model));
		TimingPlanModel plan_model = new TimingPlanModel(
			(TimingPlanList)tms.getTimingPlans().getList(), sign,
			admin);
		plan_table.setModel(plan_model);
		plan_table.setColumnModel(TimingPlanModel.createColumnModel());
		b_table = new BrightnessTable(sign, admin);
		setDevice(sign);
		super.initialize();
		location.addRow("Camera", camera);

		// create the tabs
//FIXME: it would be nice if these tabs were in separate classes (see AttributeTab) mtod, 09/16/08
		tab.add("Messages", createMessagePanel());
		tab.add("Travel Time", createTravelTimePanel());
		tab.add("Configuration", createConfigurationPanel());
		if (!Agency.isId(Agency.CALTRANS_D10))
			tab.add("Brightness", createBrightnessPanel());
		if (!Agency.isId(Agency.CALTRANS_D10))
			tab.add("Ledstar", createLedstarPanel());
		tab.add("Status", createStatusPanel());

		// optional attribute tab
		if (Agency.isId(Agency.CALTRANS_D10)) {
			attribute_tab = new AttributeTab(admin, this, 
				state, getId()); 
			tab.add(attribute_tab);
		}
	}

	/** Dispose of the form */
	protected void dispose() {
		sign_group_model.dispose();
		if(sign_text_model != null)
			sign_text_model.dispose();
		if(attribute_tab != null)
			attribute_tab.dispose();
		super.dispose();
	}

	/** Add a actions for the delete buttons */
	protected void addDeleteActions() {
		new ActionJob(this, delete_group) {
			public void perform() {
				SignGroup group = getSelectedGroup();
				if(group != null)
					group.destroy();
			}
		};
		new ActionJob(this, delete_text) {
			public void perform() throws Exception {
				SignText sign_text = getSelectedSignText();
				if(sign_text != null)
					sign_text.destroy();
			}
		};
	}

	/** Create the message panel */
	protected JPanel createMessagePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 5;
		bag.insets.left = 5;
		bag.insets.right = 5;
		bag.insets.bottom = 5;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		initGroupTable();
		JScrollPane scroll = new JScrollPane(group_table);
		bag.gridx = 0;
		bag.gridy = 0;
		panel.add(scroll, bag);
		initSignTextTable();
		scroll = new JScrollPane(sign_text_table);
		bag.gridx = 1;
		bag.gridy = 0;
		panel.add(scroll, bag);
		bag.weightx = 0;
		bag.weighty = 0;
		bag.fill = GridBagConstraints.NONE;
		if(admin) {
			bag.gridx = 0;
			bag.gridy = 1;
			delete_group.setEnabled(false);
			panel.add(delete_group, bag);
			bag.gridx = 1;
			delete_text.setEnabled(false);
			panel.add(delete_text, bag);
			addDeleteActions();
		}
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 2;
		pixel_panel.setMinimumSize(new Dimension(540, 40));
		pixel_panel.setPreferredSize(new Dimension(540, 40));
		pixel_panel.setSize(new Dimension(540, 40));
		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createTitledBorder(
			"Message Preview"));
		pnl.add(pixel_panel);
		panel.add(pnl, bag);
		return panel;
	}

	/** Initialize the sign group table */
	protected void initGroupTable() {
		final ListSelectionModel s = group_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectGroup();
			}
		};
		group_table.setAutoCreateColumnsFromModel(false);
		group_table.setColumnModel(SignGroupModel.createColumnModel());
		group_table.setModel(sign_group_model);
		group_table.setPreferredScrollableViewportSize(
			new Dimension(260, 200));
	}

	/** Select a new sign group */
	protected void selectGroup() {
		SignGroup group = getSelectedGroup();
		if(group != null) {
			if(sign_text_model != null)
				sign_text_model.dispose();
			sign_text_model = new SignTextTableModel(group,
				state.getSignText(), user);
			sign_text_table.setModel(sign_text_model);
			delete_group.setEnabled(isGroupDeletable(group));
		} else {
			sign_text_table.setModel(new DefaultTableModel());
			delete_group.setEnabled(false);
		}
	}

	/** Check if a sign group is deletable */
	protected boolean isGroupDeletable(SignGroup group) {
		return !(hasMembers(group) || hasSignText(group));
	}

	/** Check if a sign group has any members */
	protected boolean hasMembers(final SignGroup group) {
		TypeCache<DmsSignGroup> dms_sign_groups =
			state.getDmsSignGroups();
		return null != dms_sign_groups.find(new Checker<DmsSignGroup>(){
			public boolean check(DmsSignGroup g) {
				return g.getSignGroup() == group;
			}
		});
	}

	/** Check if a sign group has any sign text messages */
	protected boolean hasSignText(final SignGroup group) {
		TypeCache<SignText> sign_text = state.getSignText();
		return null != sign_text.find(new Checker<SignText>() {
			public boolean check(SignText t) {
				return t.getSignGroup() == group;
			}
		});
	}

	/** Get the selected sign group */
	protected SignGroup getSelectedGroup() {
		ListSelectionModel s = group_table.getSelectionModel();
		return sign_group_model.getProxy(s.getMinSelectionIndex());
	}

	/** Initialize the sign text table */
	protected void initSignTextTable() {
		final ListSelectionModel s = sign_text_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectSignText();
			}
		};
		sign_text_table.setAutoCreateColumnsFromModel(false);
		sign_text_table.setColumnModel(
			SignTextTableModel.createColumnModel());
		sign_text_table.setPreferredScrollableViewportSize(
			new Dimension(280, 200));
	}

	/** Select a new sign text message */
	protected void selectSignText() {
		SignText st = getSelectedSignText();
		pixel_panel.setPhysicalDimensions(h_mm, v_pix * vp_mm,
			hb_mm, 0, hp_mm, vp_mm);
		pixel_panel.setLogicalDimensions(h_pix, v_pix, c_pix, 0);
		pixel_panel.verifyDimensions();
		if(st != null)
			pixel_panel.setGraphic(renderMessage(st));
		else
			pixel_panel.setGraphic(null);
		delete_text.setEnabled(st != null);
	}

	/** Render a message to a bitmap graphic */
	protected BitmapGraphic renderMessage(SignText st) {
		MultiString multi = new MultiString(st.getMessage());
		TreeMap<Integer, BitmapGraphic> pages = renderPages(multi);
		if(pages.containsKey(0))
			return pages.get(0);
		else
			return null;
	}

	/** Render the pages of a text message */
	protected TreeMap<Integer, BitmapGraphic> renderPages(
		final MultiString multi)
	{
		final Font font = lookupFont();
		if(font == null)
			return new TreeMap<Integer, BitmapGraphic>();
		PixelMapBuilder builder = new PixelMapBuilder(h_pix,
			v_pix, c_pix, font, new PixelMapBuilder.GlyphFinder()
		{
			public Graphic lookupGraphic(int cp)
				throws InvalidMessageException
			{
				Graphic g = lookupGlyph(font, cp);
				if(g != null)
					return g;
				else
					throw new InvalidMessageException(
						"Invalid code point");
			}
		});
		multi.parse(builder);
		return builder.getPixmaps();
	}

	/** Lookup a font for the sign */
	protected Font lookupFont() {
		TypeCache<Font> fonts = state.getFonts();
		Font f = fonts.find(new Checker<Font>() {
			public boolean check(Font f) {
				return f.getWidth() == c_pix &&
					f.getHeight() == v_pix;
			}
		});
		if(f != null || c_pix == 0)
			return f;
		return fonts.find(new Checker<Font>() {
			public boolean check(Font f) {
				return f.getWidth() == 0 &&
					f.getHeight() == v_pix;
			}
		});
	}

	/** Lookup a glyph in the specified font */
	protected Graphic lookupGlyph(final Font f, final int cp) {
		TypeCache<Glyph> glyphs = state.getGlyphs();
		Glyph g = glyphs.find(new Checker<Glyph>() {
			public boolean check(Glyph g) {
				return g.getFont() == f &&
					g.getCodePoint() == cp;
			}
		});
		if(g != null)
			return g.getGraphic();
		else
			return null;
	}

	/** Get the selected sign text message */
	protected SignText getSelectedSignText() {
		SignTextTableModel m = sign_text_model;
		if(m == null)
			return null;
		ListSelectionModel s = sign_text_table.getSelectionModel();
		return m.getProxy(s.getMinSelectionIndex());
	}

	/** Create the travel time panel */
	protected JPanel createTravelTimePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BORDER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Travel template:"));
		box.add(travel);
		box.add(Box.createHorizontalGlue());
		travel.setEnabled(admin);
		panel.add(box);
		panel.add(Box.createVerticalStrut(VGAP));
		plan_table.setAutoCreateColumnsFromModel(false);
		plan_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
		JScrollPane scroll = new JScrollPane(plan_table);
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(scroll);
		box.add(Box.createHorizontalGlue());
		panel.add(box);
		if(admin) {
			box = Box.createHorizontalBox();
			box.add(Box.createHorizontalGlue());
			box.add(am_plan);
			box.add(Box.createHorizontalStrut(8));
			box.add(pm_plan);
			box.add(Box.createHorizontalGlue());
			panel.add(box);
			new ActionJob(this, am_plan) {
				public void perform() throws Exception {
					sign.addTimingPlan(TimingPlan.AM);
				}
			};
			new ActionJob(this, pm_plan) {
				public void perform() throws Exception {
					sign.addTimingPlan(TimingPlan.PM);
				}
			};
		}
		return panel;
	}

	/** Create the configuration panel */
	protected JPanel createConfigurationPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.gridy = 6;
		bag.weightx = 0.6f;
		lay.setConstraints(make, bag);
		panel.add(make);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(model, bag);
		panel.add(model);
		lay.setConstraints(version, bag);
		panel.add(version);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.EAST;
		bag.weightx = 0.5f;
		bag.fill = GridBagConstraints.NONE;
		JLabel label = new JLabel("Access:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridy = GridBagConstraints.RELATIVE;
		label = new JLabel("Type:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Sign width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Horizontal border:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Vertical border:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Legend:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Beacon:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Technology:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Character height:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Character width:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Horizontal pitch:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Vertical pitch:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 2;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.WEST;
		bag.insets.left = 2;
		lay.setConstraints(access, bag);
		panel.add(access);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(type, bag);
		panel.add(type);
		lay.setConstraints(height, bag);
		panel.add(height);
		lay.setConstraints(pHeight, bag);
		panel.add(pHeight);
		lay.setConstraints(width, bag);
		panel.add(width);
		lay.setConstraints(pWidth, bag);
		panel.add(pWidth);
		lay.setConstraints(hBorder, bag);
		panel.add(hBorder);
		lay.setConstraints(vBorder, bag);
		panel.add(vBorder);
		lay.setConstraints(legend, bag);
		panel.add(legend);
		lay.setConstraints(beacon, bag);
		panel.add(beacon);
		lay.setConstraints(tech, bag);
		panel.add(tech);
		lay.setConstraints(cHeight, bag);
		panel.add(cHeight);
		lay.setConstraints(cWidth, bag);
		panel.add(cWidth);
		lay.setConstraints(hPitch, bag);
		panel.add(hPitch);
		lay.setConstraints(vPitch, bag);
		panel.add(vPitch);
		return panel;
	}

	/** Create Ledstar-specific panel */
	protected JPanel createLedstarPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = 2;
		bag.insets.bottom = 2;
		bag.gridx = 0;
		bag.gridy = GridBagConstraints.RELATIVE;
		bag.anchor = GridBagConstraints.EAST;
		panel.add(new JLabel("LDC pot base: "), bag);
		panel.add(new JLabel("Pixel current low threshold: "), bag);
		panel.add(new JLabel("Pixel current high threshold: "), bag);
		panel.add(new JLabel("Bad pixel limit: "), bag);
		bag.gridx = 1;
		bag.anchor = GridBagConstraints.WEST;
		panel.add(ldcPotBase, bag);
		panel.add(currentLow, bag);
		panel.add(currentHigh, bag);
		panel.add(badLimit, bag);
		if(admin) {
			bag.gridx = 2;
			bag.anchor = GridBagConstraints.EAST;
			panel.add(ldcPotBaseSpn, bag);
			panel.add(currentLowSpn, bag);
			panel.add(currentHighSpn, bag);
			panel.add(badLimitSpn, bag);
			bag.gridx = 0;
			bag.gridwidth = 3;
			JButton send = new JButton("Set values");
			panel.add(send, bag);
			new ActionJob(this, send) {
				public void perform() throws Exception {
					setLedstarValues();
				}
			};
		}
		return panel;
	}

	/** Set the Ledstar pixel configuration values on the sign */
	protected void setLedstarValues() throws RemoteException {
		int base = ((Integer)ldcPotBaseSpn.getValue()).intValue();
		int low = ((Integer)currentLowSpn.getValue()).intValue();
		int high = ((Integer)currentHighSpn.getValue()).intValue();
		int bad = ((Integer)badLimitSpn.getValue()).intValue();
		sign.setLdcPotBase(base);
		sign.setPixelCurrentLow(low);
		sign.setPixelCurrentHigh(high);
		sign.setBadPixelLimit(bad);
		sign.notifyUpdate();
	}

	/** Create status panel */
	protected JPanel createStatusPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.setBorder(BORDER);
		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		power_table.setAutoCreateColumnsFromModel(false);
		power_table.setPreferredScrollableViewportSize(
			new Dimension(300, 200));
		JScrollPane scroll = new JScrollPane(power_table);
		box.add(scroll);
		box.add(Box.createVerticalGlue());
		pane.add(box);
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 1;
		bag.gridy = 0;

		// pixelTest button (optional)
		if( pixelTest!=null ) {
			lay.setConstraints(pixelTest, bag);
			panel.add(pixelTest);
			new ActionJob(this, pixelTest) {
				public void perform() throws Exception {
					sign.testPixels();
				}
			};
		}

		bag.gridy = GridBagConstraints.RELATIVE;

		// lamp test button (optional)
		if( lampTest!=null ) {
			lay.setConstraints(lampTest, bag);
			panel.add(lampTest);
			new ActionJob(this, lampTest) {
				public void perform() throws Exception {
					sign.testLamps();
				}
			};
		}

		// fanTest button (optional)
		if( fanTest!=null ) {
			lay.setConstraints(fanTest, bag);
			panel.add(fanTest);
			new ActionJob(this, fanTest) {
				public void perform() throws Exception {
					sign.testFans();
				}
			};
		}

		// get status button (optional)
		if( getStatusButton!=null ) {
			getStatusButton.setToolTipText(
				I18NMessages.get("DMSProperties.GetStatusButton.ToolTip"));
			lay.setConstraints(getStatusButton, bag);
			panel.add(getStatusButton);
			new ActionJob(this, getStatusButton) {
				public void perform() throws Exception {
					sign.getSignMessage();
				}
			};
		}

		// reset button (optional)
		if( resetButton!=null ) {
			resetButton.setToolTipText(
				I18NMessages.get("DMSProperties.ResetButton.ToolTip"));
			lay.setConstraints(resetButton, bag);
			panel.add(resetButton);
			new ActionJob(this, resetButton) {
				public void perform() throws Exception {
					sign.reset();
				}
			};
		}

		// reset modem button (optional)
		if( resetModemButton!=null ) {
			resetModemButton.setToolTipText(
				I18NMessages.get("DMSProperties.ResetModemButton.ToolTip"));
			lay.setConstraints(resetModemButton, bag);
			panel.add(resetModemButton);
			new ActionJob(this, resetModemButton) {
				public void perform() throws Exception {
					sign.reset();
				}
			};
			resetModemButton.setEnabled(false);	//FIXME: remove this line when operation supported in D10 cmsserver
		}

		bag.gridx = 2;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.EAST;
		JLabel label = new JLabel("Bad pixels:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridy = GridBagConstraints.RELATIVE;
		label = new JLabel("Lamp status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Fan status:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Heat tape:");
		bag.insets.top = 10;
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Cabinet temp:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Ambient temp:");
		bag.insets.top = 0;
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Housing temp:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.insets.top = 10;
		label = new JLabel("Operation:");
		lay.setConstraints(label, bag);
		panel.add(label);
		label = new JLabel("Note:");
		lay.setConstraints(label, bag);
		panel.add(label);
		bag.gridx = 3;
		bag.gridy = 0;
		bag.anchor = GridBagConstraints.WEST;
		bag.insets.top = 0;
		bag.insets.left = 2;
		lay.setConstraints(badPixels, bag);
		panel.add(badPixels);
		bag.gridy = GridBagConstraints.RELATIVE;
		lay.setConstraints(lamp, bag);
		panel.add(lamp);
		lay.setConstraints(fan, bag);
		panel.add(fan);
		bag.insets.top = 10;
		lay.setConstraints(heat_tape, bag);
		panel.add(heat_tape);
		lay.setConstraints(cabinetTemp, bag);
		panel.add(cabinetTemp);
		bag.insets.top = 0;
		lay.setConstraints(ambientTemp, bag);
		panel.add(ambientTemp);
		lay.setConstraints(housingTemp, bag);
		panel.add(housingTemp);
		bag.insets.top = 10;
		lay.setConstraints(operation, bag);
		panel.add(operation);
		operation.setForeground(Color.BLACK);
		lay.setConstraints(dms_note, bag);
		panel.add(dms_note);
		pane.add(panel);
		return pane;
	}

	/** Create a brightness table panel */
	protected JPanel createBrightnessPanel() {
		GridBagLayout lay = new GridBagLayout();
		JPanel panel = new JPanel(lay);
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.fill = GridBagConstraints.BOTH;
		bag.gridwidth = 4;
		bag.weightx = 1;
		bag.weighty = 1;
		b_table.setBorder(b_table.new ScaleBorder(16));
		lay.setConstraints(b_table, bag);
		panel.add(b_table);
		bag.weightx = 0.1f;
		bag.weighty = 0;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.gridwidth = 1;
		bag.fill = GridBagConstraints.NONE;
		bag.insets.top = 6;
		ButtonGroup group = new ButtonGroup();
		group.add(con_photocell);
		new ActionJob(this, con_photocell) {
			public void perform() throws Exception {
				sign.activateManualBrightness(false);
			}
		};
		lay.setConstraints(con_photocell, bag);
		panel.add(con_photocell);
		bag.gridx = 1;
		group.add(con_manual);
		new ActionJob(this, con_manual) {
			public void perform() throws Exception {
				sign.activateManualBrightness(true);
			}
		};
		lay.setConstraints(con_manual, bag);
		panel.add(con_manual);

		return panel;
	}

	/** Update the form with the current state of the sign */
	protected void doUpdate() throws RemoteException {
		super.doUpdate();
		camera.setSelectedItem(state.lookupCamera(sign.getCamera()));
		String t = sign.getTravel();
		Color color = Color.GRAY;
		if(sign.isActive())
			color = OK;
		make.setForeground(color);
		model.setForeground(color);
		version.setForeground(color);
		access.setForeground(color);
		type.setForeground(color);
		height.setForeground(color);
		pHeight.setForeground(color);
		width.setForeground(color);
		pWidth.setForeground(color);
		hBorder.setForeground(color);
		vBorder.setForeground(color);
		legend.setForeground(color);
		beacon.setForeground(color);
		tech.setForeground(color);
		cHeight.setForeground(color);
		cWidth.setForeground(color);
		hPitch.setForeground(color);
		vPitch.setForeground(color);
		badPixels.setForeground(color);
		ldcPotBase.setForeground(color);
		currentLow.setForeground(color);
		currentHigh.setForeground(color);
		badLimit.setForeground(color);
		lamp.setForeground(color);
		fan.setForeground(color);
		heat_tape.setForeground(color);
		cabinetTemp.setForeground(color);
		ambientTemp.setForeground(color);
		housingTemp.setForeground(color);
		dms_note.setForeground(color);
		b_table.doUpdate();
		travel.setText(t);
	}

	/** Get the station index for the given item */
	protected int stationIndex(Object item) {
		if(item instanceof String) {
			String st = ((String)item).substring(0, 4).trim();
			try { return Integer.parseInt(st); }
			catch(NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	/** Refresh the status of the object */
	protected void doStatus() throws RemoteException {
		super.doStatus();
		make.setText(sign.getMake());
		model.setText(sign.getModel());
		version.setText(sign.getVersion());
		access.setText(sign.getSignAccess());
		type.setText(sign.getSignMatrixTypeDescription());
		height.setText(formatMM(sign.getSignHeight()));
		pHeight.setText(formatPixels(sign.getSignHeightPixels()));
		width.setText(formatMM(sign.getSignWidth()));
		pWidth.setText(formatPixels(sign.getSignWidthPixels()));
		hBorder.setText(formatMM(sign.getHorizontalBorder()));
		vBorder.setText(formatMM(sign.getVerticalBorder()));
		legend.setText(sign.getSignLegend());
		beacon.setText(sign.getBeaconType());
		tech.setText(sign.getSignTechnology());
		cWidth.setText(formatPixels(sign.getCharacterWidthPixels()));
		cHeight.setText(formatPixels(sign.getCharacterHeightPixels()));
		hPitch.setText(formatMM(sign.getHorizontalPitch()));
		vPitch.setText(formatMM(sign.getVerticalPitch()));
		StatusTableModel m = new StatusTableModel(
			sign.getPowerSupplyTable());
		power_table.setColumnModel(m.createColumnModel());
		power_table.setDefaultRenderer(Object.class, m.getRenderer());
		power_table.setModel(m);
		badPixels.setText(String.valueOf(sign.getPixelFailureCount()));
		ldcPotBase.setText(formatNumber(sign.getLdcPotBase()));
		currentLow.setText(formatMA(sign.getPixelCurrentLow()));
		currentHigh.setText(formatMA(sign.getPixelCurrentHigh()));
		badLimit.setText(formatNumber(sign.getBadPixelLimit()));
		lamp.setText(sign.getLampStatus());
		fan.setText(sign.getFanStatus());
		heat_tape.setText(sign.getHeatTapeStatus());
		cabinetTemp.setText(formatTemp(sign.getMinCabinetTemp(),
			sign.getMaxCabinetTemp()));
		ambientTemp.setText(formatTemp(sign.getMinAmbientTemp(),
			sign.getMaxAmbientTemp()));
		housingTemp.setText(formatTemp(sign.getMinHousingTemp(),
			sign.getMaxHousingTemp()));
		dms_note.setText(sign.getUserNote());
		operation.setText(sign.getOperation());
		b_table.doStatus();
		if(sign.isManualBrightness())
			con_manual.setSelected(true);
		else
			con_photocell.setSelected(true);

		h_pix = sign.getSignWidthPixels();
		v_pix = sign.getLineHeightPixels();
		c_pix = sign.getCharacterWidthPixels();
		hp_mm = sign.getHorizontalPitch();
		vp_mm = sign.getVerticalPitch();
		h_mm = sign.getSignWidth();
		hb_mm = sign.getHorizontalBorder();
		selectSignText();
	}

	/** Apply button is pressed */
	protected void applyPressed() throws Exception {
		super.applyPressed();
		sign.setCamera(getCameraName((Camera)camera.getSelectedItem()));
		if(b_table.isModified()) {
			int[] table = b_table.getTableData();
			sign.setBrightnessTable(table);
		}
		sign.setTravel(travel.getText());
		sign.notifyUpdate();
	}

	/** Get the sign id */
	protected String getId() {
		String signId = "";
		try {
			signId = sign.getId();
		} catch (RemoteException ex) {
			System.err.println("DMSProperties.getId(): exception="+ex);
		}
		return signId;
	}
}
