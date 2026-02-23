/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import static us.mn.state.dot.tms.client.widget.IOptionPane.showHint;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Table model for message lines.
 *
 * @author Douglas Lau
 */
public class MsgLineTableModel extends ProxyTableModel<MsgLine> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<MsgLine> descriptor(Session s) {
		return new ProxyDescriptor<MsgLine>(
			s.getSonarState().getDmsCache().getMsgLine(),
			false
		);
	}

	/** Default rank */
	static private final short DEF_RANK = (short) 50;

	/** Format MULTI string */
	static private String formatMulti(Object value) {
		return new MultiString(value.toString()).normalizeLine()
			.toString();
	}

	/** Cell renderer for this table */
	static private final DefaultTableCellRenderer RENDERER =
		new DefaultTableCellRenderer();
	static {
		RENDERER.setHorizontalAlignment(SwingConstants.CENTER);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<MsgLine>> createColumns() {
		ArrayList<ProxyColumn<MsgLine>> cols =
			new ArrayList<ProxyColumn<MsgLine>>(3);
		cols.add(new ProxyColumn<MsgLine>("dms.line", 36, Short.class){
			public Object getValueAt(MsgLine ml) {
				return ml.getLine();
			}
			public boolean isEditable(MsgLine ml) {
				return canWrite(ml);
			}
			public void setValueAt(MsgLine ml, Object value) {
				if (value instanceof Number) {
					selected = ml.getName();
					Number n = (Number) value;
					ml.setLine(n.shortValue());
				}
			}
		});
		cols.add(new ProxyColumn<MsgLine>("dms.rank", 40, Short.class)
		{
			public Object getValueAt(MsgLine ml) {
				return ml.getRank();
			}
			public boolean isEditable(MsgLine ml) {
				return canWrite(ml);
			}
			public void setValueAt(MsgLine ml, Object value) {
				if (value instanceof Number) {
					selected = ml.getName();
					Number n = (Number) value;
					ml.setRank(n.shortValue());
				}
			}
			protected TableCellEditor createCellEditor() {
				return new RankCellEditor();
			}
		});
		cols.add(new ProxyColumn<MsgLine>("dms.multi", 320) {
			public Object getValueAt(MsgLine ml) {
				return ml.getMulti();
			}
			public boolean isEditable(MsgLine ml) {
				return canWrite(ml);
			}
			public void setValueAt(MsgLine ml, Object value) {
				selected = ml.getName();
				ml.setMulti(formatMulti(value));
			}
			protected TableCellRenderer createCellRenderer() {
				return RENDERER;
			}
		});
		return cols;
	}

	/** Message pattern */
	private final MsgPattern msg_pattern;

	/** Message line creator */
	private final MsgLineCreator creator;

	/** Name of selected message line */
	protected String selected;

	/** Create a new message line table model */
	public MsgLineTableModel(Session s, MsgPattern pat) {
		super(s, descriptor(s), 12);
		msg_pattern = pat;
		creator = new MsgLineCreator(s);
	}

	/** Get a proxy comparator */
	@Override
	protected MsgLineComparator comparator() {
		return new MsgLineComparator();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(MsgLine proxy) {
		return proxy.getMsgPattern() == msg_pattern;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return msg_pattern != null
		    && super.canAdd(msg_pattern.getName() + "_XX");
	}

	/** Create a new message line */
	@Override
	public void createObject(String v) {
		if (msg_pattern != null) {
			String ms = formatMulti(v);
			if (ms.length() > 0) {
				selected = creator.create(msg_pattern,
					(short) 1, ms, DEF_RANK);
			}
		}
	}
}
