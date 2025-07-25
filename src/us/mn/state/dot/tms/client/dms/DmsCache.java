/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Cache for DMS proxy objects.
 *
 * @author Douglas Lau
 */
public class DmsCache {

	/** Cache of font proxies */
	private final TypeCache<Font> fonts;

	/** Get the font type cache */
	public TypeCache<Font> getFonts() {
		return fonts;
	}

	/** Font proxy list model */
	private final ProxyListModel<Font> font_model;

	/** Get the font list model */
	public ProxyListModel<Font> getFontModel() {
		return font_model;
	}

	/** Cache of glyph proxies */
	private final TypeCache<Glyph> glyphs;

	/** Cache of sign configs */
	private final TypeCache<SignConfig> sign_configs;

	/** Get the sign configs */
	public TypeCache<SignConfig> getSignConfigs() {
		return sign_configs;
	}

	/** Cache of sign details */
	private final TypeCache<SignDetail> sign_details;

	/** Get the sign details */
	public TypeCache<SignDetail> getSignDetails() {
		return sign_details;
	}

	/** Cache of sign messages */
	private final TypeCache<SignMessage> sign_messages;

	/** Get the sign message cache */
	public TypeCache<SignMessage> getSignMessages() {
		return sign_messages;
	}

	/** Cache of msg patterns */
	private final TypeCache<MsgPattern> msg_patterns;

	/** Get the msg pattern cache */
	public TypeCache<MsgPattern> getMsgPatterns() {
		return msg_patterns;
	}

	/** Cache of dynamic message signs */
	private final TypeCache<DMS> dmss;

	/** Get the dynamic message sign cache */
	public TypeCache<DMS> getDMSs() {
		return dmss;
	}

	/** DMS proxy list model */
	private final ProxyListModel<DMS> dms_model;

	/** Get the DMS list model */
	public ProxyListModel<DMS> getDMSModel() {
		return dms_model;
	}

	/** Cache of message lines */
	private final TypeCache<MsgLine> msg_lines;

	/** Get the message line cache */
	public TypeCache<MsgLine> getMsgLine() {
		return msg_lines;
	}

	/** Create a new DMS cache */
	public DmsCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		fonts = new TypeCache<Font>(Font.class, client);
		font_model = new ProxyListModel<Font>(fonts);
		font_model.initialize();
		glyphs = new TypeCache<Glyph>(Glyph.class, client);
		sign_configs = new TypeCache<SignConfig>(SignConfig.class,
			client);
		sign_details = new TypeCache<SignDetail>(SignDetail.class,
			client);
		sign_messages = new TypeCache<SignMessage>(SignMessage.class,
			client);
		msg_patterns = new TypeCache<MsgPattern>(MsgPattern.class,
			client);
		dmss = new TypeCache<DMS>(DMS.class, client);
		dms_model = new ProxyListModel<DMS>(dmss);
		dms_model.initialize();
		msg_lines = new TypeCache<MsgLine>(MsgLine.class, client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(fonts);
		client.populateReadable(glyphs);
		client.populateReadable(sign_configs);
		client.populateReadable(sign_details);
		client.populateReadable(sign_messages);
		client.populateReadable(dmss);
		if (client.canRead(DMS.SONAR_TYPE)) {
			dmss.ignoreAttribute("operation");
			dmss.ignoreAttribute("status");
			dmss.ignoreAttribute("pixelFailures");
			// We can't ignore msgCurrent because
			// DmsCellRenderer lists need the updates
			dmss.ignoreAttribute("msgSched");
		}
		client.populateReadable(msg_patterns);
		client.populateReadable(msg_lines);
	}
}
