/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2017  Minnesota Department of Transportation
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
import java.util.Comparator;
import java.util.HashMap;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmState;
import static us.mn.state.dot.tms.GateArmArray.MAX_ARMS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.comm.ControllerForm;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * Table model for gate arms within an array.
 *
 * @author Douglas Lau
 */
public class GateArmTableModel extends ProxyTableModel<GateArm> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<GateArm> descriptor(Session s) {
		return new ProxyDescriptor<GateArm>(
			s.getSonarState().getGateArms(), true
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<GateArm>> createColumns() {
		ArrayList<ProxyColumn<GateArm>> cols =
			new ArrayList<ProxyColumn<GateArm>>(4);
		cols.add(new ProxyColumn<GateArm>("gate.arm.index", 36,
			Integer.class)
		{
			public Object getValueAt(GateArm ga) {
				return ga.getIdx();
			}
		});
		cols.add(new ProxyColumn<GateArm>("device.name", 74) {
			public Object getValueAt(GateArm ga) {
				return ga.getName();
			}
		});
		cols.add(new ProxyColumn<GateArm>("device.notes", 200) {
			public Object getValueAt(GateArm ga) {
				return ga.getNotes();
			}
			public boolean isEditable(GateArm ga) {
				return canWrite(ga, "notes");
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

	/** Gate arm array */
	private final GateArmArray ga_array;

	/** Create a new gate arm table model */
	public GateArmTableModel(Session s, GateArmArray ga) {
		super(s, descriptor(s), MAX_ARMS);
		ga_array = ga;
	}

	/** Get a proxy comparator */
	@Override
	protected Comparator<GateArm> comparator() {
		return new Comparator<GateArm>() {
			public int compare(GateArm a, GateArm b) {
				Integer aa = Integer.valueOf(a.getIdx());
				Integer bb = Integer.valueOf(b.getIdx());
				int c = aa.compareTo(bb);
				if (c == 0) {
					String an = a.getName();
					String bn = b.getName();
					return an.compareTo(bn);
				} else
					return c;
			}
		};
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(GateArm proxy) {
		return proxy.getGaArray() == ga_array;
	}

	/** Show the properties form for a proxy */
	@Override
	public void showPropertiesForm(GateArm proxy) {
		Controller c = proxy.getController();
		if (c != null) {
			SmartDesktop sd = session.getDesktop();
			sd.show(new ControllerForm(session, c));
		}
	}

	/** Create a new gate arm */
	@Override
	public void createObject(String name) {
		int idx = getRowCount() + 1;
		if (idx < MAX_ARMS) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("ga_array", ga_array);
			attrs.put("idx", new Integer(idx));
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Check if the user can remove a proxy */
	@Override
	public boolean canRemove(GateArm proxy) {
		return super.canRemove(proxy) && isLastArm(proxy);
	}

	/** Check if a proxy is the last in list */
	private boolean isLastArm(GateArm proxy) {
		return proxy == getRowProxy(getRowCount() - 1);
	}
}
