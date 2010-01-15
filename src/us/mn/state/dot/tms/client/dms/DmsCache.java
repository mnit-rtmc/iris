/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.Glyph;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Cache for DMS proxy objects.
 *
 * @author Douglas Lau
 */
public class DmsCache {

	/** Cache of font proxies */
	protected final TypeCache<Font> fonts;

	/** Get the font type cache */
	public TypeCache<Font> getFonts() {
		return fonts;
	}

	/** Cache of glyph proxies */
	protected final TypeCache<Glyph> glyphs;

	/** Get the glyph type cache */
	public TypeCache<Glyph> getGlyphs() {
		return glyphs;
	}

	/** Cache of sign messages */
	protected final TypeCache<SignMessage> sign_messages;

	/** Get the sign message cache */
	public TypeCache<SignMessage> getSignMessages() {
		return sign_messages;
	}

	/** Cache of quick messages */
	protected final TypeCache<QuickMessage> quick_messages;

	/** Get the quick message cache */
	public TypeCache<QuickMessage> getQuickMessages() {
		return quick_messages;
	}

	/** Cache of dynamic message signs */
	protected final TypeCache<DMS> dmss;

	/** Get the dynamic message sign cache */
	public TypeCache<DMS> getDMSs() {
		return dmss;
	}

	/** Cache of sign groups */
	protected final TypeCache<SignGroup> sign_groups;

	/** Get the sign group cache */
	public TypeCache<SignGroup> getSignGroups() {
		return sign_groups;
	}

	/** Cache of DMS sign groups */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Get the DMS sign group cache */
	public TypeCache<DmsSignGroup> getDmsSignGroups() {
		return dms_sign_groups;
	}

	/** Cache of sign text */
	protected final TypeCache<SignText> sign_text;

	/** Get the sign text cache */
	public TypeCache<SignText> getSignText() {
		return sign_text;
	}

	/** Create a new DMS cache */
	public DmsCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		fonts = new TypeCache<Font>(Font.class, client);
		glyphs = new TypeCache<Glyph>(Glyph.class, client);
		sign_messages = new TypeCache<SignMessage>(SignMessage.class,
			client);
		quick_messages = new TypeCache<QuickMessage>(QuickMessage.class,
			client);
		dmss = new TypeCache<DMS>(DMS.class, client);
		sign_groups = new TypeCache<SignGroup>(SignGroup.class, client);
		dms_sign_groups = new TypeCache<DmsSignGroup>(
			DmsSignGroup.class, client);
		sign_text = new TypeCache<SignText>(SignText.class, client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(fonts);
		client.populateReadable(glyphs);
		client.populateReadable(sign_messages);
		client.populateReadable(quick_messages);
		client.populateReadable(dmss);
		if(client.canRead(DMS.SONAR_TYPE)) {
			dmss.ignoreAttribute("operation");
			dmss.ignoreAttribute("opStatus");
		}
		client.populateReadable(sign_groups);
		client.populateReadable(dms_sign_groups);
		client.populateReadable(sign_text);
	}
}
