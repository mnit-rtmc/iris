/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2011  Minnesota Department of Transportation
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

import java.util.TreeSet;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextTableModel extends ProxyTableModel<SignText> {

	/** Cell renderer for this table */
	static protected final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<SignText>("Line", 36, Short.class) {
			public Object getValueAt(SignText st) {
				return st.getLine();
			}
			public boolean isEditable(SignText st) {
				return canUpdate(st);
			}
			public void setValueAt(SignText st, Object value) {
				if(value instanceof Number) {
					Number n = (Number)value;
					st.setLine(n.shortValue());
				}
			}
		},
		new ProxyColumn<SignText>("MULTI", 400) {
			public Object getValueAt(SignText st) {
				return st.getMulti();
			}
			public boolean isEditable(SignText st) {
				if(st != null)
					return canUpdate(st);
				else
					return canAdd();
			}
			public void setValueAt(SignText st, Object value) {
				String v = formatMulti(value);
				if(st != null)
					st.setMulti(v);
				else if(v.length() > 0)
					createSignText(v);
			}
			protected TableCellRenderer createCellRenderer() {
				return RENDERER;
			}
		},
		new ProxyColumn<SignText>("Priority", 48, Short.class) {
			public Object getValueAt(SignText st) {
				return st.getPriority();
			}
			public boolean isEditable(SignText st) {
				return canUpdate(st);
			}
			public void setValueAt(SignText st, Object value) {
				if(value instanceof Number) {
					Number n = (Number)value;
					st.setPriority(n.shortValue());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new PriorityCellEditor();
			}
		}
	    };
	}

	/** Format MULTI string */
	static protected String formatMulti(Object value) {
		return MultiString.normalize(value.toString().trim());
	}

	/** Create an empty set of proxies */
	protected TreeSet<SignText> createProxySet() {
		return new TreeSet<SignText>(new SignTextComparator());
	}

	/** Add a new proxy to the table model */
	protected int doProxyAdded(SignText proxy) {
		if(proxy.getSignGroup() == group)
			return super.doProxyAdded(proxy);
		else
			return -1;
	}

	/** Sign group */
	protected final SignGroup group;

	/** Sign text creator */
	protected final SignTextCreator creator;

	/** Create a new sign text table model */
	public SignTextTableModel(Session s, SignGroup g) {
		super(s, s.getSonarState().getDmsCache().getSignText());
		group = g;
		creator = new SignTextCreator(s);
	}

	/** Create a new sign text message using the current line and priority
	 * values */
	protected void createSignText(String multi) {
		creator.create(group, (short)1, multi, (short)50);
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return creator.canAddSignText(group.getName() + "_XX");
	}
}
