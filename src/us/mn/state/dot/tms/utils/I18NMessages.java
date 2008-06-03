/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.utils;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Convenience class to handle I18N messages.
 *
 *
 * @version    Initial release, 06/03/08
 * @author     p.w.wong, AHMCT
 */
public class I18NMessages
{
	/** base name for resource bundles */
	private static final String BASENAME="MessagesBundle.MessagesBundle";

	/** the resource bundle */
	static private ResourceBundle m_i18NMessages=null;

	/** class can't be instantiated */
	private I18NMessages() {}

	/**
	 * Read resource bundle and assign to public static field.
	 * This method should be called once at program startup.
	 *
	 * @param props Opened property file which contains the 
	 * 		language, country, and variant.
	 */
	static public void initialize(Properties props) {

		String l=PropertyFile.get(props,"language");
		String c=PropertyFile.get(props,"country");
		String v=PropertyFile.get(props,"variant");

		System.err.println("Opening I18N resources: Language=" +
			l + ", Country=" + c + ", Variant=" + v);

		// load bundle using: lang, country, variant
		try {
			Locale loc=null;
			if (v!=null && l!=null && c!=null) {
				loc = new Locale(l,c,v);
				m_i18NMessages = ResourceBundle.getBundle(
					BASENAME, loc);
			}
		} catch (Exception ex) {
			System.err.println("Error, could not load message bundle: "+ex);
			m_i18NMessages=null;
		}
		if (m_i18NMessages==null) {
			System.err.println("Error: failed to open I18N resource bundle: "+
			BASENAME+"_"+l+"_"+c+"_"+v);
		}
	}

	/** get string using id */
	static public String get(String id) {
		String s="Undefined I18N string";
		if (id==null || id.length()==0)
			return s;
		if (m_i18NMessages==null) {
			System.err.println("Error: load message bundle not loaded.");
			return "Message bundle not read";
		}
		try {
			s=m_i18NMessages.getString(id);
		} catch (Exception ex) {
			System.err.println(
				"Error: attempting to read id ("+id+
				") from bundle, ex="+ex);
		}
		return s;
	}
}

