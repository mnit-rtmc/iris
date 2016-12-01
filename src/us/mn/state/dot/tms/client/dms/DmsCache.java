/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignConfig;
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

	/** Get the glyph type cache */
	public TypeCache<Glyph> getGlyphs() {
		return glyphs;
	}

	/** Cache of sign configs */
	private final TypeCache<SignConfig> sign_configs;

	/** Get the sign configs */
	public TypeCache<SignConfig> getSignConfigs() {
		return sign_configs;
	}

	/** Cache of sign messages */
	private final TypeCache<SignMessage> sign_messages;

	/** Get the sign message cache */
	public TypeCache<SignMessage> getSignMessages() {
		return sign_messages;
	}

	/** Cache of quick messages */
	private final TypeCache<QuickMessage> quick_messages;

	/** Get the quick message cache */
	public TypeCache<QuickMessage> getQuickMessages() {
		return quick_messages;
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

	/** Cache of sign groups */
	private final TypeCache<SignGroup> sign_groups;

	/** Get the sign group cache */
	public TypeCache<SignGroup> getSignGroups() {
		return sign_groups;
	}

	/** Cache of DMS sign groups */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Get the DMS sign group cache */
	public TypeCache<DmsSignGroup> getDmsSignGroups() {
		return dms_sign_groups;
	}

	/** Cache of sign text */
	private final TypeCache<SignText> sign_text;

	/** Get the sign text cache */
	public TypeCache<SignText> getSignText() {
		return sign_text;
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
		sign_messages = new TypeCache<SignMessage>(SignMessage.class,
			client);
		quick_messages = new TypeCache<QuickMessage>(QuickMessage.class,
			client);
		dmss = new TypeCache<DMS>(DMS.class, client);
		dms_model = new ProxyListModel<DMS>(dmss);
		dms_model.initialize();
		sign_groups = new TypeCache<SignGroup>(SignGroup.class, client);
		dms_sign_groups = new TypeCache<DmsSignGroup>(
			DmsSignGroup.class, client);
		sign_text = new TypeCache<SignText>(SignText.class, client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(fonts);
		client.populateReadable(glyphs);
		client.populateReadable(sign_configs);
		client.populateReadable(sign_messages);
		client.populateReadable(dmss);
		if(client.canRead(DMS.SONAR_TYPE)) {
			dmss.ignoreAttribute("operation");
			dmss.ignoreAttribute("opStatus");
			dmss.ignoreAttribute("minCabinetTemp");
			dmss.ignoreAttribute("maxCabinetTemp");
			dmss.ignoreAttribute("minAmbientTemp");
			dmss.ignoreAttribute("maxAmbientTemp");
			dmss.ignoreAttribute("minHousingTemp");
			dmss.ignoreAttribute("maxHousingTemp");
			dmss.ignoreAttribute("lightOutput");
			dmss.ignoreAttribute("photocellStatus");
			dmss.ignoreAttribute("powerStatus");
			// We can't ignore msgCurrent because
			// DmsCellRenderer lists need the updates
			dmss.ignoreAttribute("msgSched");
			dmss.ignoreAttribute("deployTime");
			dmss.ignoreAttribute("heatTapeStatus");
		}
		client.populateReadable(sign_groups);
		client.populateReadable(dms_sign_groups);
		client.populateReadable(quick_messages);
		client.populateReadable(sign_text);
	}
}
