/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.CommLink;
import static us.mn.state.dot.tms.CommLink.MAX_TIMEOUT_MS;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.units.Interval;

/**
 * Table model for comm links
 *
 * @author Douglas Lau
 */
public class CommLinkModel extends ProxyTableModel<CommLink> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CommLink> descriptor(Session s) {
		return new ProxyDescriptor<CommLink>(
			s.getSonarState().getConCache().getCommLinks(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CommLink>> createColumns() {
		ArrayList<ProxyColumn<CommLink>> cols =
			new ArrayList<ProxyColumn<CommLink>>(6);
		cols.add(new ProxyColumn<CommLink>("comm.link", 90) {
			public Object getValueAt(CommLink cl) {
				return cl.getName();
			}
		});
		cols.add(new ProxyColumn<CommLink>("device.description", 220) {
			public Object getValueAt(CommLink cl) {
				return cl.getDescription();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "description");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setDescription(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.uri", 280) {
			public Object getValueAt(CommLink cl) {
				return cl.getUri();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "uri");
			}
			public void setValueAt(CommLink cl, Object value) {
				cl.setUri(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<CommLink>("device.status", 44) {
			public Object getValueAt(CommLink cl) {
				return cl.getStatus();
			}
			protected TableCellRenderer createCellRenderer() {
				return new StatusCellRenderer();
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.protocol", 140) {
			public Object getValueAt(CommLink cl) {
//				return PROTOCOLS.get(cl.getProtocol());
				return CommProtocol.fromOrdinal(
					cl.getProtocol());
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "protocol");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof CommProtocol) {
					CommProtocol cp = (CommProtocol) value;
					cl.setProtocol((short) cp.ordinal());
				}
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<CommProtocol> cbx = new JComboBox
					<CommProtocol>(CommProtocol.values());
				return new DefaultCellEditor(cbx);
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.poll_enabled", 56,
			Boolean.class)
		{
			public Object getValueAt(CommLink cl) {
				return cl.getPollEnabled();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "pollEnabled");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof Boolean)
					cl.setPollEnabled((Boolean)value);
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.poll_period", 60){
			public Object getValueAt(CommLink cl) {
				Interval p = new Interval(cl.getPollPeriod());
				for (Interval per: CommLink.VALID_PERIODS) {
					if (p.equals(per))
						return per;
				}
				return p;
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "pollPeriod");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof Interval) {
					Interval p = (Interval)value;
					cl.setPollPeriod(p.round(
						Interval.Units.SECONDS));
				}
			}
			protected TableCellEditor createCellEditor() {
				return new PollPeriodCellEditor();
			}
		});
		cols.add(new ProxyColumn<CommLink>("comm.link.timeout", 60) {
			public Object getValueAt(CommLink cl) {
				return cl.getTimeout();
			}
			public boolean isEditable(CommLink cl) {
				return canUpdate(cl, "timeout");
			}
			public void setValueAt(CommLink cl, Object value) {
				if (value instanceof Integer)
					cl.setTimeout((Integer)value);
			}
			protected TableCellEditor createCellEditor() {
				return new TimeoutCellEditor(MAX_TIMEOUT_MS);
			}
		});
		return cols;
	}

	/** Create a new comm link table model */
	public CommLinkModel(Session s) {
		super(s, descriptor(s));
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 8;
	}

	/** Get the row height */
	@Override
	public int getRowHeight() {
		return 24;
	}
}
