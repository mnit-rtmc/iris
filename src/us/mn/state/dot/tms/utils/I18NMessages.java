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
 * Class description
 *
 *
 * @version    Initial release, 08/05/19
 * @author     p.w.wong    
 */
public class I18NMessages
{
	static public ResourceBundle i18nMessages;

	/**
	 * Method description
	 *
	 *
	 * @param props
	 */
	static public void initialize(Properties props) {
		Locale currentLocale;

		System.out.println("Opening I18N resources; Language "
				   + props.getProperty("language")
				   + " Country " + props.getProperty("country")
				   + " Variant "
				   + props.getProperty("variant"));

		if((props.getProperty("variant") != null)
			&& (props.getProperty("language") != null)
			&& (props.getProperty("country") != null)) {
			currentLocale =
				new Locale(props.getProperty("language"),
					   props.getProperty("country"),
					   props.getProperty("variant"));
			i18nMessages = ResourceBundle.getBundle(
				"MessagesBundle.MessagesBundle", currentLocale);
		} else if((props.getProperty("variant") == null)
			&& (props.getProperty("language") != null)
			&& (props.getProperty("country") != null)) {
			currentLocale =
				new Locale(props.getProperty("language"),
					   props.getProperty("country"));
			i18nMessages = ResourceBundle.getBundle(
				"MessagesBundle.MessagesBundle", currentLocale);
		} else if((props.getProperty("country") == null)
			&& (props.getProperty("language") != null)) {
			currentLocale =
				new Locale(props.getProperty("language"));
			i18nMessages = ResourceBundle.getBundle(
				"MessagesBundle.MessagesBundle", currentLocale);
		} else {
			i18nMessages = ResourceBundle.getBundle(
				"MessagesBundle.MessagesBundle");
		}

		System.out.println("For " + i18nMessages.getString("DOT")
				   + " district "
				   + i18nMessages.getString("District"));

	}

	/**
	 * Method description
	 *
	 */
	static public void PrintJunk() {
		System.out.println("XXX printjunk");
	}
}
