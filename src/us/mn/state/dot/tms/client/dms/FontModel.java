/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for DMS fonts.
 *
 * @author Douglas Lau
 */
public class FontModel extends ProxyTableModel<Font> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Font> descriptor(Session s) {
		return new ProxyDescriptor<Font>(
			s.getSonarState().getDmsCache().getFonts(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Font>> createColumns() {
		ArrayList<ProxyColumn<Font>> cols =
			new ArrayList<ProxyColumn<Font>>(7);
		cols.add(new ProxyColumn<Font>("font", 140) {
			public Object getValueAt(Font f) {
				return f.getName();
			}
		});
		cols.add(new ProxyColumn<Font>("font.number", 70,
			Integer.class)
		{
			public Object getValueAt(Font f) {
				return f.getNumber();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setNumber((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Font>("font.height", 70,Integer.class){
			public Object getValueAt(Font f) {
				return f.getHeight();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setHeight((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Font>("font.width", 70, Integer.class){
			public Object getValueAt(Font f) {
				return f.getWidth();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setWidth((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Font>("font.spacing.line", 90,
			Integer.class)
		{
			public Object getValueAt(Font f) {
				return f.getLineSpacing();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setLineSpacing((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Font>("font.spacing.char", 90,
			Integer.class)
		{
			public Object getValueAt(Font f) {
				return f.getCharSpacing();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setCharSpacing((Integer)value);
			}
		});
		cols.add(new ProxyColumn<Font>("font.version",74,Integer.class){
			public Object getValueAt(Font f) {
				return f.getVersionID();
			}
			public boolean isEditable(Font f) {
				return canUpdate(f);
			}
			public void setValueAt(Font f, Object value) {
				if (value instanceof Integer)
					f.setVersionID((Integer)value);
			}
		});
		return cols;
	}

	/** Create a new font table model */
	public FontModel(Session s) {
		super(s, descriptor(s),
		      true,	/* has_create_delete */
		      true);	/* has_name */
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 6;
	}
}
