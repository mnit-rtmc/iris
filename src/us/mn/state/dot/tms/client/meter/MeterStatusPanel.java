/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2006  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.meter;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.TmsSelectionEvent;
import us.mn.state.dot.tms.client.TmsSelectionListener;

/**
 * The MeterStatusPanel provides a GUI representation for RampMeter status
 * information.
 *
 * FIXME: This class was cut-and-pasted from RampMeterProperties.java
 * and has diverged significantly. These classes should be merged somehow.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MeterStatusPanel extends JPanel implements TmsSelectionListener {

	/** Remote ramp meter object */
	protected MeterProxy proxy = null;

	/** ID component */
	protected final JTextField txtId = new JTextField();

	/** Camera component */
	protected final JTextField txtCamera = new JTextField();

	/** Location component */
	protected final JTextField txtLocation = new JTextField();

	/** Status component */
	protected final JTextField txtStatus = new JTextField();

	/** Metering on radio button */
	protected final JRadioButton meter_on = new JRadioButton("On");

	/** Metering off radio button */
	protected final JRadioButton meter_off = new JRadioButton("Off");

	/** Cycle time component */
	protected final JTextField txtCycle = new JTextField();

	/** Queue component */
	protected final JTextField txtQueue = new JTextField();

	/** Queue shrink button */
	protected final JButton shrink = new JButton("Shrink");

	/** Queue grow button */
	protected final JButton grow = new JButton("Grow");

	/** Meter locked button */
	protected final JCheckBox locked = new JCheckBox("Locked:");

	/** Name of user who locked the meter */
	protected final JTextField txtLock = new JTextField();

	/** Reason the meter was locked */
	protected final JTextArea lockReason = new JTextArea();

	protected final JButton dataButton = new JButton("Data");

	protected final MeterHandler handler;

	protected final TmsConnection connection;

	/** Create a new MeterStatusPanel */
	public MeterStatusPanel(TmsConnection connection, MeterHandler handler)
	{
		super(new GridBagLayout());
		this.handler = handler;
		this.connection = connection;
		lockReason.setLineWrap( true );
		lockReason.setEditable( false );
		lockReason.setBackground(getBackground());
		handler.getSelectionModel().addTmsSelectionListener( this );
		setBorder(BorderFactory.createTitledBorder(
			"Selected Ramp Meter"));
		setEnabled(false);
		GridBagConstraints bag = new GridBagConstraints();
		bag.gridx = 0;
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("ID"), bag);
		bag.gridx = 2;
		add(new JLabel("Camera"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new JLabel("Location"), bag);
		bag.gridy = 2;
		add(new JLabel("Status"), bag);
		bag.gridy = 3;
		add(new JLabel("Metering"), bag);
		bag.gridy = 4;
		add(new JLabel("Cycle Time"), bag);
		bag.gridy = 5;
		bag.gridheight = 2;
		add(new JLabel("Queue"), bag);
		bag.gridy = 7;
		bag.gridheight = 1;
		add(locked, bag);
		bag.gridy = 8;
		add(new JLabel("Reason"), bag);
		bag.gridx = 1;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		txtId.setEditable(false);
		add(txtId, bag);
		bag.gridx = 3;
		txtCamera.setEditable(false);
		add(txtCamera, bag);
		bag.gridx = 1;
		bag.gridy = 1;
		bag.gridwidth = 3;
		txtLocation.setEditable(false);
		add(txtLocation, bag);
		bag.gridy = 2;
		txtStatus.setEditable(false);
		add(txtStatus, bag);
		bag.gridy = 3;
		bag.gridwidth = 1;
		add(meter_on, bag);
		bag.gridx = 2;
		bag.gridwidth = 2;
		add(meter_off, bag);
		bag.gridx = 1;
		bag.gridy = 4;
		bag.gridwidth = 3;
		txtCycle.setEditable(false);
		add(txtCycle, bag);
		bag.gridy = 5;
		bag.gridheight = 2;
		bag.gridwidth = 1;
		txtQueue.setEditable(false);
		add(txtQueue, bag);
		bag.gridy = 7;
		bag.gridheight = 1;
		bag.gridwidth = 3;
		txtLock.setEditable(false);
		add(txtLock, bag);
		bag.gridy = 8;
		bag.fill = GridBagConstraints.BOTH;
		JScrollPane scroll = new JScrollPane(lockReason);
		scroll.setBorder(null);
		scroll.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroll, bag);
		bag.gridx = 0;
		bag.gridy = 9;
		bag.gridwidth = 4;
		bag.fill = GridBagConstraints.NONE;
		bag.anchor = GridBagConstraints.CENTER;
		add(dataButton, bag);
		bag.gridx = 2;
		bag.gridy = 5;
		bag.gridwidth = 2;
		add(shrink, bag);
		bag.gridy = 6;
		add(grow, bag);
	}

	/** Enable or disable the status panel */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		meter_on.setEnabled(enabled);
		meter_off.setEnabled(enabled);
		shrink.setEnabled(enabled);
		grow.setEnabled(enabled);
		locked.setEnabled(enabled);
		lockReason.setEnabled(enabled);
	}

	/** Select a new meter to display */
	public void setMeter(final MeterProxy p) {
		proxy = p;
		refreshUpdate();
		refreshStatus();
		setEnabled(true);
	}

	/** Clear the meter status panel */
	protected void clearMeter() {
		setEnabled(false);
		txtId.setText("");
		txtCamera.setText("");
		txtLocation.setText("");
		txtStatus.setText("");
		txtLock.setText("");
		lockReason.setText("");
		txtCycle.setText("");
		txtQueue.setText("");
	}

	/** Selection changed to another meter */
	public void selectionChanged(TmsSelectionEvent e) {
		final TMSObject o = e.getSelected();
		if(o instanceof MeterProxy) {
			MeterProxy p = (MeterProxy)o;
			if(p != null)
				setMeter(p);
			else
				clearMeter();
		}
	}

	/** Refresh the update status of the selected ramp meter */
	public void refreshUpdate() {
		final MeterProxy p = proxy;
		dataButton.setAction(new MeterDataAction(p,
			connection.getDesktop()));
		shrink.setAction(new ShrinkQueueAction(p));
		grow.setAction(new GrowQueueAction(p));
		meter_on.setAction(new TurnOnAction(p));
		meter_off.setAction(new TurnOffAction(p));
		locked.setAction(new LockMeterAction(p,
			connection.getDesktop()));
		txtId.setText(p.getId());
		txtCamera.setText(p.getCameraId());
		txtLocation.setText(p.getLocationString());
	}

	/** Refresh the status of the ramp meter */
	public void refreshStatus() {
		final MeterProxy p = proxy;
		Color color = Color.GRAY;
		String UNKNOWN = "???";
		String s_status = UNKNOWN;
		boolean metering = false;
		boolean meter_en = false;
		boolean isLocked = false;
		String s_lockName = "";
		String s_lockReason = "";
		String s_cycle = UNKNOWN;
		String s_queue = UNKNOWN;
		try {
			if(p == null)
				return;
			s_status = p.getStatus();
			if(s_status == null) {
				s_status = UNKNOWN;
				return;
			}
			isLocked = p.isLocked();
			if(isLocked) {
				RampMeterLock lock = p.getLock();
				s_lockName = lock.getUser();
				if(s_lockName.indexOf('.') > 0) {
					s_lockName = s_lockName.substring(0,
						s_lockName.indexOf('.'));
				}
				s_lockReason = lock.getReason();
			}
			int mode = p.getControlMode();
			if(mode == RampMeter.MODE_STANDBY ||
				mode == RampMeter.MODE_CENTRAL)
			{
				meter_en = true;
			}
			if(p.isMetering()) {
				metering = true;
				int r = p.getReleaseRate();
				int i_cycle = Math.round(36000.0f / r);
				s_cycle = (i_cycle / 10) + "." +
					(i_cycle % 10) + " seconds";
			} else
				s_cycle = "N/A";
			if(p.queueExists())
				s_queue = "Yes";
			else
				s_queue = "No";
			color = null;
		} finally {
			txtStatus.setBackground(color);
			txtStatus.setText(s_status);
			locked.setSelected(isLocked);
			locked.setEnabled(meter_en);
			txtLock.setBackground(color);
			txtLock.setText(s_lockName);
			lockReason.setText(s_lockReason);
			meter_on.setEnabled(meter_en & !metering);
			meter_on.setSelected(metering);
			meter_off.setEnabled(meter_en & metering);
			meter_off.setSelected(!metering);
			txtCycle.setBackground(color);
			txtCycle.setText(s_cycle);
			txtQueue.setBackground(color);
			txtQueue.setText(s_queue);
			shrink.setEnabled(meter_en && metering);
			grow.setEnabled(meter_en && metering);
			repaint();
		}
	}

	public void dispose() {
		handler.getSelectionModel().removeTmsSelectionListener( this );
		clearMeter();
		this.removeAll();
	}
}
