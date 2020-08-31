/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.util.ArrayList;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.CommConfig;
import static us.mn.state.dot.tms.CommConfig.MAX_TIMEOUT_MS;
import us.mn.state.dot.tms.CommConfigHelper;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.units.Interval;

/**
 * Table model for comm configurations.
 *
 * @author Douglas Lau
 */
public class CommConfigModel extends ProxyTableModel<CommConfig> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CommConfig> descriptor(Session s) {
		return new ProxyDescriptor<CommConfig>(
			s.getSonarState().getConCache().getCommConfigs(),
			false,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** Column for a time period */
	static abstract private class PeriodColumn
		extends ProxyColumn<CommConfig>
	{
		private final Interval[] periods;
		private PeriodColumn(String tid, Interval[] per, int w) {
			super(tid, w);
			periods = per;
		}
		@Override
		public final Object getValueAt(CommConfig cc) {
			Interval p = new Interval(getPeriodSec(cc));
			for (Interval per: periods) {
				if (p.equals(per))
					return per;
			}
			return p;
		}
		abstract protected int getPeriodSec(CommConfig cc);
		@Override
		public final void setValueAt(CommConfig cc, Object value) {
			if (value instanceof Interval) {
				Interval p = (Interval) value;
				setPeriodSec(cc,
					p.round(Interval.Units.SECONDS));
			}
		}
		abstract protected void setPeriodSec(CommConfig cc, int s);
		@Override
		protected final TableCellEditor createCellEditor() {
			return new PeriodCellEditor(periods);
		}
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CommConfig>> createColumns() {
		ArrayList<ProxyColumn<CommConfig>> cols =
			new ArrayList<ProxyColumn<CommConfig>>(9);
		cols.add(new ProxyColumn<CommConfig>("comm.config", 60) {
			public Object getValueAt(CommConfig cc) {
				return cc.getName();
			}
		});
		cols.add(new ProxyColumn<CommConfig>("device.description", 220)
		{
			public Object getValueAt(CommConfig cc) {
				return cc.getDescription();
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "description");
			}
			public void setValueAt(CommConfig cc, Object value) {
				cc.setDescription(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<CommConfig>("comm.config.protocol",
			140)
		{
			public Object getValueAt(CommConfig cc) {
				return CommProtocol.fromOrdinal(
					cc.getProtocol());
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "protocol");
			}
			public void setValueAt(CommConfig cc, Object value) {
				if (value instanceof CommProtocol) {
					CommProtocol cp = (CommProtocol) value;
					cc.setProtocol((short) cp.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<CommProtocol> cbx = new JComboBox
					<CommProtocol>(CommProtocol
					.valuesSorted());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<CommConfig>("comm.config.modem", 56,
			Boolean.class)
		{
			public Object getValueAt(CommConfig cc) {
				return cc.getModem();
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "modem");
			}
			public void setValueAt(CommConfig cc, Object value) {
				if (value instanceof Boolean)
					cc.setModem((Boolean) value);
			}
		});
		cols.add(new ProxyColumn<CommConfig>("comm.config.timeout_ms",
			60)
		{
			public Object getValueAt(CommConfig cc) {
				return cc.getTimeoutMs();
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "timeoutMs");
			}
			public void setValueAt(CommConfig cc, Object value) {
				if (value instanceof Integer)
					cc.setTimeoutMs((Integer) value);
			}
			protected TableCellEditor createCellEditor() {
				return new TimeoutCellEditor(MAX_TIMEOUT_MS);
			}
		});
		cols.add(new PeriodColumn("comm.config.poll_period_sec",
			CommConfig.VALID_PERIODS, 92)
		{
			protected int getPeriodSec(CommConfig cc) {
				return cc.getPollPeriodSec();
			}
			protected void setPeriodSec(CommConfig cc, int s) {
				cc.setPollPeriodSec(s);
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "pollPeriodSec");
			}
		});
		cols.add(new PeriodColumn("comm.config.long_poll_period_sec",
			CommConfig.VALID_PERIODS, 92)
		{
			protected int getPeriodSec(CommConfig cc) {
				return cc.getLongPollPeriodSec();
			}
			protected void setPeriodSec(CommConfig cc, int s) {
				cc.setLongPollPeriodSec(s);
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "longPollPeriodSec");
			}
		});
		cols.add(new PeriodColumn("comm.config.idle_disconnect_sec",
			CommConfig.VALID_DISCONNECT, 108)
		{
			protected int getPeriodSec(CommConfig cc) {
				return cc.getIdleDisconnectSec();
			}
			protected void setPeriodSec(CommConfig cc, int s) {
				cc.setIdleDisconnectSec(s);
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "idleDisconnectSec");
			}
		});
		cols.add(new PeriodColumn(
			"comm.config.no_response_disconnect_sec",
			CommConfig.VALID_DISCONNECT, 108)
		{
			protected int getPeriodSec(CommConfig cc) {
				return cc.getNoResponseDisconnectSec();
			}
			protected void setPeriodSec(CommConfig cc, int s) {
				cc.setNoResponseDisconnectSec(s);
			}
			public boolean isEditable(CommConfig cc) {
				return canWrite(cc, "noResponseDisconnectSec");
			}
		});
		return cols;
	}

	/** Create a new comm config table model */
	public CommConfigModel(Session s) {
		super(s, descriptor(s), 8, 24);
	}

	/** Create an object with the given name */
	@Override
	public void createObject(String n) {
		String name = CommConfigHelper.createUniqueName();
		if (name != null)
			super.createObject(name);
	}
}
