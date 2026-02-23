/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2026  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for editing and creating message patterns.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternTableModel extends ProxyTableModel<MsgPattern> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<MsgPattern> descriptor(Session s) {
		return new ProxyDescriptor<MsgPattern>(
			s.getSonarState().getDmsCache().getMsgPatterns(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<MsgPattern>> createColumns() {
		ArrayList<ProxyColumn<MsgPattern>> cols =
			new ArrayList<ProxyColumn<MsgPattern>>(3);
		cols.add(new ProxyColumn<MsgPattern>("msg.pattern.name", 168){
			public Object getValueAt(MsgPattern pat) {
				return pat.getName();
			}
		});
		cols.add(new ProxyColumn<MsgPattern>(
			"msg.pattern.compose.hashtag", 110)
		{
			public Object getValueAt(MsgPattern pat) {
				return pat.getComposeHashtag();
			}
			public boolean isEditable(MsgPattern pat) {
				return canWrite(pat);
			}
			public void setValueAt(MsgPattern pat, Object value) {
				String cht = Hashtags.normalize(value.toString());
				pat.setComposeHashtag(cht);
			}
		});
		cols.add(new ProxyColumn<MsgPattern>(
			"msg.pattern.prototype", 168)
		{
			public Object getValueAt(MsgPattern pat) {
				return pat.getPrototype();
			}
			public boolean isEditable(MsgPattern pat) {
				return canWrite(pat);
			}
			public void setValueAt(MsgPattern pat, Object value) {
				pat.setPrototype(value.toString());
			}
		});
		return cols;
	}

	/** Create a new table model.
	 * @param s Session */
	public MsgPatternTableModel(Session s) {
		super(s, descriptor(s), 18, 20);
	}
}
