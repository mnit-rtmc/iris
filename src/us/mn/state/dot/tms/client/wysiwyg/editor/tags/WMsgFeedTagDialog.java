/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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


package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFeedMsg;

/**
 * WYSIWYG DMS Message Editor dialog form for editing MsgFeed action tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WMsgFeedTagDialog extends WMultiTagDialog {
	protected WtFeedMsg editTok;
	private WTagParamSonarObjectField<CommLink> fidField;
	
	private String fid = "";
	private CommLink feedCommLink;
	
	public WMsgFeedTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtFeedMsg) tok;
		fid = editTok.getFeedID();
		feedCommLink = CommLinkHelper.lookup(fid);
	}
	
	@Override
	protected void addTagForm() {
		// make a ComboBox containing MsgFeed CommLinks
		CommLink msgFeeds[] = getMsgFeedCommLinks();
		fidField = new WTagParamSonarObjectField<CommLink>(
				msgFeeds, feedCommLink, true);
		
		// add a renderer that displays the name and description
		fidField.setRenderer(new CommLinkListRenderer());
		addField("wysiwyg.msgfeed_dialog.fid", fidField);
	}

	@Override
	protected WtFeedMsg makeNewTag() {
		feedCommLink = fidField.getSelectedItem();
		fid = feedCommLink.getName();
		return new WtFeedMsg(fid);
	}
	
	/** Return a list of CommLinks that correspond to MsgFeeds currently in
	 *  the database.
	 */
	private static CommLink[] getMsgFeedCommLinks() {
		// look through all CommLinks for ones with protocol MSG_FEED
		ArrayList<CommLink> msgFeeds = new ArrayList<CommLink>();
		Iterator<CommLink> it = CommLinkHelper.iterator();
		while (it.hasNext()) {
			CommLink c = it.next();
			if (c.getProtocol() == CommProtocol.MSG_FEED.ordinal())
				msgFeeds.add(c);
		}
		CommLink arr[] = new CommLink[msgFeeds.size()];
		msgFeeds.toArray(arr);
		return arr;
	}
	
	/** Renderer for displaying CommLinks with "Description: (Name)" */
	private class CommLinkListRenderer implements ListCellRenderer<SonarObject> {
		private DefaultListCellRenderer cell = new DefaultListCellRenderer();
		
		@Override  
		public Component getListCellRendererComponent(
				JList<?extends SonarObject> list, SonarObject o,
		      int index, boolean isSelected, boolean cellHasFocus) {
			CommLink c = (CommLink) o;
			cell.getListCellRendererComponent(
					list, c, index, isSelected, cellHasFocus);
			String txt = (c != null) ? String.format("%s (%s)",
					c.getDescription(), c.getName()) : "";
			cell.setText(txt);
		    return cell;
		  }
	}
}
