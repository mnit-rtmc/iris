/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gate;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sched.Job;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmState;
import static us.mn.state.dot.tms.GateArmArray.MAX_ARMS;
import us.mn.state.dot.tms.client.Session;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for gate arms within an array.
 *
 * @author Douglas Lau
 */
public class GateArmTableModel extends AbstractTableModel
	implements ProxyListener<GateArm>
{
	/** User session */
	private final Session session;

	/** Proxy type cache */
	private final TypeCache<GateArm> cache;

	/** Proxy columns */
	private final ArrayList<ProxyColumn<GateArm>> columns;

	/** Rows of gate arms */
	private final GateArm[] rows = new GateArm[MAX_ARMS];

	/** Create the columns in the model */
	private ArrayList<ProxyColumn<GateArm>> createColumns() {
		ArrayList<ProxyColumn<GateArm>> cols =
			new ArrayList<ProxyColumn<GateArm>>(4);
		cols.add(new ProxyColumn<GateArm>("gate.arm.index", 36,
			Integer.class)
		{
			public Object getValueAt(int row){
				return row + 1;
			}
			public Object getValueAt(GateArm ga) {
				return ga.getIdx();
			}
		});
		cols.add(new ProxyColumn<GateArm>("device.name", 74) {
			public Object getValueAt(GateArm ga) {
				return ga.getName();
			}
			public boolean isEditable(GateArm ga) {
				return (ga == null) && canAdd();
			}
			public void setValueAt(GateArm ga,int row,Object value){
				String v = value.toString().trim();
				if(v.length() > 0)
					createGateArm(v, row + 1);
			}
		});
		cols.add(new ProxyColumn<GateArm>("device.notes", 200) {
			public Object getValueAt(GateArm ga) {
				return ga.getNotes();
			}
			public boolean isEditable(GateArm ga) {
				return canUpdate(ga, "notes");
			}
			public void setValueAt(GateArm ga, Object value) {
				ga.setNotes(value.toString().trim());
			}
		});
		cols.add(new ProxyColumn<GateArm>("controller.version", 100) {
			public Object getValueAt(GateArm ga) {
				return ga.getVersion();
			}
		});
		cols.add(new ProxyColumn<GateArm>("gate.arm.state", 100) {
			public Object getValueAt(GateArm ga) {
				return GateArmState.fromOrdinal(
					ga.getArmState());
			}
		});
		return cols;
	}

	/** Check if the user can add a proxy */
	private boolean canAdd(String n) {
		return session.canAdd(GateArm.SONAR_TYPE, n);
	}

	/** Check if the user can add a proxy */
	private boolean canAdd() {
		return canAdd("oname");
	}

	/** Check if the user can update a proxy */
	private boolean canUpdate(GateArm proxy, String aname) {
		return session.canUpdate(proxy, aname);
	}

	/** Check if the user can remove a proxy */
	public boolean canRemove(GateArm proxy) {
		return session.canRemove(proxy);
	}

	/** Create a new gate arm */
	private void createGateArm(String name, int idx) {
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("ga_array", ga_array);
		attrs.put("idx", new Integer(idx));
		cache.createObject(name, attrs);
	}

	/** Gate arm array */
	private final GateArmArray ga_array;

	/** Create a new gate arm table model */
	public GateArmTableModel(Session s, GateArmArray ga) {
		session = s;
		cache = s.getSonarState().getGateArms();
		columns = createColumns();
		ga_array = ga;
	}

	/** Initialize the proxy table model. This cannot be done in the
	 * constructor because subclasses may not be fully constructed. */
	public void initialize() {
		cache.addProxyListener(this); // add all children to table model
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** Add a new proxy to the table model */
	@Override public final void proxyAdded(final GateArm proxy) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyAddedSlow(proxy);
			}
		});
	}

	/** Add a new proxy to the table model */
	private void proxyAddedSlow(GateArm proxy) {
		final int row = doProxyAdded(proxy);
		if(row >= 0) {
			runSwing(new Runnable() {
				public void run() {
					fireTableRowsUpdated(row, row);
				}
			});
		}
	}

	/** Add a new proxy to the table model */
	private int doProxyAdded(GateArm proxy) {
		if(proxy.getGaArray() == ga_array) {
			int row = proxy.getIdx() - 1;
			if(row >= 0 && row < MAX_ARMS) {
				rows[row] = proxy;
				return row;
			}
		}
		return -1;
	}

	/** Enumeration of all proxies is complete */
	@Override public void enumerationComplete() {
		// Nothing to do
	}

	/** Remove a proxy from the table model */
	@Override public final void proxyRemoved(final GateArm proxy) {
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyRemovedSlow(proxy);
			}
		});
	}

	/** Remove a proxy from the table model */
	private void proxyRemovedSlow(GateArm proxy) {
		final int row = doProxyRemoved(proxy);
		if(row >= 0) {
			runSwing(new Runnable() {
				public void run() {
					fireTableRowsUpdated(row, row);
				}
			});
		}
	}

	/** Remove a proxy from the table model */
	private int doProxyRemoved(GateArm proxy) {
		int row = proxy.getIdx() - 1;
		if(row >= 0 && row < MAX_ARMS) {
			rows[row] = null;
			return row;
		}
		return -1;
	}

	/** Change a proxy in the table model */
	@Override public final void proxyChanged(final GateArm proxy,
		final String attrib)
	{
		// Don't hog the SONAR TaskProcessor thread
		WORKER.addJob(new Job() {
			public void perform() {
				proxyChangedSlow(proxy, attrib);
			}
		});
	}

	/** Change a proxy in the table model */
	private void proxyChangedSlow(GateArm proxy, String attrib) {
		final int row = proxy.getIdx() - 1;
		if(row >= 0 && row < MAX_ARMS) {
			runSwing(new Runnable() {
				public void run() {
					fireTableRowsUpdated(row, row);
				}
			});
		}
	}

	/** Get the value at the specified cell */
	@Override public Object getValueAt(int row, int col) {
		ProxyColumn<GateArm> pc = getProxyColumn(col);
		if(pc != null) {
			GateArm proxy = getProxy(row);
			if(proxy != null)
				return pc.getValueAt(proxy);
			else
				return pc.getValueAt(row);
		}
		return null;
	}

	/** Get the proxy column at the given column index */
	public ProxyColumn<GateArm> getProxyColumn(int col) {
		if(col >= 0 && col < columns.size())
			return columns.get(col);
		else
			return null;
	}

	/** Get the count of rows in the table */
	@Override public int getRowCount() {
		return MAX_ARMS;
	}

	/** Get the count of columns in the table */
	@Override public int getColumnCount() {
		return columns.size();
	}

	/** Check if the specified cell is editable */
	@Override public boolean isCellEditable(int row, int col) {
		ProxyColumn<GateArm> pc = getProxyColumn(col);
		return pc != null && pc.isEditable(getProxy(row));
	}

	/** Set the value at the specified cell */
	@Override public void setValueAt(Object value, int row, int col) {
		ProxyColumn<GateArm> pc = getProxyColumn(col);
		if(pc != null)
			pc.setValueAt(getProxy(row), row, value);
	}

	/** Get the proxy at the specified row */
	public GateArm getProxy(int row) {
		if(row >= 0 && row < MAX_ARMS)
			return rows[row];
		else
			return null;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for(int i = 0; i < columns.size(); ++i)
			columns.get(i).addColumn(m, i);
		return m;
	}
}
