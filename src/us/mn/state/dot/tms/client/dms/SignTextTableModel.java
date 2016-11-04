/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Table model for sign text.
 *
 * @author Douglas Lau
 */
public class SignTextTableModel extends ProxyTableModel<SignText> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<SignText> descriptor(Session s) {
		return new ProxyDescriptor<SignText>(
			s.getSonarState().getDmsCache().getSignText(),
			false
		);
	}

	/** Default rank */
	static private final short DEF_RANK = (short) 50;

	/** Format MULTI string */
	static private String formatMulti(Object value) {
		return new MultiString(value.toString()).normalize().trim();
	}

	/** Cell renderer for this table */
	static private final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<SignText>> createColumns() {
		ArrayList<ProxyColumn<SignText>> cols =
			new ArrayList<ProxyColumn<SignText>>(3);
		cols.add(new ProxyColumn<SignText>("dms.line", 36, Short.class){
			public Object getValueAt(SignText st) {
				return st.getLine();
			}
			public boolean isEditable(SignText st) {
				return canUpdate(st);
			}
			public void setValueAt(SignText st, Object value) {
				if (value instanceof Number) {
					Number n = (Number)value;
					st.setLine(n.shortValue());
				}
			}
		});
		cols.add(new ProxyColumn<SignText>("dms.multi", 400) {
			public Object getValueAt(SignText st) {
				return st.getMulti();
			}
			public boolean isEditable(SignText st) {
				return canUpdate(st);
			}
			public void setValueAt(SignText st, Object value) {
				st.setMulti(formatMulti(value));
			}
			protected TableCellRenderer createCellRenderer() {
				return RENDERER;
			}
		});
		cols.add(new ProxyColumn<SignText>("dms.rank", 40,
			Short.class)
		{
			public Object getValueAt(SignText st) {
				return st.getRank();
			}
			public boolean isEditable(SignText st) {
				return canUpdate(st);
			}
			public void setValueAt(SignText st, Object value) {
				if (value instanceof Number) {
					Number n = (Number)value;
					st.setRank(n.shortValue());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new RankCellEditor();
			}
		});
		return cols;
	}

	/** Sign group */
	private final SignGroup group;

	/** Sign text creator */
	private final SignTextCreator creator;

	/** Create a new sign text table model */
	public SignTextTableModel(Session s, SignGroup g) {
		super(s, descriptor(s), 12);
		group = g;
		creator = new SignTextCreator(s);
	}

	/** Get a proxy comparator */
	@Override
	protected SignTextComparator comparator() {
		return new SignTextComparator();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(SignText proxy) {
		return proxy.getSignGroup() == group;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return group != null && super.canAdd(group.getName() + "_XX");
	}

	/** Create a new sign text message using default line and rank values */
	@Override
	public void createObject(String v) {
		if (group != null) {
			String m = formatMulti(v);
			if (m.length() > 0)
				creator.create(group, (short)1, m, DEF_RANK);
		}
	}
}
