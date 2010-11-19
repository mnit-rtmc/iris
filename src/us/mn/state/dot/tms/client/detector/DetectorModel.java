/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.ControllerForm;

/**
 * Table model for detectors
 *
 * @author Douglas Lau
 */
public class DetectorModel extends ProxyTableModel<Detector> {

	/** List of all lane types */
	static protected final LinkedList<String> LANE_TYPES =
		new LinkedList<String>();
	static {
		for(String lt: LaneType.getDescriptions())
			LANE_TYPES.add(lt);
	}

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Detector>("Detector", 60) {
			public Object getValueAt(Detector d) {
				return d.getName();
			}
			public boolean isEditable(Detector d) {
				return (d == null) && canAdd();
			}
			public void setValueAt(Detector d, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					cache.createObject(v);
			}
		},
		new ProxyColumn<Detector>("Label", 140) {
			public Object getValueAt(Detector d) {
				return DetectorHelper.getLabel(d);
			}
		},
		new ProxyColumn<Detector>("Lane Type", 80) {
			public Object getValueAt(Detector d) {
				return LANE_TYPES.get(d.getLaneType());
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d);
			}
			public void setValueAt(Detector d, Object value) {
				d.setLaneType((short)LANE_TYPES.indexOf(value));
			}
			protected TableCellEditor createCellEditor() {
				JComboBox combo = new JComboBox(
					LaneType.getDescriptions());
				return new DefaultCellEditor(combo);
			}
		},
		new ProxyColumn<Detector>("Lane #", 60, Short.class) {
			public Object getValueAt(Detector d) {
				return d.getLaneNumber();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d);
			}
			public void setValueAt(Detector d, Object value) {
				if(value instanceof Number) {
					d.setLaneNumber(
						((Number)value).shortValue());
				}
			}
		},
		new ProxyColumn<Detector>("Abandoned", 60, Boolean.class) {
			public Object getValueAt(Detector d) {
				return d.getAbandoned();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d, "abandoned");
			}
			public void setValueAt(Detector d, Object value) {
				if(value instanceof Boolean)
					d.setAbandoned((Boolean)value);
			}
		},
		new ProxyColumn<Detector>("Force Fail", 60, Boolean.class) {
			public Object getValueAt(Detector d) {
				return d.getForceFail();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d, "forceFail");
			}
			public void setValueAt(Detector d, Object value) {
				if(value instanceof Boolean)
					d.setForceFail((Boolean)value);
			}
		},
		new ProxyColumn<Detector>("Field Len", 60, Float.class) {
			public Object getValueAt(Detector d) {
				return d.getFieldLength();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d, "fieldLength");
			}
			public void setValueAt(Detector d, Object value) {
				if(value instanceof Number) {
					d.setFieldLength(
						((Number)value).floatValue());
				}
			}
		},
		new ProxyColumn<Detector>("Fake", 180) {
			public Object getValueAt(Detector d) {
				return d.getFake();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d);
			}
			public void setValueAt(Detector d, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					d.setFake(v);
				else
					d.setFake(null);
			}
		},
		new ProxyColumn<Detector>("Notes", 180) {
			public Object getValueAt(Detector d) {
				return d.getNotes();
			}
			public boolean isEditable(Detector d) {
				return canUpdate(d);
			}
			public void setValueAt(Detector d, Object value) {
				String v = value.toString().trim();
				d.setNotes(v);
			}
		}
	    };
	}

	/** Create a new detector table model */
	public DetectorModel(Session s) {
		super(s, s.getSonarState().getDetCache().getDetectors());
	}

	/** Create an empty set of proxies */
	protected TreeSet<Detector> createProxySet() {
		return new TreeSet<Detector>(
			new Comparator<Detector>() {
				public int compare(Detector a, Detector b) {
					return DetectorHelper.compare(a, b);
				}
			}
		);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Detector.SONAR_TYPE;
	}

	/** Create a controller form for one detector */
	protected ControllerForm createControllerForm(Detector d) {
		Controller c = d.getController();
		if(c != null)
			return new ControllerForm(session, c);
		else
			return null;
	}

	/** Determine if a controller form is available */
	public boolean hasController() {
		return true;
	}
}
