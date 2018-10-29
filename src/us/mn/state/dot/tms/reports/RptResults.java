/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Container for report results.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptResults {

	//-------------------------------------------
	
	private ArrayList<RptResultItem> list =
			new ArrayList<RptResultItem>();

	public RptResults() {
		super();
	}

	public RptResults(ArrayList<RptResultItem> list, int sortcol, Comparator<RptResultItem> compareResults) {
		super();
		this.list = list;
		this.sortcol = sortcol;
		this.compareResults = compareResults;
	}

	// Create a RptResults from a serialization string
	public RptResults(String ssResults) throws IOException {
		initFromResultsString(ssResults);
	}

	public void clear() {
		list.clear();
	}

	public void addRptRecord(RptResultItem item) {
		list.add(item);
	}
	
	public List<RptResultItem> getRptResults() {
		return list;
	}

	public Iterator<RptResultItem> iterateRptResults() {
		return list.iterator();
	}
	
	public int resultsSize(){
		return list.size();
	}

	//-------------------------------------------

	public final static int SORTonDEVICENAME  = 1;
	public final static int SORTonDATETIME    = 2;
	public final static int SORTonUSERNAME    = 3;
	public final static int SORTonDESCRIPTION = 4;
	
	protected int sortcol = 0; // initially, unsorted

	/** stable-sort comparator that sorts on sortcol column */
	public Comparator<RptResultItem> compareResults	= new Comparator<RptResultItem>() {
			public int compare(RptResultItem a, RptResultItem b) {
				//FIXME: Substitute alphanum sort for device names */
				switch (sortcol) {
					case SORTonDEVICENAME:
						return a.getName().compareTo(b.getName());
					case SORTonDATETIME:
						return a.getDatetimeLong().compareTo(b.getDatetimeLong());
					case SORTonUSERNAME:
						return a.getUsername().compareTo(b.getUsername());
					case SORTonDESCRIPTION:
						return a.getDescription().compareTo(b.getDescription());
					case -SORTonDEVICENAME:
						return b.getName().compareTo(a.getName());
					case -SORTonDATETIME:
						return b.getDatetimeLong().compareTo(a.getDatetimeLong());
					case -SORTonUSERNAME:
						return b.getUsername().compareTo(a.getUsername());
					case -SORTonDESCRIPTION:
						return b.getDescription().compareTo(a.getDescription());
				}
				return 0;
			}		
	};
	
	/** Sort report list.  Calling with same column
	 *  parameter (1-4) a second time will reverse
	 *  the sort. */
	public void sort(int column) {
		sortcol = (sortcol == column) ? -column : column;
		Collections.sort(list, compareResults); 
	}

	//-------------------------------------------
	
	RptStringSet exceptions = new RptStringSet("exceptions");
	
	public void addException(String message) {
		exceptions.add(message);
	}
		
	//-------------------------------------------

	/** Converts result to a composite-results String. */
	public String toResultsString() {
		RptStringSetMap rssm = new RptStringSetMap();
		RptResultItem item;
		RptStringSet rssRow;

		// convert to RptStringSetMap
		int i = 0;
		Iterator<RptResultItem> iter = list.iterator();
		while (iter.hasNext()) {
			item = iter.next();
			++i;
			rssRow = new RptStringSet("row" + i);
			rssRow.add(item.getDatetimeLong().toString());
			rssRow.add(item.getName());
			rssRow.add(item.getUsername());
			rssRow.add(item.getDescription());
			rssm.add(rssRow);
		}

		return rssm.toCompositeString();
	}
	
	/** Initialize from a composite-results String. */
	public void initFromResultsString(String sRes) {
		if (sRes == null)
			sRes = "";
		try {
			RptStringSetMap rssm;
			rssm = new RptStringSetMap(sRes);
			RptStringSet rssRow;
			RptResultItem it;
			String [] strs = new String[0];
			int row = 1;
			String key = "row1";
			while ((rssRow = rssm.get(key)) != null) {
				strs = rssRow.toArray(strs);
				it = new RptResultItem(Long.parseLong(strs[0]), strs[1], strs[2], strs[3]);
				addRptRecord(it);
				++row;
				key = "row" + row;
			}
			this.sortcol = SORTonDATETIME;
//			this.compareResults = compareResults;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Obtain length of maximum description field in results list */
	public int getMaxDescriptionLength() {
		RptResultItem item;
		int maxLen = 0;
		int len;

		Iterator<RptResultItem> iter = list.iterator();
		while (iter.hasNext()) {
			item = iter.next();
			len = item.getDescription().length();
			if (maxLen < len)
				maxLen = len;
		}

		return maxLen;
	}
}
