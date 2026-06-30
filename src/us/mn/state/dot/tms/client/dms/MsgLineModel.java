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
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
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
public class MsgLineModel extends ProxyTableModel<MsgLine> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<MsgLine> descriptor(Session s) {
		return new ProxyDescriptor<MsgLine>(
			s.getSonarState().getDmsCache().getMsgLine(),
			false,  /* has_properties */
			true,   /* has_create_delete */
			false   /* has_name */
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
			new ArrayList<ProxyColumn<MsgLine>>(4);
		cols.add(new ProxyColumn<MsgLine>("hashtag", 100) {
			public Object getValueAt(MsgLine ml) {
				return ml.getHashtag();
			}
		});
		cols.add(new ProxyColumn<MsgLine>("dms.line", 36, Short.class){
			public Object getValueAt(MsgLine ml) {
				return ml.getLine();
			}
			public boolean isEditable(MsgLine ml) {
				return canWrite(ml);
			}
			public void setValueAt(MsgLine ml, Object value) {
				if (value instanceof Number) {
					Number n = (Number) value;
					ml.setLine(n.shortValue());
					selected = ml;
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
					Number n = (Number) value;
					ml.setRank(n.shortValue());
					selected = ml;
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
				ml.setMulti(formatMulti(value));
				selected = ml;
			}
			protected TableCellRenderer createCellRenderer() {
				return RENDERER;
			}
		});
		return cols;
	}

	/** Compose hashtag */
	private final String hashtag;

	/** Filter hashtags */
	private final Hashtags tags;

	/** Message line creator */
	private final MsgLineCreator creator;

	/** Selected message line */
	private MsgLine selected;

	/** Create a new message line model */
	public MsgLineModel(Session s, String ht) {
		super(s, descriptor(s), 12);
		hashtag = ht;
		tags = new Hashtags(ht);
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
		return hashtag == null || tags.contains(proxy.getHashtag());
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return !tags.tags().isEmpty() && super.canAdd("ml_XX");
	}

	/** Create a new message line */
	@Override
	public void createObject(String v) {
		if (selected != null) {
			MsgLine ml = selected;
			String nm = creator.create(ml.getHashtag(),
				ml.getLine(), ml.getMulti(), ml.getRank());
			selected = MsgLineHelper.lookup(nm);
		} else if (hashtag != null) {
			String nm = creator.create(hashtag,
				(short) 1, "", DEF_RANK);
			selected = MsgLineHelper.lookup(nm);
		}
	}
}
