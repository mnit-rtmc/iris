/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Iteris Inc.
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
import java.util.HashMap;
import us.mn.state.dot.tms.Word;
import us.mn.state.dot.tms.WordHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for dictionary words.
 * @author Michael Darter
 */
public class DictTableModel extends ProxyTableModel<Word> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Word> descriptor(Session s) {
		return new ProxyDescriptor<Word>(
			s.getSonarState().getWords(), false
		);
	}

	/** Table model is for allowed or banned words */
	private final boolean allowed_words;

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Word>> createColumns() {
		ArrayList<ProxyColumn<Word>> cols =
			new ArrayList<ProxyColumn<Word>>(2);
		cols.add(new ProxyColumn<Word>("dictionary.word", 140) {
			public Object getValueAt(Word wo) {
				return WordHelper.decode(wo.getName());
			}
		});
		cols.add(new ProxyColumn<Word>("dictionary.abbr", 80) {
			public Object getValueAt(Word wo) {
				return wo.getAbbr();
			}
			// only allowed words can have abbreviation
			public boolean isEditable(Word wo) {
				return canUpdate(wo) && allowed_words;
			}
			public void setValueAt(Word wo, Object abbr){
				if (wo != null && abbr != null)
					wo.setAbbr(abbr.toString());
			}
		});
		return cols;
	}

	/** Constructor
	 * @param s Session
	 * @param allowed True if table model is for allowed words else 
	 * 		  false for banned */
	public DictTableModel(Session s, boolean allowed) {
		super(s, descriptor(s));
		allowed_words = allowed;
	}

	/** Check if a proxy is included in the list. This is used 
	 * to determine if the added word is in the allowed or 
	 * banned table model.
	 * @return True to add the word else false */
	@Override
	protected boolean check(Word proxy) {
		return (proxy == null ? 
			false : proxy.getAllowed() == allowed_words);
	}

	/** Create an object with the given name
	 * @param name Unencoded word */
	@Override
	public void createObject(String name) {
		if (name.length() > 0) {
			String en = WordHelper.encode(name);
			descriptor.cache.createObject(en, createAttrs(name));
		}
	}

	/** Create attrs for a new word
	 * @param name Unencoded word to create attributes for
	 * @return Hashmap of the word's attributes */
	private HashMap<String, Object> createAttrs(String name) {
		Word wo = WordHelper.lookup(name);
		String abbr = (wo == null) ? "" : wo.getAbbr();
		boolean aw = (wo == null) ? allowed_words : wo.getAllowed();
		HashMap<String, Object> attrs =	new HashMap<String, Object>();
		attrs.put("abbr", abbr);
		attrs.put("allowed", aw);
		return attrs;
	}

	/** Get the row height */
	@Override
	public int getRowHeight() {
		return 20;
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 20;
	}
}
